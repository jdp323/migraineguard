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

object HealthConnectPackages {
    const val GOOGLE = "com.google.android.apps.healthdata"
    const val SAMSUNG_HEALTH = "com.sec.android.app.shealth"
    const val ACTION_HC_SETTINGS = "androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"
    const val ACTION_MANAGE_PERMISSIONS = "androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS"
    const val ACTION_MANAGE_PERMISSIONS_FW = "android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"
}

data class HealthConnectDiagnostics(
    val sdkStatusRaw: Int,
    val sdkStatusName: String,
    val providerPackage: String,
    val lastError: String,
    val googleHcInstalled: Boolean,
    val samsungHealthInstalled: Boolean,
)

interface HealthDataRepository {
    suspend fun latestFitness(): HealthSnapshot
    suspend fun latestSleep(): SleepSnapshot
    suspend fun availabilityStatus(): HealthConnectStatus
    fun requiredPermissions(): Set<String>
    fun openHealthConnectSettingsIntent(): Intent
    fun openHealthConnectInstallOrSettingsIntents(): List<Intent>
    fun openSamsungHealthSettingsIntent(): Intent?
    suspend fun hasAllPermissions(): Boolean
    fun sdkStatusRaw(uiContext: Context? = null): Int
    fun sdkStatusName(raw: Int): String
    fun providerPackage(): String
    fun lastError(): String
    fun recordError(message: String)
    fun isPackagePresent(packageName: String): Boolean
    fun playStoreUpdateIntents(): List<Intent>
    fun manageHealthPermissionsIntents(appPackage: String): List<Intent>
    fun diagnostics(uiContext: Context? = null): HealthConnectDiagnostics
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

    @Volatile
    private var lastErrorText: String = ""

    override fun providerPackage(): String = HealthConnectPackages.GOOGLE

    override fun lastError(): String = lastErrorText

    override fun recordError(message: String) {
        lastErrorText = message
    }

    override fun sdkStatusName(raw: Int): String = when (raw) {
        HealthConnectClient.SDK_AVAILABLE -> "SDK_AVAILABLE"
        HealthConnectClient.SDK_UNAVAILABLE -> "SDK_UNAVAILABLE"
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
            "SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED"
        else -> "UNKNOWN($raw)"
    }

