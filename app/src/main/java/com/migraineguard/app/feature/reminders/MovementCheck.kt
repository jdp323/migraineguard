package com.migraineguard.app.feature.reminders

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CancellationException
import java.time.Instant

/** A recent-step proxy, never a posture or sitting detector. Null means unavailable, not zero. */
object MovementPolicy {
    fun shouldRemind(steps: Long?): Boolean = steps == null || steps < 20
    fun message(steps: Long?, minutes: Int): String = if (steps == null)
        "Time for a movement break. Stand, stretch or take a short walk if it suits you."
    else "Fewer than 20 steps were shared in the last $minutes minutes. Consider standing, stretching or a short walk. Device sync can be delayed."
}

object MovementCheck {
    const val BACKGROUND_PERMISSION = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"
    fun supported(context: Context): Boolean = runCatching {
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE &&
            HealthConnectClient.getOrCreate(context).features.getFeatureStatus(
                HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
    }.getOrDefault(false)
    suspend fun allowed(context: Context): Boolean = supported(context) &&
        HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions().containsAll(
            setOf(BACKGROUND_PERMISSION, HealthPermission.getReadPermission(StepsRecord::class)))
    suspend fun recentSteps(context: Context, minutes: Int): Long? {
        return try {
            if (!allowed(context)) return null
            val end = Instant.now()
            HealthConnectClient.getOrCreate(context).aggregate(AggregateRequest(
                setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(end.minusSeconds(minutes * 60L), end)
            ))[StepsRecord.COUNT_TOTAL]
        } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
    }
}
