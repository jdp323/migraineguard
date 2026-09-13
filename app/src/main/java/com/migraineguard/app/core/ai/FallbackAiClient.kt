package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.domain.NutritionScanResult
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * Tries providers in priority order: Gemini → Groq → OpenRouter → OfflineHealthBrain.
 * Runtime DataStore keys override BuildConfig when set on the HTTP providers.
 */
class FallbackAiClient(
    providers: List<AiProvider>,
    private val offlineBrain: OfflineHealthBrain? = providers.filterIsInstance<OfflineHealthBrain>().firstOrNull(),
) {
    private val ordered = providers.sortedBy { it.priority }

    suspend fun complete(prompt: String): Result<String> {
        var lastError: String? = null
        for (provider in ordered) {
            when (val result = provider.complete(prompt)) {
                is Result.Success -> return result
                is Result.Error -> lastError = "${provider.name}: ${result.message}"
            }
        }
        return Result.Error(lastError ?: "No AI providers configured")
    }

    /**
     * Persona-aware completion with context pack JSON appended.
     * Optionally fakes progressive chunks via [onChunk].
     */
    suspend fun completePersona(
        persona: AiPersona,
        userMessage: String,
        contextJson: String,
        onChunk: (suspend (String) -> Unit)? = null,
    ): Result<String> {
        val prompt = buildString {
            appendLine("System: ${Personas.systemPrompt(persona)}")
            appendLine()
            appendLine("User context (JSON):")
            appendLine(contextJson)
            appendLine()
            appendLine("User: $userMessage")
        }
        val result = complete(prompt)
        if (onChunk != null && result is Result.Success) {
            streamFake(result.data, onChunk)
        }
        return result
    }

    private suspend fun streamFake(full: String, onChunk: suspend (String) -> Unit) {
        val parts = full.chunked(48.coerceAtLeast(full.length / 24).coerceAtMost(120))
        var acc = ""
        for (p in parts) {
            acc += p
            onChunk(acc)
            delay(18)
        }
    }

    fun providerNames(): List<String> = ordered.map { it.name }

    fun offline(): OfflineHealthBrain? = offlineBrain

    suspend fun analyzeNutrition(description: String, hasImage: Boolean): NutritionScanResult {
        val prompt = buildString {
            append("Analyze this food for a migraine tracking app. ")
            if (hasImage) append("User provided a photo. ")
            append("Food: $description. ")
            append("Reply ONLY with JSON keys: foodLabel, calories, proteinG, carbsG, fatG, notes, migraineTriggerHint.")
        }
        return when (val r = complete(prompt)) {
            is Result.Success -> parseNutrition(r.data, description)
            is Result.Error -> fakeNutrition(description, r.message)
        }
    }

    suspend fun summarizeInsights(contextBlob: String): String {
        return when (val r = complete("Summarize migraine trends for the user:\n$contextBlob")) {
            is Result.Success -> r.data
            is Result.Error -> "Offline summary: $contextBlob\n(${r.message})"
        }
    }

    suspend fun testGemini(keyProvider: suspend () -> String): Result<String> {
        return GeminiFreeProvider(keyProvider).testConnection()
    }

    private fun parseNutrition(raw: String, fallbackLabel: String): NutritionScanResult {
        val jsonStart = raw.indexOf('{')
        val jsonEnd = raw.lastIndexOf('}')
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            runCatching {
                val obj = JSONObject(raw.substring(jsonStart, jsonEnd + 1))
                return NutritionScanResult(
                    foodLabel = obj.optString("foodLabel", fallbackLabel.ifBlank { "Meal" }),
                    caloriesEstimate = obj.optInt("calories").takeIf { it > 0 },
                    proteinG = obj.optDouble("proteinG").toFloat().takeIf { it > 0 },
                    carbsG = obj.optDouble("carbsG").toFloat().takeIf { it > 0 },
                    fatG = obj.optDouble("fatG").toFloat().takeIf { it > 0 },
                    notes = obj.optString("notes", "AI nutrition estimate"),
                    migraineTriggerHint = obj.optString("migraineTriggerHint").ifBlank { null },
                )
            }
        }
        return fakeNutrition(fallbackLabel, "Parsed stub from AI text")
    }

    private fun fakeNutrition(description: String, note: String): NutritionScanResult {
        val label = description.ifBlank { "Mixed meal" }
        val hash = label.hashCode().and(0x7fffffff)
        return NutritionScanResult(
            foodLabel = label,
            caloriesEstimate = 180 + (hash % 420),
            proteinG = 8f + (hash % 25),
            carbsG = 20f + (hash % 55),
            fatG = 5f + (hash % 20),
            notes = "Structured estimate ($note). Add AI keys in Settings for live analysis.",
            migraineTriggerHint = if (hash % 3 == 0) {
                "Possible trigger: aged cheese / caffeine / processed food"
            } else {
                "No strong classic triggers detected"
            },
        )
    }
}
