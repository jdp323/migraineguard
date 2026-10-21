package com.migraineguard.app.feature.nutrition

import android.content.Context
import android.net.Uri
import com.migraineguard.app.core.ai.GeminiFreeProvider
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.data.db.NutritionDao
import com.migraineguard.app.core.data.db.NutritionScanEntity
import com.migraineguard.app.core.domain.NutritionScanRecord
import com.migraineguard.app.core.domain.NutritionScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NutritionAnalyzer {
    suspend fun analyzeFoodLabel(description: String, imageUri: String? = null, sendPhotoToGemini: Boolean = false): NutritionScanResult
    fun observeRecent(limit: Int = 30): Flow<List<NutritionScanRecord>>
    suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord>
    suspend fun persist(result: NutritionScanResult): Long
}

class AiNutritionAnalyzer(
    private val aiClient: FallbackAiClient,
    private val dao: NutritionDao,
    private val context: Context,
    private val imageProvider: com.migraineguard.app.core.ai.ConfiguredAiProvider,
) : NutritionAnalyzer {
    override suspend fun analyzeFoodLabel(description: String, imageUri: String?, sendPhotoToGemini: Boolean): NutritionScanResult {
        val base = if (sendPhotoToGemini && imageUri != null) {
            val image = prepareFoodImage(context, Uri.parse(imageUri))
            val prompt = "Identify the visible food, using this description if present: $description. " +
                "Estimate nutrition only if portions can be reasonably estimated; otherwise use null. " +
                "Do not diagnose personal migraine triggers or claim allergens are absent. Explain portion uncertainty. " +
                "Reply only with JSON: foodLabel, calories, proteinG, carbsG, fatG, notes, migraineTriggerHint."
            when (val response = imageProvider.completeWithImage(prompt, image.base64, image.mimeType)) {
                is Result.Success -> aiClient.parseNutrition(response.data, description).let {
                    it.copy(notes = it.notes + " Photo-based AI estimate; portions and ingredients may be inaccurate.")
                }
                is Result.Error -> NutritionScanResult(
                    foodLabel = description.ifBlank { "Photo meal" }, caloriesEstimate = null,
                    notes = "Photo analysis unavailable (${response.message}). Saved locally without nutrition estimates.",
                )
            }
        } else aiClient.analyzeNutrition(description, hasImage = imageUri != null)
        return base.copy(imageUri = imageUri)
    }

    override fun observeRecent(limit: Int): Flow<List<NutritionScanRecord>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord> =
        dao.inRange(startMs, endMs).map { it.toDomain() }

    override suspend fun persist(result: NutritionScanResult): Long =
        dao.insert(NutritionScanEntity.from(result, System.currentTimeMillis()))
}
