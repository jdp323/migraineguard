package com.migraineguard.app.feature.meds

import com.migraineguard.app.core.data.db.MedicationDao
import com.migraineguard.app.core.data.db.MedicationEntity
import com.migraineguard.app.core.domain.MedicationEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.migraineguard.app.core.time.localDates
import java.time.LocalDate

class MedicationRepository(
    private val dao: MedicationDao,
    private val dates: Flow<LocalDate> = localDates(),
) {
    private fun todayIso(): String = LocalDate.now().toString()

    fun observeMedications(): Flow<List<MedicationEntry>> =
        combine(dao.observeAll(), dates) { list, date ->
            list.map { it.toDomain(date.toString()) }
        }

    suspend fun add(name: String, dose: String, timeLabel: String): Long =
        dao.insert(
            MedicationEntity(
                name = name.trim(),
                dose = dose.trim(),
                timeLabel = timeLabel.trim(),
            ),
        )

    suspend fun setTakenToday(entry: MedicationEntry, taken: Boolean) {
        val today = todayIso()
        dao.update(
            MedicationEntity(
                id = entry.id,
                name = entry.name,
                dose = entry.dose,
                timeLabel = entry.timeLabel,
                takenToday = taken,
                lastTakenMillis = if (taken) System.currentTimeMillis() else entry.lastTakenMillis,
                takenDateIso = if (taken) today else "",
            ),
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
