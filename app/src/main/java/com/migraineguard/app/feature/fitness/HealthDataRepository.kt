package com.migraineguard.app.feature.fitness

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import java.time.Instant
import java.time.temporal.ChronoUnit

interface HealthDataRepository {
    suspend fun latestFitness(): HealthSnapshot
    suspend fun latestSleep(): SleepSnapshot
    suspend fun availabilityStatus(): HealthConnectStatus
    fun requiredPermissions(): Set<String>
    fun openHealthConnectSettingsIntent(): Intent
}

enum class HealthConnectStatus {
    AVAILABLE,
    NOT_INSTALLED,
    UNSUPPORTED,
    USING_FALLBACK,
}

class HealthConnectHealthDataRepository(
    private val context: Context,
) : HealthDataRepository {

    private val fake = FakeHealthDataRepository()

    override fun requiredPermissions(): Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )

    override suspend fun availabilityStatus(): HealthConnectStatus {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.NOT_INSTALLED
            else -> HealthConnectStatus.UNSUPPORTED
        }
    }

    override fun openHealthConnectSettingsIntent(): Intent {
        val settings = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
        return if (settings.resolveActivity(context.packageManager) != null) {
            settings
        } else {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata"),
            )
        }
    }

    override suspend fun latestFitness(): HealthSnapshot {
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE) {
            return fake.latestFitness().copy(sourceLabel = "Demo (Health Connect unavailable)")
        }
        return runCatching {
            val client = HealthConnectClient.getOrCreate(context)
            val end = Instant.now()
            val start = end.minus(1, ChronoUnit.DAYS)
            val stepsReq = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
            val steps = client.readRecords(stepsReq).records.sumOf { it.count.toInt() }
            val hrReq = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
            val samples = client.readRecords(hrReq).records.flatMap { it.samples }
            val bpm = samples.lastOrNull()?.beatsPerMinute?.toInt()
            HealthSnapshot(
                steps = if (steps > 0) steps else fake.latestFitness().steps,
                heartRateBpm = bpm ?: fake.latestFitness().heartRateBpm,
                activeMinutes = (steps / 100).coerceAtMost(180),
                sourceLabel = "Health Connect",
            )
        }.getOrElse {
            fake.latestFitness().copy(sourceLabel = "Demo (read failed)")
        }
    }

    override suspend fun latestSleep(): SleepSnapshot {
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE) {
            return fake.latestSleep().copy(sourceLabel = "Demo (Health Connect unavailable)")
        }
        return runCatching {
            val client = HealthConnectClient.getOrCreate(context)
            val end = Instant.now()
            val start = end.minus(2, ChronoUnit.DAYS)
            val req = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
            val sessions = client.readRecords(req).records
            val latest = sessions.maxByOrNull { it.startTime }
            if (latest == null) {
                return@runCatching fake.latestSleep().copy(sourceLabel = "Health Connect (no sessions)")
            }
            val hours = ChronoUnit.MINUTES.between(latest.startTime, latest.endTime) / 60f
            val quality = when {
                hours >= 7.5f -> "Restorative"
                hours >= 6f -> "Fair"
                else -> "Short"
            }
            SleepSnapshot(hours = hours, qualityLabel = quality, sourceLabel = "Health Connect")
        }.getOrElse {
            fake.latestSleep().copy(sourceLabel = "Demo (read failed)")
        }
    }
}

class FakeHealthDataRepository : HealthDataRepository {
    override suspend fun latestFitness(): HealthSnapshot = HealthSnapshot(
        steps = 6420,
        heartRateBpm = 72,
        activeMinutes = 38,
        sourceLabel = "Demo",
    )

    override suspend fun latestSleep(): SleepSnapshot = SleepSnapshot(
        hours = 6.5f,
        qualityLabel = "Fair",
        sourceLabel = "Demo",
    )

    override suspend fun availabilityStatus(): HealthConnectStatus = HealthConnectStatus.USING_FALLBACK

    override fun requiredPermissions(): Set<String> = emptySet()

    override fun openHealthConnectSettingsIntent(): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
}
