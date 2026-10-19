package com.migraineguard.app.core.time

import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDatesTest {
    @Test fun midnightAndClockChangesEmitWithoutDatabaseUpdates() = runTest {
        var date = LocalDate.parse("2026-10-18")
        val seen = mutableListOf<LocalDate>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            localDates({ date }, 1000).collect { seen += it }
        }
        advanceTimeBy(1000); runCurrent()
        assertEquals(listOf(date), seen)
        date = date.plusDays(1)
        advanceTimeBy(1000); runCurrent()
        assertEquals(listOf(date.minusDays(1), date), seen)
        date = date.minusDays(1)
        advanceTimeBy(1000); runCurrent()
        assertEquals(date, seen.last())
        assertEquals(3, seen.size)
    }
}
