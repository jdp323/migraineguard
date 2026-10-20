package com.migraineguard.app.feature.setup

import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot

/** Permission grants and real records are separate evidence. Demo/manual values cannot prove a connection. */
data class HealthSetupEvidence(val available: Boolean = false, val allPermissions: Boolean = false,
    val fitness: HealthSnapshot? = null, val sleep: SleepSnapshot? = null) {
    val stepsVerified get() = available && fitness?.stepSync?.total != null
    val heartVerified get() = available && fitness?.heartRateBpm != null && fitness.heartRateRecordedAtMillis != null
    val sleepVerified get() = available && sleep?.recordedAtMillis != null && sleep.hours.isFinite() && sleep.hours > 0f
    val samsungVerified: Boolean get() {
        if (!available) return false
        val origin = "com.sec.android.app.shealth"
        return fitness?.stepSync?.sourcePackages?.contains(origin) == true ||
            (fitness?.stepSync?.samsungTotal ?: 0) > 0 ||
            (heartVerified && fitness?.heartRateSource == origin) || (sleepVerified && sleep?.sourceLabel == origin)
    }
}