    override fun isPackagePresent(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun sdkStatusRaw(uiContext: Context?): Int {
        val ctx = uiContext ?: context
        return runCatching {
            HealthConnectClient.getSdkStatus(ctx, providerPackage())
        }.getOrElse { first ->
            recordError("getSdkStatus(provider): ${first.message ?: first}")
            runCatching { HealthConnectClient.getSdkStatus(ctx) }.getOrElse { second ->
                recordError("getSdkStatus: ${second.message ?: second}")
                -1
            }
        }
    }

    override fun diagnostics(uiContext: Context?): HealthConnectDiagnostics {
        val raw = sdkStatusRaw(uiContext)
        return HealthConnectDiagnostics(
            sdkStatusRaw = raw,
            sdkStatusName = sdkStatusName(raw),
            providerPackage = providerPackage(),
            lastError = lastErrorText,
            googleHcInstalled = isPackagePresent(HealthConnectPackages.GOOGLE),
            samsungHealthInstalled = isPackagePresent(HealthConnectPackages.SAMSUNG_HEALTH),
        )
    }

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
        return when (sdkStatusRaw()) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectStatus.NOT_INSTALLED
            HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectStatus.UNSUPPORTED
            else -> HealthConnectStatus.UNSUPPORTED
        }
    }

    override suspend fun hasAllPermissions(): Boolean {
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE) return false
        return runCatching {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            requiredPermissions().all { it in granted }
        }.getOrElse {
            recordError("hasAllPermissions: ${it.message ?: it}")
            false
        }
    }

    override fun openHealthConnectSettingsIntent(): Intent =
        openHealthConnectInstallOrSettingsIntents().first()

    override fun manageHealthPermissionsIntents(appPackage: String): List<Intent> {
        val candidates = buildList {
            runCatching { add(HealthConnectClient.getHealthConnectManageDataIntent(context)) }
            add(
                Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, appPackage)
                },
            )
            add(
                Intent(HealthConnectPackages.ACTION_HC_SETTINGS).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, appPackage)
                },
            )
            add(
                Intent(HealthConnectPackages.ACTION_MANAGE_PERMISSIONS).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, appPackage)
                },
            )
            add(
                Intent(HealthConnectPackages.ACTION_MANAGE_PERMISSIONS_FW).apply {
                    putExtra(Intent.EXTRA_PACKAGE_NAME, appPackage)
                },
            )
        }
        return distinctResolvable(candidates)
    }

    override fun playStoreUpdateIntents(): List<Intent> {
        val hc = HealthConnectPackages.GOOGLE
        val candidates = buildList {
            add(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(
                        "market://details?id=$hc&url=healthconnect%3A%2F%2Fonboarding",
                    )
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                },
            )
            add(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$hc")))
            add(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$hc"),
                ),
            )
            if (isPackagePresent(HealthConnectPackages.SAMSUNG_HEALTH)) {
                add(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${HealthConnectPackages.SAMSUNG_HEALTH}")),
                )
            }
        }
        return distinctResolvable(candidates).ifEmpty { listOf(candidates[1]) }
    }

    override fun openHealthConnectInstallOrSettingsIntents(): List<Intent> {
        val candidates = buildList {
            addAll(manageHealthPermissionsIntents(context.packageName))
            add(Intent(Intent.ACTION_VIEW).setPackage(HealthConnectPackages.GOOGLE))
            add(Intent(Intent.ACTION_MAIN).setPackage(HealthConnectPackages.GOOGLE))
            addAll(playStoreUpdateIntents())
        }
        return distinctResolvable(candidates).ifEmpty { playStoreUpdateIntents() }
    }

    override fun openSamsungHealthSettingsIntent(): Intent? {
        if (!isPackagePresent(HealthConnectPackages.SAMSUNG_HEALTH)) return null
        return context.packageManager.getLaunchIntentForPackage(HealthConnectPackages.SAMSUNG_HEALTH)
    }

    private fun distinctResolvable(candidates: List<Intent>): List<Intent> {
        val pm = context.packageManager
        val seen = LinkedHashSet<String>()
        val out = mutableListOf<Intent>()
        for (intent in candidates) {
            val key = "${intent.action}|${intent.`package`}|${intent.data}"
            if (!seen.add(key)) continue
            if (intent.resolveActivity(pm) != null) out += intent
        }
        return out.ifEmpty { candidates.take(1) }
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
            recordError("latestFitness: ${it.message ?: it}")
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
            recordError("latestSleep: ${it.message ?: it}")
            fake.latestSleep().copy(sourceLabel = "Demo (read failed)")
        }
    }
}

class FakeHealthDataRepository : HealthDataRepository {
    @Volatile
    private var lastErrorText: String = ""

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
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${HealthConnectPackages.GOOGLE}"))

    override fun openHealthConnectInstallOrSettingsIntents(): List<Intent> =
        listOf(openHealthConnectSettingsIntent())

    override fun openSamsungHealthSettingsIntent(): Intent? = null

    override suspend fun hasAllPermissions(): Boolean = false

    override fun sdkStatusRaw(uiContext: Context?): Int = -1

    override fun sdkStatusName(raw: Int): String = "USING_FALLBACK"

    override fun providerPackage(): String = HealthConnectPackages.GOOGLE

    override fun lastError(): String = lastErrorText

    override fun recordError(message: String) {
        lastErrorText = message
    }

    override fun isPackagePresent(packageName: String): Boolean = false

    override fun playStoreUpdateIntents(): List<Intent> = listOf(openHealthConnectSettingsIntent())

    override fun manageHealthPermissionsIntents(appPackage: String): List<Intent> =
        listOf(openHealthConnectSettingsIntent())

    override fun diagnostics(uiContext: Context?): HealthConnectDiagnostics =
        HealthConnectDiagnostics(
            sdkStatusRaw = -1,
            sdkStatusName = "USING_FALLBACK",
            providerPackage = providerPackage(),
            lastError = lastErrorText,
            googleHcInstalled = false,
            samsungHealthInstalled = false,
        )
}

fun Context.findActivity(): android.app.Activity? {
    var ctx: Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun Context.startHealthActivity(intent: Intent) {
    val toLaunch = Intent(intent)
    if (findActivity() == null) {
        toLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(toLaunch)
}
