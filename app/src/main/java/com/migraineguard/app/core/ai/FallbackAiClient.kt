package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.domain.NutritionScanResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

/**
 * Tries providers in priority order: Gemini → Groq → OpenRouter → OfflineHealthBrain.
 * Uses per-user DataStore keys only for cloud providers; OfflineHealthBrain always available.
 */
class FallbackAiClient(
    providers: List<AiProvider>,
    private val offlineBrain: OfflineHealthBrain? = providers.filterIsInstance<OfflineHealthBrain>().firstOrNull(),
    private val providerTimeoutMillis: Long = 60_000L,
) {
    private val ordered = providers.sortedBy { it.priority }

    init { require(providerTimeoutMillis > 0) }

    suspend fun complete(prompt: String): Result<String> {
        var lastError: String? = null
        for (provider in ordered) {
            currentCoroutineContext().ensureActive()
            val result = try {
                withTimeout(providerTimeoutMillis) { provider.complete(prompt) }
            } catch (e: TimeoutCancellationException) {
                // A parent's timeout must stop the request, not trigger another provider.
                currentCoroutineContext().ensureActive()
                Result.Error("Provider timed out", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error("Provider request failed", e)
            }
            currentCoroutineContext().ensureActive()
            when (result) {
                is Result.Success -> if (result.data.isNotBlank()) return result
                    else lastError = "${provider.name}: empty response"
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
            if (hasImage) append("The photo is not included; analyze only the description. ")
            append("Food: $description. ")
            append("Reply ONLY with JSON keys: foodLabel, calories, proteinG, carbsG, fatG, notes, migraineTriggerHint.")
        }
        return when (val r = complete(prompt)) {
            is Result.Success -> parseNutrition(r.data, description)
            is Result.Error -> unavailableNutrition(description)
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
                if (!listOf("calories", "proteinG", "carbsG", "fatG").any { obj.has(it) }) {
                    return unavailableNutrition(fallbackLabel)
                }
                return NutritionScanResult(
                    foodLabel = obj.optString("foodLabel", fallbackLabel.ifBlank { "Meal" }),
                    caloriesEstimate = obj.optDouble("calories").takeIf { it.isFinite() && it >= 0 && it <= Int.MAX_VALUE }?.toInt(),
                    proteinG = obj.optDouble("proteinG").toFloat().takeIf { it.isFinite() && it >= 0 },
                    carbsG = obj.optDouble("carbsG").toFloat().takeIf { it.isFinite() && it >= 0 },
                    fatG = obj.optDouble("fatG").toFloat().takeIf { it.isFinite() && it >= 0 },
                    notes = obj.optString("notes", "AI nutrition estimate"),
                    migraineTriggerHint = obj.optString("migraineTriggerHint").ifBlank { null },
                )
            }
        }
        return unavailableNutrition(fallbackLabel)
    }

    private fun unavailableNutrition(description: String) = NutritionScanResult(
        foodLabel = description.ifBlank { "Meal" },
        caloriesEstimate = null,
        notes = "Nutrition estimate unavailable. This description is saved without calorie, macro or trigger estimates.",
        migraineTriggerHint = null,
    )
}
