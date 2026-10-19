package com.migraineguard.app.feature.fitness
import org.junit.Assert.*
import org.junit.Test

class StepReadingPolicyTest {
    private val samsung = "com.sec.android.app.shealth"
    @Test fun samsungSelectionDoesNotHideWatchCountBehindZeroCombined() {
        assertEquals(180, chooseStepReading(0, 180, emptyMap(), true).total)
        assertEquals(12, chooseStepReading(12, 180, emptyMap(), false).total)
    }
    @Test fun rawStoredRecordsRecoverZeroAggregatesWithoutMixingWriters() {
        val choice = chooseStepReading(0, 0, mapOf(samsung to 180, "other" to 30), true)
        assertEquals(180, choice.total); assertTrue(choice.estimated)
    }
    @Test fun overlappingDailyBucketsAreNotAddedTogether() {
        val records = listOf(StepInterval(0, 1000, 100, samsung), StepInterval(0, 1000, 180, samsung))
        assertEquals(180, conservativeSteps(records, 0, 1000))
        val dailyAndHourly = listOf(StepInterval(0, 24000, 180, samsung),
            StepInterval(0, 1000, 100, samsung), StepInterval(1000, 2000, 50, samsung))
        assertEquals(180, conservativeSteps(dailyAndHourly, 0, 24000))
        assertEquals(180, conservativeSteps(listOf(StepInterval(0, 1000, 100, samsung),
            StepInterval(500, 1500, 180, samsung)), 0, 1500))
    }
    @Test fun disjointIntervalsAddAndCrossDayIntervalsAreClipped() {
        val records = listOf(StepInterval(-1000, 1000, 100, samsung), StepInterval(1000, 2000, 80, samsung))
        assertEquals(130, conservativeSteps(records, 0, 2000))
    }
    @Test fun noRecordsRemainUnknownAndActualZeroRemainsZero() {
        assertNull(conservativeSteps(emptyList(), 0, 1000))
        assertNull(chooseStepReading(null, null, emptyMap(), true).total)
        assertEquals(0, chooseStepReading(0, null, emptyMap(), true).total)
    }
    @Test(expected = IllegalArgumentException::class) fun differentRawWritersCannotBeSummed() {
        conservativeSteps(listOf(StepInterval(0, 1000, 100, samsung), StepInterval(0, 1000, 100, "other")), 0, 1000)
    }
}
