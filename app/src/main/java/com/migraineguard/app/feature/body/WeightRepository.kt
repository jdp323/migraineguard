package com.migraineguard.app.feature.body

import com.migraineguard.app.core.data.db.WeightDao
import com.migraineguard.app.core.data.db.WeightEntryEntity
import com.migraineguard.app.core.domain.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(private val dao: WeightDao) {
    fun observeEntries(): Flow<List<WeightEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun save(entry: WeightEntry): Long =
        dao.insert(WeightEntryEntity.fromDomain(entry.copy(id = 0)))

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun latest(): WeightEntry? = dao.latest()?.toDomain()
}
