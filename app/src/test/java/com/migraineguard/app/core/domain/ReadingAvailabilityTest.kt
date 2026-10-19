package com.migraineguard.app.core.domain

import org.junit.Assert.*
import org.junit.Test

class ReadingAvailabilityTest {
    @Test fun missingHealthReadDoesNotBecomeMeasuredZero() {
        val snapshot = HealthSnapshot(0, null, 0, stepSync=StepSyncSnapshot(null,null,emptyList(),0,"Unavailable"))
        assertNull(snapshot.observedSteps)
        assertEquals(0, snapshot.steps) // backwards-compatible raw field is not presentation truth
    }
    @Test fun actualZeroAndManualReadingsRemainAvailable() {
        assertEquals(0, HealthSnapshot(0,null,0,stepSync=StepSyncSnapshot(0,null,emptyList(),0,"Read")).observedSteps)
        assertEquals(25, HealthSnapshot(25,null,0,sourceLabel="Manual").observedSteps)
    }
    @Test fun absentOrInvalidSleepCannotTriggerShortSleepHints() {
        for (hours in listOf(0f,-1f,Float.NaN,Float.POSITIVE_INFINITY)) assertNull(SleepSnapshot(hours,"Unknown").recordedHours)
        assertEquals(7.5f, SleepSnapshot(7.5f,"Recorded").recordedHours!!, 0f)
    }
    @Test fun emptyInsightReportKeepsHealthSnapshotsUnknown() {
        val report=InsightReport("Fixture","","Today")
        assertNull(report.totalSteps)
        assertNull(report.avgSleepHours)
    }
}
