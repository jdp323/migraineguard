package com.migraineguard.app.feature.fitness

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
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
    fun openHealthConnectInstallOrSettingsIntents(): List<Intent>
    fun openSamsungHealthSettingsIntent(): Intent?
    suspend fun hasAllPermissions(): Boolean
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

    override fun requiredPermissions(): Set<String> {
        val base = linkedSetOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        )
        // Rebuild never-empty set (defensive against SDK quirks)
        return if (base.isEmpty()) {
            setOf(
                "android.permission.health.READ_STEPS",
                "android.permission.health.READ_SLEEP",
                "android.permission.health.READ_HEART_RATE",
                "android.permission.health.READ_EXERCISE",
                "android.permission.health.READ_DISTANCE",
                "android.permission.health.READ_TOTAL_CALORIES_BURNED",
                "android.permission.health.WRITE_EXERCISE",
            )
        } else {
            base
        }
    }

    override suspend fun availabilityStatus(): HealthConnectStatus {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.NOT_INSTALLED
            else -> HealthConnectStatus.UNSUPPORTED
        }
    }

    override suspend fun hasAllPermissions(): Boolean {
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE) return false
        return runCatching {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            requiredPermissions().all { it in granted }
        }.getOrDefault(false)
    }

    override fun openHealthConnectSettingsIntent(): Intent =
        openHealthConnectInstallOrSettingsIntents().first()

    override fun openHealthConnectInstallOrSettingsIntents(): List<Intent> {
        val candidates = listOf(
            Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"),
            Intent(Intent.ACTION_VIEW).setPackage("com.google.android.apps.healthdata"),
            Intent(Intent.ACTION_MAIN).setPackage("com.google.android.apps.healthdata"),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata"),
            ),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"),
            ),
        )
        val pm = context.packageManager
        val resolvable = candidates.filter { intent ->
            intent.resolveActivity(pm) != null
        }
        return resolvable.ifEmpty { listOf(candidates.last()) }
    }

    override fun openSamsungHealthSettingsIntent(): Intent? {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("com.sec.android.app.shealth", 0)
            val launch = pm.getLaunchIntentForPackage("com.sec.android.app.shealth")
            launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launch
        } catch (_: PackageManager.NameNotFoundException) {
            null
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
            val filter = TimeRangeFilter.between(start, end)

            val steps = client.readRecords(
                ReadRecordsRequest(StepsRecord::class, filter),
            ).records.sumOf { it.count.toInt() }

            val samples = client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, filter),
            ).records.flatMap { it.samples }
            val bpm = samples.lastOrNull()?.beatsPerMinute?.toInt()

            val distance = client.readRecords(
                ReadRecordsRequest(DistanceRecord::class, filter),
            ).records.sumOf { it.distance.inMeters }

            val calories = client.readRecords(
                ReadRecordsRequest(TotalCaloriesBurnedRecord::class, filter),
            ).records.sumOf { it.energy.inKilocalories }

            val exerciseSessions = client.readRecords(
                ReadRecordsRequest(ExerciseSessionRecord::class, filter),
            ).records
            val activeFromExercise = exerciseSessions.sumOf {
                ChronoUnit.MINUTES.between(it.startTime, it.endTime).toInt().coerceAtLeast(0)
            }

            val demo = fake.latestFitness()
            HealthSnapshot(
                steps = if (steps > 0) steps else demo.steps,
                heartRateBpm = bpm ?: demo.heartRateBpm,
                activeMinutes = if (activeFromExercise > 0) activeFromExercise else (steps / 100).coerceAtMost(180),
                sourceLabel = "Health Connect",
                distanceMeters = distance.takeIf { it > 0 },
                caloriesBurned = calories.takeIf { it > 0 },
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
            val sessions = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    TimeRangeFilter.between(start, end),
                ),
            ).records
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
        distanceMeters = 4200.0,
        caloriesBurned = 280.0,
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

    override fun openHealthConnectInstallOrSettingsIntents(): List<Intent> =
        listOf(openHealthConnectSettingsIntent())

    override fun openSamsungHealthSettingsIntent(): Intent? = null

    override suspend fun hasAllPermissions(): Boolean = false
}
