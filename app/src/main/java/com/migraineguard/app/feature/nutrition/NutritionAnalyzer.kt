package com.migraineguard.app.feature.nutrition

import com.migraineguard.app.core.domain.NutritionScanResult

/**
 * Food / camera scan analysis.
 * Later: free AI APIs with fallback chain via core/ai.FallbackAiClient.
 */
interface NutritionAnalyzer {
    suspend fun analyzeFoodLabel(description: String): NutritionScanResult
    suspend fun analyzeCameraStub(): NutritionScanResult
}

class FakeNutritionAnalyzer : NutritionAnalyzer {
    override suspend fun analyzeFoodLabel(description: String): NutritionScanResult =
        NutritionScanResult(
            foodLabel = description.ifBlank { "Unknown food" },
            caloriesEstimate = 250,
            notes = "Fake analyzer — wire Gemini/Groq/OpenRouter later.",
        )

    override suspend fun analyzeCameraStub(): NutritionScanResult =
        NutritionScanResult(
            foodLabel = "Sample salad (camera stub)",
            caloriesEstimate = 180,
            notes = "No camera capture yet; placeholder for ML Kit / AI vision.",
        )
}
