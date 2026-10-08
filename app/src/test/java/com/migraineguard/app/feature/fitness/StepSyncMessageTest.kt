package com.migraineguard.app.feature.fitness

import org.junit.Assert.assertTrue
import org.junit.Test

class StepSyncMessageTest {
    @Test fun failedReadDoesNotClaimZeroSteps() {
        assertTrue(stepSyncMessage(null, null).contains("could not be read"))
    }
    @Test fun missingSamsungDataGivesSharingAndSyncSteps() {
        assertTrue(stepSyncMessage(12, null).contains("Allow Samsung Health to write Steps"))
        assertTrue(stepSyncMessage(12, 0).contains("Sync now"))
    }
    @Test fun higherSamsungCountExplainsPriorityWithoutAddingCounts() {
        val message = stepSyncMessage(12, 180)
        assertTrue(message.contains("source priority"))
        assertTrue(message.contains("not added together"))
    }
    @Test fun failedSamsungCheckIsNotReportedAsNoSamsungData() {
        assertTrue(stepSyncMessage(12, null, true).contains("could not be checked"))
    }
    @Test fun matchingCountsExplainArrivalDelay() {
        assertTrue(stepSyncMessage(180, 180).contains("updates can arrive later"))
    }
}
