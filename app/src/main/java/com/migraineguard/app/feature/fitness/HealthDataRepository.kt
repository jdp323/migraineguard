package com.migraineguard.app.feature.fitness

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import com.migraineguard.app.core.domain.StepSyncSnapshot
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.time.healthDayEnd
import com.migraineguard.app.core.time.healthDayStart
import kotlinx.coroutines.CancellationException
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
    suspend fun allowDeviceHealth() {}
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
    private val testClient: HealthConnectClient? = null,
    private val testSdkStatus: (() -> Int)? = null,
    private val now: () -> Instant = Instant::now,
    private val readPageSize: Int = 1000,
    private val stepSource: suspend () -> String = { "samsung" },
) : HealthDataRepository {

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
        testSdkStatus?.let { return it() }
        val ctx = uiContext ?: context
        return healthRead {
            HealthConnectClient.getSdkStatus(ctx, providerPackage())
        }.getOrElse { first ->
            recordError("getSdkStatus(provider): ${first.message ?: first}")
            healthRead { HealthConnectClient.getSdkStatus(ctx) }.getOrElse { second ->
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
        return healthRead {
            val client = testClient ?: HealthConnectClient.getOrCreate(context)
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
            healthRead { add(HealthConnectClient.getHealthConnectManageDataIntent(context)) }
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

    private fun sleepReadPermission(): String =
        HealthPermission.getReadPermission(SleepSessionRecord::class)

    private fun stepsReadPermission(): String =
        HealthPermission.getReadPermission(StepsRecord::class)

    /** Read every page: a first page can contain old or empty sessions. */
    private suspend inline fun <reified T : Record> readAll(
        client: HealthConnectClient, start: Instant, end: Instant,
    ): List<T> {
        val records = mutableListOf<T>()
        var token: String? = null
        var pages = 0
        do {
            check(++pages <= 100) { "Health Connect returned too many pages; try refreshing later" }
            val page = client.readRecords(ReadRecordsRequest(T::class,
                TimeRangeFilter.between(start, end.plusMillis(1)), ascendingOrder = false,
                pageSize = readPageSize, pageToken = token))
            records += page.records
            token = page.pageToken?.takeIf { it.isNotBlank() }
        } while (token != null)
        return records
    }

    private fun readFailure(label: String, error: Throwable): String =
        if (error is SecurityException) label + " read permission is missing or was revoked. Open Health Connect permissions."
        else label + " read failed (" + error.javaClass.simpleName + "). Retry; if it persists, update Health Connect and Samsung Health."

    override suspend fun latestFitness(): HealthSnapshot {
        lastErrorText = ""
        val end = now()
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE) return HealthSnapshot(
            0, null, 0, "Health Connect unavailable",
            stepSync = StepSyncSnapshot(null, null, emptyList(), end.toEpochMilli(), "Health Connect unavailable"))
        return try {
            val client = testClient ?: HealthConnectClient.getOrCreate(context)
            // A permission-query failure is not an empty grant set.
            val granted = client.permissionController.getGrantedPermissions()
            val start = healthDayStart(end)
            val dayFilter = TimeRangeFilter.between(start, healthDayEnd(end))
            val hasSteps = stepsReadPermission() in granted
            val hasHeart = HealthPermission.getReadPermission(HeartRateRecord::class) in granted
            val summary = listOf("Steps" to hasSteps, "Heart rate" to hasHeart,
                "Sleep" to (sleepReadPermission() in granted))
                .joinToString(" · ") { (name, allowed) -> name + ": " + if (allowed) "allowed" else "not allowed" }
            val stepRead = if (hasSteps) healthRead {
                client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), dayFilter))
            } else kotlin.Result.failure(SecurityException("Steps not granted"))
            val combined = stepRead.getOrNull()?.get(StepsRecord.COUNT_TOTAL)
                ?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt()
            // Query Samsung independently, even if the combined aggregate is empty or fails.
            val samsungRead = if (hasSteps) healthRead {
                client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), dayFilter,
                    setOf(DataOrigin(HealthConnectPackages.SAMSUNG_HEALTH))))[StepsRecord.COUNT_TOTAL]
                    ?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt()
            } else kotlin.Result.success(null)
            val samsung = samsungRead.getOrNull()
            val preferSamsung = stepSource() == "samsung"
            val needsRaw = hasSteps && ((combined ?: 0) == 0 || (preferSamsung && (samsung ?: 0) == 0))
            val rawRead = if (needsRaw) healthRead { readAll<StepsRecord>(client, start.minus(1, ChronoUnit.DAYS), healthDayEnd(end)) }
                else kotlin.Result.success(emptyList())
            val rawRows = rawRead.getOrNull().orEmpty()
            val rawBySource = rawRows.groupBy { it.metadata.dataOrigin.packageName }
            val rawCounts = rawBySource.mapValues { (_, rows) -> conservativeSteps(rows.map {
                StepInterval(it.startTime.toEpochMilli(), it.endTime.toEpochMilli(), it.count, it.metadata.dataOrigin.packageName)
            }, start.toEpochMilli(), healthDayEnd(end).toEpochMilli()) }
            val choice = chooseStepReading(combined, samsung, rawCounts, preferSamsung)
            val total = choice.total
            val rawSummary = if (!needsRaw) "Raw cross-check not needed: aggregate is available."
                else if (rawRead.isFailure) readFailure("Raw steps", rawRead.exceptionOrNull()!!)
                else "Raw records today: " + rawRows.size + "; writers: " + rawBySource.map { (name, rows) -> name + " (" + rows.size + " records; conservative count " + rawCounts[name] + ")" }.joinToString()
            val stepMessage = when {
                !hasSteps -> "Steps read permission is not allowed for this installed app. Open Health Connect permissions."
                choice.estimated -> "The aggregate was empty or zero despite stored step records. Showing a conservative estimate from one writer; overlapping records are not added together."
                preferSamsung && (samsung ?: 0) > 0 -> "Showing Samsung Health's shared count. Combined Health Connect count: " + (combined?.toString() ?: "unavailable") + ". Counts are not added together."
                combined == null && samsung != null -> "Showing Samsung Health's shared count because the combined Health Connect total is unavailable. Counts are not added together."
                stepRead.isFailure -> readFailure("Steps", stepRead.exceptionOrNull()!!)
                else -> stepSyncMessage(total, samsung, samsungRead.isFailure)
            }
            if (stepRead.isFailure && total == null) recordError(stepMessage)
            val stepSync = StepSyncSnapshot(total, samsung,
                (stepRead.getOrNull()?.dataOrigins?.map { it.packageName }.orEmpty() + rawBySource.keys).distinct().sorted(),
                end.toEpochMilli(), stepMessage, rawSummary, choice.estimated, combined)

            val heartRead = if (hasHeart) healthRead {
                readAll<HeartRateRecord>(client, end.minus(7, ChronoUnit.DAYS), healthDayEnd(end))
                    .flatMap { record -> record.samples.map { it to record.metadata.dataOrigin.packageName } }
                    .filter { (sample, _) -> !sample.time.isAfter(end) && !sample.time.isBefore(end.minus(7, ChronoUnit.DAYS)) }
                    .maxByOrNull { (sample, _) -> sample.time }
            } else kotlin.Result.success(null)
            val heart = heartRead.getOrNull()
            val heartMessage = when {
                !hasHeart -> "Heart rate read permission is not allowed for this installed app."
                heartRead.isFailure -> readFailure("Heart rate", heartRead.exceptionOrNull()!!)
                heart == null -> "Permission allowed; no heart rate samples shared in the last 7 days. Allow Samsung Health to write Heart rate and sync the watch in Samsung Health."
                else -> "Latest recorded sample; not a live watch reading."
            }
            heartRead.exceptionOrNull()?.let { recordError(heartMessage) }
            val distance = if (HealthPermission.getReadPermission(DistanceRecord::class) in granted) healthRead {
                client.aggregate(AggregateRequest(setOf(DistanceRecord.DISTANCE_TOTAL), dayFilter))[DistanceRecord.DISTANCE_TOTAL]?.inMeters
            }.getOrNull() else null
            val calories = if (HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) in granted) healthRead {
                val combinedCalories = client.aggregate(AggregateRequest(setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL), dayFilter))
                val origins = combinedCalories.dataOrigins
                if (origins.size > 1) {
                    val samsungOrigin = origins.firstOrNull { it.packageName == HealthConnectPackages.SAMSUNG_HEALTH }
                    samsungOrigin?.let { origin -> client.aggregate(AggregateRequest(
                        setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL), dayFilter, setOf(origin)
                    ))[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.let { it to "Samsung Health" } }
                } else combinedCalories[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.let {
                    it to origins.firstOrNull()?.packageName.orEmpty()
                }
            }.getOrNull() else null
            val active = if (HealthPermission.getReadPermission(ExerciseSessionRecord::class) in granted) healthRead {
                client.aggregate(AggregateRequest(setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL), dayFilter))[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]
                    ?.toMinutes()?.coerceIn(0L, 1440L)?.toInt() ?: 0
            }.getOrDefault(0) else 0
            HealthSnapshot(total ?: 0, heart?.first?.beatsPerMinute?.toInt(), active,
                sourceLabel = choice.source + if (choice.source.startsWith("Samsung")) " via Health Connect" else "",
                distanceMeters = distance, caloriesBurned = calories?.first, caloriesSourceLabel = calories?.second,
                stepSync = stepSync, heartRateRecordedAtMillis = heart?.first?.time?.toEpochMilli(),
                heartRateSource = heart?.second, heartRateStatus = heartMessage, permissionSummary = summary)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            val message = readFailure("Health Connect", e)
            recordError(message)
            HealthSnapshot(0, null, 0, message,
                stepSync = StepSyncSnapshot(null, null, emptyList(), end.toEpochMilli(), message),
                heartRateStatus = message, permissionSummary = "Could not check permissions; refresh.")
        }
    }

    override suspend fun latestSleep(): SleepSnapshot {
        if (availabilityStatus() != HealthConnectStatus.AVAILABLE)
            return SleepSnapshot(0f, "—", "Health Connect unavailable")
        return try {
            val client = testClient ?: HealthConnectClient.getOrCreate(context)
            if (sleepReadPermission() !in client.permissionController.getGrantedPermissions())
                return SleepSnapshot(0f, "—", "Sleep read permission is not allowed for this installed app.")
            val end = now()
            val latest = readAll<SleepSessionRecord>(client, end.minus(14, ChronoUnit.DAYS), healthDayEnd(end))
                .filter { !it.endTime.isAfter(end) && it.endTime.isAfter(it.startTime) }
                .maxByOrNull { it.endTime }
                ?: return SleepSnapshot(0f, "No data",
                    "Permission allowed; no sleep sessions shared in the last 14 days. Allow Samsung Health to write Sleep, open Samsung Health and sync the watch.")
            val recent = !latest.endTime.isBefore(end.minus(36, ChronoUnit.HOURS))
            SleepSnapshot(ChronoUnit.SECONDS.between(latest.startTime, latest.endTime) / 3600f,
                if (recent) "Recorded session duration" else "Older session — not last night's sleep",
                latest.metadata.dataOrigin.packageName.ifBlank { "Health Connect" },
                latest.endTime.toEpochMilli(), recent)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { SleepSnapshot(0f, "—", readFailure("Sleep", e)) }
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

/** Expected read failures may degrade gracefully; cancellation must always propagate. */
private inline fun <T> healthRead(block: () -> T): kotlin.Result<T> = try {
    kotlin.Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    kotlin.Result.failure(e)
}
