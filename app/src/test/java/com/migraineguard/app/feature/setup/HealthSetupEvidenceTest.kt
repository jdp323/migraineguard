package com.migraineguard.app.feature.setup

import com.migraineguard.app.core.domain.*
import org.junit.Assert.*
import org.junit.Test

class HealthSetupEvidenceTest {
    private val samsung = "com.sec.android.app.shealth"
    @Test fun permissionsAloneDoNotProveRecordsOrSamsungSharing() {
        val e = HealthSetupEvidence(available = true, allPermissions = true)
        assertFalse(e.stepsVerified); assertFalse(e.heartVerified); assertFalse(e.sleepVerified); assertFalse(e.samsungVerified)
    }
    @Test fun demoAndManualNumbersDoNotProveHealthConnect() {
        val e = HealthSetupEvidence(true, true, HealthSnapshot(6000, 72, 20, "Demo"), SleepSnapshot(7f, "Demo", "Demo"))
        assertFalse(e.stepsVerified); assertFalse(e.heartVerified); assertFalse(e.sleepVerified); assertFalse(e.samsungVerified)
    }
    @Test fun actualZeroIsVerifiedWithoutInventingSamsungSharing() {
        val e = HealthSetupEvidence(true, true, HealthSnapshot(0, null, 0, stepSync = StepSyncSnapshot(
            0, 0, listOf("other.writer"), 1000, "read")))
        assertTrue(e.stepsVerified); assertFalse(e.samsungVerified)
    }
    @Test fun datedSamsungHeartOrSleepCanProveSharingWithoutSteps() {
        val h = HealthSnapshot(0, 65, 0, heartRateRecordedAtMillis = 1000, heartRateSource = samsung)
        assertTrue(HealthSetupEvidence(true, false, h).heartVerified)
        assertTrue(HealthSetupEvidence(true, false, h).samsungVerified)
        val s = SleepSnapshot(7f, "recorded", samsung, 1000, false)
        assertTrue(HealthSetupEvidence(true, false, sleep = s).sleepVerified)
        assertTrue(HealthSetupEvidence(true, false, sleep = s).samsungVerified)
    }
    @Test fun unavailableServiceCannotVerifyStaleReadings() {
        val e = HealthSetupEvidence(false, false, HealthSnapshot(1, 65, 1, heartRateRecordedAtMillis = 1000,
            heartRateSource = samsung, stepSync = StepSyncSnapshot(1, 1, listOf(samsung), 1000, "old")),
            SleepSnapshot(7f, "old", samsung, 1000))
        assertFalse(e.stepsVerified); assertFalse(e.heartVerified); assertFalse(e.sleepVerified); assertFalse(e.samsungVerified)
    }
}
