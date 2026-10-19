package com.migraineguard.app.feature.meds

import com.migraineguard.app.core.data.db.MedicationDao
import com.migraineguard.app.core.data.db.MedicationEntity
import com.migraineguard.app.core.domain.MedicationEntry
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationDayTest {
    @Test fun takenStateResetsAtMidnightWithNoRowWrite() = runTest {
        val date = MutableStateFlow(LocalDate.parse("2026-10-18"))
        val dao = MemoryMedicationDao()
        val repository = MedicationRepository(dao, date)
        var seen = emptyList<MedicationEntry>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { repository.observeMedications().collect { seen = it } }
        runCurrent()
        assertTrue(seen.single().takenToday)
        date.value = date.value.plusDays(1)
        runCurrent()
        assertFalse(seen.single().takenToday)
        assertEquals("2026-10-18", dao.rows.value.single().takenDateIso)
        assertEquals(0, dao.writes)
    }
}

private class MemoryMedicationDao : MedicationDao {
    val rows = MutableStateFlow(listOf(MedicationEntity(id=1, name="Fixture", dose="Label", timeLabel="Morning", takenToday=true, takenDateIso="2026-10-18")))
    var writes = 0
    override fun observeAll() = rows
    override suspend fun insert(entity: MedicationEntity): Long { writes++; rows.value += entity; return entity.id }
    override suspend fun update(entity: MedicationEntity) { writes++; rows.value = rows.value.map { if(it.id==entity.id)entity else it } }
    override suspend fun delete(id: Long) { writes++; rows.value = rows.value.filterNot { it.id==id } }
}
