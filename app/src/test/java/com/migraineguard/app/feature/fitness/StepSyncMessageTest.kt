package com.migraineguard.app.feature.fitness

import org.junit.Assert.assertTrue
import org.junit.Test

class StepSyncMessageTest {
    @Test fun emptyReadExplainsSharingWithoutClaimingPermissionFailure() {
        assertTrue(stepSyncMessage(null, null).contains("returned no steps"))
    }
    @Test fun nonSamsungDataDoesNotRequireSamsungSharing() {
        assertTrue(stepSyncMessage(12, null).contains("No Samsung count is required"))
        assertTrue(stepSyncMessage(12, 0).contains("source app"))
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
