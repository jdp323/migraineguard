package com.migraineguard.app.feature.body

import com.migraineguard.app.core.data.db.BodyMeasurementDao
import com.migraineguard.app.core.data.db.BodyMeasurementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

class BodyMeasurementRepository(private val dao: BodyMeasurementDao) {
    fun observe() = dao.observe()
    suspend fun save(id: String? = null, date: String, valuesCm: Map<String, Float>, notes: String): BodyMeasurementEntity = withContext(Dispatchers.IO) {
        require(!LocalDate.parse(date).isAfter(LocalDate.now())) { "Choose today or an earlier date." }
        val old = id?.let { requireNotNull(dao.get(it)) { "This entry is no longer available." } }
        require(old?.deleted != true) { "This entry was deleted. Create a new log instead." }
        BodyMeasurementEntity(id ?: UUID.randomUUID().toString(), date, valuesCm.toSortedMap(), notes.trim(),
            maxOf(System.currentTimeMillis(), (old?.modifiedAtMillis ?: -1) + 1)).also { it.validate(); dao.put(it) }
    }
    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        val old = dao.get(id) ?: return@withContext
        dao.put(old.copy(measurementsCm = emptyMap(), notes = "", deleted = true,
            modifiedAtMillis = maxOf(System.currentTimeMillis(), old.modifiedAtMillis + 1)))
    }
}
