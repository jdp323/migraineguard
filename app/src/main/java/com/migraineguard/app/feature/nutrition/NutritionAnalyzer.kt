package com.migraineguard.app.feature.nutrition

import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.data.db.NutritionDao
import com.migraineguard.app.core.data.db.NutritionScanEntity
import com.migraineguard.app.core.domain.NutritionScanRecord
import com.migraineguard.app.core.domain.NutritionScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NutritionAnalyzer {
    suspend fun analyzeFoodLabel(description: String, imageUri: String? = null): NutritionScanResult
    fun observeRecent(limit: Int = 30): Flow<List<NutritionScanRecord>>
    suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord>
    suspend fun persist(result: NutritionScanResult): Long
}

class AiNutritionAnalyzer(
    private val aiClient: FallbackAiClient,
    private val dao: NutritionDao,
) : NutritionAnalyzer {
    override suspend fun analyzeFoodLabel(description: String, imageUri: String?): NutritionScanResult {
        val base = aiClient.analyzeNutrition(description, hasImage = imageUri != null)
        return base.copy(imageUri = imageUri)
    }

    override fun observeRecent(limit: Int): Flow<List<NutritionScanRecord>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord> =
        dao.inRange(startMs, endMs).map { it.toDomain() }

    override suspend fun persist(result: NutritionScanResult): Long =
        dao.insert(NutritionScanEntity.from(result, System.currentTimeMillis()))
}
