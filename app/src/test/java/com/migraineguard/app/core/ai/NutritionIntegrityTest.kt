package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.db.NutritionScanEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class NutritionIntegrityTest {
    @Test fun offlineFailureDoesNotInventNutritionOrTriggers() = runTest {
        val client = FallbackAiClient(emptyList())
        val meal = client.analyzeNutrition("cheese sandwich", hasImage = false)
        assertEquals("cheese sandwich", meal.foodLabel)
        assertNull(meal.caloriesEstimate)
        assertNull(meal.proteinG)
        assertNull(meal.carbsG)
        assertNull(meal.fatG)
        assertNull(meal.migraineTriggerHint)
        assertTrue(meal.notes.contains("unavailable"))
    }

    @Test fun malformedProviderResponseStaysUnavailable() = runTest {
        val provider = TestProvider("text", 0) { Result.Success("Try drinking water") }
        val meal = FallbackAiClient(listOf(provider)).analyzeNutrition("salad", false)
        assertNull(meal.caloriesEstimate)
        assertNull(meal.migraineTriggerHint)
    }

    @Test fun validZeroNutritionIsPreserved() = runTest {
        val provider = TestProvider("json", 0) {
            Result.Success("""{"foodLabel":"water","calories":0,"proteinG":0,"carbsG":0,"fatG":0,"notes":"Text-based estimate"}""")
        }
        val meal = FallbackAiClient(listOf(provider)).analyzeNutrition("water", false)
        assertEquals(0, meal.caloriesEstimate)
        assertEquals(0f, meal.proteinG!!, 0f)
    }

    @Test fun negativeAndNonFiniteValuesAreDiscarded() = runTest {
        val provider = TestProvider("json", 0) {
            Result.Success("""{"calories":-1,"proteinG":"NaN","carbsG":-2,"fatG":"Infinity"}""")
        }
        val meal = FallbackAiClient(listOf(provider)).analyzeNutrition("meal", false)
        assertNull(meal.caloriesEstimate)
        assertNull(meal.proteinG)
        assertNull(meal.carbsG)
        assertNull(meal.fatG)
    }

    @Test fun attachedPhotoIsNeverClaimedToHaveBeenAnalyzed() = runTest {
        var sentPrompt = ""
        val provider = object : AiProvider {
            override val name = "capture"
            override val priority = 0
            override suspend fun complete(prompt: String): Result<String> {
                sentPrompt = prompt
                return Result.Success("No estimate")
            }
        }
        FallbackAiClient(listOf(provider)).analyzeNutrition("soup", true)
        assertTrue(sentPrompt.contains("photo is not included"))
    }

    @Test fun legacyPlaceholderNumbersAndTriggersDoNotReachInsights() {
        val saved = NutritionScanEntity(
            id = 3, timestampMillis = 10, foodLabel = "sandwich", caloriesEstimate = 400,
            proteinG = 20f, carbsG = 30f, fatG = 10f,
            notes = "Structured estimate (Parsed stub from AI text). Add AI keys in Settings for live analysis.",
            imageUri = "content://example/photo", migraineTriggerHint = "Possible trigger: aged cheese",
        )
        val record = saved.toDomain()
        assertEquals(3L, record.id)
        assertEquals("sandwich", record.result.foodLabel)
        assertEquals(saved.imageUri, record.result.imageUri)
        assertNull(record.result.caloriesEstimate)
        assertNull(record.result.proteinG)
        assertNull(record.result.migraineTriggerHint)
        assertTrue(record.result.notes.startsWith("Legacy placeholder"))
    }
}
