package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.domain.NutritionScanResult
import org.json.JSONObject

/**
 * Tries providers in priority order. On failure advances to the next.
 * Also exposes structured helpers used by Nutrition / Insights.
 */
class FallbackAiClient(
    providers: List<AiProvider>,
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

    fun providerNames(): List<String> = ordered.map { it.name }

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
            notes = "Structured estimate ($note). Add AI keys in local.properties for live analysis.",
            migraineTriggerHint = if (hash % 3 == 0) "Possible trigger: aged cheese / caffeine / processed food" else "No strong classic triggers detected",
        )
    }
}
