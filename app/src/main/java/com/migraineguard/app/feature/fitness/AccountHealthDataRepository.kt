package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.domain.StepSyncSnapshot
import kotlinx.coroutines.flow.first

/** Health Connect is phone-wide, not Google-account-specific. Each account must opt in. */
class AccountHealthDataRepository(
    private val delegate: HealthDataRepository,
    private val preferences: UserPreferencesRepository,
) : HealthDataRepository by delegate {
    private suspend fun enabled() = preferences.preferences.first().deviceHealthEnabled
    override suspend fun allowDeviceHealth() { preferences.setDeviceHealthEnabled(true) }
    override suspend fun hasAllPermissions() = enabled() && delegate.hasAllPermissions()
    override suspend fun latestFitness(): HealthSnapshot = if (enabled()) delegate.latestFitness() else {
        val message = "Connect this phone's health data for this account in Fitness."
        HealthSnapshot(0, null, 0, message,
            stepSync = StepSyncSnapshot(null, null, emptyList(), System.currentTimeMillis(), message),
            heartRateStatus = message)
    }
    override suspend fun latestSleep(): SleepSnapshot = if (enabled()) delegate.latestSleep() else
        SleepSnapshot(0f, "—", "Connect this phone's health data for this account in Fitness.")
}
