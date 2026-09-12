package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot

/**
 * Abstraction over Health Connect / Wear / vendor SDKs.
 * Shared by Fitness and Sleep feature stubs.
 */
interface HealthDataRepository {
    suspend fun latestFitness(): HealthSnapshot
    suspend fun latestSleep(): SleepSnapshot
}

class FakeHealthDataRepository : HealthDataRepository {
    override suspend fun latestFitness(): HealthSnapshot = HealthSnapshot(
        steps = 6420,
        heartRateBpm = 72,
        activeMinutes = 38,
    )

    override suspend fun latestSleep(): SleepSnapshot = SleepSnapshot(
        hours = 6.5f,
        qualityLabel = "Fair (fake)",
    )
}
