package com.migraineguard.app.feature.backup

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class BackupMergeTest {
    private data class Record(val id: Long, val notes: String)

    @Test fun collidingSourceIdsKeepBothDistinctRecords() = runTest {
        val inserted = mutableListOf<Record>()
        val count = merge(listOf(Record(1, "new journal")), listOf(Record(1, "existing journal")),
            { it.copy(id = 0) }) { inserted.add(it) }
        assertEquals(1, count)
        assertEquals(listOf(Record(0, "new journal")), inserted)
    }

    @Test fun repeatedRecordsAreNotDuplicated() = runTest {
        val inserted = mutableListOf<Record>()
        val count = merge(listOf(Record(9, "same"), Record(10, "same"), Record(11, "new")),
            listOf(Record(1, "same")), { it.copy(id = 0) }) { inserted.add(it) }
        assertEquals(1, count)
        assertEquals("new", inserted.single().notes)
    }
}
