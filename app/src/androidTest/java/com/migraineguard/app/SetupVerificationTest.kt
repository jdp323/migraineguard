package com.migraineguard.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.feature.fitness.*
import com.migraineguard.app.feature.setup.HealthConnectStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupVerificationTest {
    @get:Rule val compose = createComposeRule()
    private class Reads : HealthDataRepository by FakeHealthDataRepository() {
        var records = false
        var fail = false
        override suspend fun availabilityStatus() = HealthConnectStatus.AVAILABLE
        override suspend fun hasAllPermissions() = !fail
        override suspend fun latestFitness(): HealthSnapshot {
            if (fail) throw SecurityException("permission revoked")
            return HealthSnapshot(if (records) 180 else 0, if (records) 65 else null, 0,
                stepSync = StepSyncSnapshot(if (records) 180 else null, if (records) 180 else null,
                    if (records) listOf("com.sec.android.app.shealth") else emptyList(), 1000, "test"),
                heartRateRecordedAtMillis = if (records) 1000 else null,
                heartRateSource = if (records) "com.sec.android.app.shealth" else null)
        }
        override suspend fun latestSleep() = if (records) SleepSnapshot(7f, "recorded", "com.sec.android.app.shealth", 1000)
            else SleepSnapshot(0f, "No data")
    }
    private fun show(repo: Reads) { compose.setContent { MigraineGuardTheme { Surface {
        HealthConnectStep(repo, remember { SnackbarHostState() }, {}, {})
    } } } }
    private fun checked() { compose.waitUntil(10_000) {
        compose.onAllNodesWithText("Connection check completed").fetchSemanticsNodes().isNotEmpty()
    } }
    private fun verified(title: String, expected: Boolean) {
        val match = hasAnyDescendant(hasContentDescription("Verified"))
        compose.onNodeWithTag("verification-" + title).assert(if (expected) match else match.not())
    }
    @Test fun permissionsWithoutRecordsDoNotShowDataCheckmarks() {
        show(Reads()); checked()
        verified("Requested permissions", true)
        for (title in listOf("Steps received", "Heart rate received", "Sleep received")) verified(title, false)
    }
    @Test fun realRecordsCheckAndRevokedAccessClearsTheirChecks() {
        val repo = Reads().apply { records = true }
        show(repo); checked()
        for (title in listOf("Steps received", "Heart rate received", "Sleep received")) verified(title, true)
        repo.fail = true
        compose.onNodeWithText("Check connection & readings").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("The connection could not be checked. Retry when Health Connect is available.").fetchSemanticsNodes().isNotEmpty() }
        for (title in listOf("Requested permissions", "Steps received", "Heart rate received", "Sleep received")) verified(title, false)
    }
}
