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
        return healthRead {
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

    /** Soft / permission-only messages must not clobber a real fitness error. */
    private fun recordMeaningfulError(message: String, soft: Boolean = false) {
        if (soft) return
        val trimmed = message.trim()
        if (trimmed.isBlank()) return
        lastErrorText = trimmed
    }

    private fun clearErrorIfSleepOnly() {
        if (lastErrorText.contains("latestSleep", ignoreCase = true) ||
            lastErrorText.contains("Sleep permission", ignoreCase = true)
        ) {
            lastErrorText = ""
        }
    }

    override suspend fun latestFitness(): HealthSnapshot {
        lastErrorText = ""
        return try {
            if (availabilityStatus() != HealthConnectStatus.AVAILABLE) {
                return HealthSnapshot(
                    steps = 0,
                    heartRateBpm = null,
                    activeMinutes = 0,
                    sourceLabel = "Health Connect unavailable",
                    stepSync = StepSyncSnapshot(null, null, emptyList(), System.currentTimeMillis(), "Health Connect unavailable"),
                )
            }
            val client = HealthConnectClient.getOrCreate(context)
            val granted = healthRead {
                client.permissionController.getGrantedPermissions()
            }.getOrDefault(emptySet())

            val stepsPerm = stepsReadPermission()
            if (stepsPerm.isNotBlank() && stepsPerm !in granted && granted.isNotEmpty()) {
                // Partial grants: still try reads; missing perms throw SecurityException which we catch.
            }

            val end = Instant.now()
            val start = healthDayStart(end)
            val filter = TimeRangeFilter.between(start, end)
            // Health Connect rescales interval counts to the requested window. Cover the
            // whole day to include updated daily buckets whose end time is tonight.
            val dayFilter = TimeRangeFilter.between(start, healthDayEnd(end))

            val stepRead = healthRead {
                client.aggregate(
                    AggregateRequest(metrics = setOf(StepsRecord.COUNT_TOTAL), timeRangeFilter = dayFilter),
                )
            }
            val stepTotal = stepRead.getOrNull()?.let { response ->
                response[StepsRecord.COUNT_TOTAL]?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt()
            }
            stepRead.exceptionOrNull()?.let { e ->
                recordMeaningfulError(
                    if (e is SecurityException) "Steps permission needed"
                    else "Steps read unavailable (${e.javaClass.simpleName})",
                )
            }
            // This is a comparison only: never sum a source total with the combined aggregate.
            val samsungRead = if (stepRead.isSuccess) healthRead {
                client.aggregate(
                    AggregateRequest(
                        metrics = setOf(StepsRecord.COUNT_TOTAL),
                        timeRangeFilter = dayFilter,
                        dataOriginFilter = setOf(DataOrigin(HealthConnectPackages.SAMSUNG_HEALTH)),
                    ),
                )[StepsRecord.COUNT_TOTAL]?.coerceIn(0L, Int.MAX_VALUE.toLong())?.toInt()
            } else kotlin.Result.success(null)
            val steps = stepTotal ?: 0
            val stepSync = StepSyncSnapshot(
                total = stepTotal,
                samsungTotal = samsungRead.getOrNull(),
                sourcePackages = stepRead.getOrNull()?.dataOrigins?.map { it.packageName }?.sorted().orEmpty(),
                readAtMillis = end.toEpochMilli(),
                message = stepSyncMessage(stepTotal, samsungRead.getOrNull(), samsungRead.isFailure),
            )

            val bpm = healthRead {
                client.readRecords(ReadRecordsRequest(HeartRateRecord::class, filter, ascendingOrder = false, pageSize = 1))
                    .records.flatMap { it.samples }
                    .maxByOrNull { it.time }?.beatsPerMinute?.toInt()
            }.getOrNull()

            val distance = healthRead {
                client.aggregate(AggregateRequest(setOf(DistanceRecord.DISTANCE_TOTAL), dayFilter))[DistanceRecord.DISTANCE_TOTAL]?.inMeters
            }.getOrNull()

            val calorieRead = healthRead {
                val combined = client.aggregate(AggregateRequest(setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL), dayFilter))
                val origins = combined.dataOrigins
                if (origins.size > 1) {
                    // Calorie aggregates do not reliably deduplicate multiple writers.
                    val samsung = origins.firstOrNull { it.packageName == HealthConnectPackages.SAMSUNG_HEALTH }
                    if (samsung == null) null else {
                        val value = client.aggregate(AggregateRequest(
                            setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL), filter, setOf(samsung),
                        ))[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
                        value?.let { it to "Samsung Health" }
                    }
                } else combined[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.let {
                    it to origins.firstOrNull()?.packageName.orEmpty()
                }
            }.getOrNull()
            val calories = calorieRead?.first

            val activeFromExercise = healthRead {
                client.aggregate(AggregateRequest(setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL), filter))[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes()?.coerceIn(0L, 1440L)?.toInt() ?: 0
            }.getOrDefault(0)

            clearErrorIfSleepOnly()
            val label = when {
                stepsPerm.isNotBlank() && stepsPerm !in granted && steps == 0 && bpm == null ->
                    "Fitness permission needed"
                steps == 0 && bpm == null && activeFromExercise == 0 ->
                    "Health Connect (no fitness data)"
                else -> "Health Connect"
            }
            HealthSnapshot(
                steps = steps,
                heartRateBpm = bpm,
                activeMinutes = activeFromExercise,
                sourceLabel = label,
                distanceMeters = distance?.takeIf { it > 0 },
                caloriesBurned = calories?.takeIf { it > 0 },
                caloriesSourceLabel = calorieRead?.second,
                stepSync = stepSync,
            )
        } catch (e: SecurityException) {
            HealthSnapshot(
                steps = 0,
                heartRateBpm = null,
                activeMinutes = 0,
                sourceLabel = "Fitness permission needed",
                    stepSync = StepSyncSnapshot(null, null, emptyList(), System.currentTimeMillis(), "Fitness permission needed"),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            recordMeaningfulError("latestFitness: ${e.message ?: e.javaClass.simpleName}")
            HealthSnapshot(
                steps = 0,
                heartRateBpm = null,
                activeMinutes = 0,
                sourceLabel = "Fitness read unavailable",
                    stepSync = StepSyncSnapshot(null, null, emptyList(), System.currentTimeMillis(), "Fitness read unavailable"),
            )
        }
    }

    override suspend fun latestSleep(): SleepSnapshot {
        return try {
            if (availabilityStatus() != HealthConnectStatus.AVAILABLE) {
                return SleepSnapshot(
                    hours = 0f,
                    qualityLabel = "—",
                    sourceLabel = "Health Connect unavailable",
                )
            }
            val client = HealthConnectClient.getOrCreate(context)
            val granted = healthRead {
                client.permissionController.getGrantedPermissions()
            }.getOrDefault(emptySet())
            val sleepPerm = sleepReadPermission()
            if (sleepPerm.isNotBlank() && sleepPerm !in granted) {
                // Soft: do not set lastError — sleep must not block fitness UI.
                return SleepSnapshot(
                    hours = 0f,
                    qualityLabel = "—",
                    sourceLabel = "Sleep permission needed",
                )
            }

            val end = Instant.now()
            val start = end.minus(2, ChronoUnit.DAYS)
            val sessions = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    TimeRangeFilter.between(start, end),
                ),
            ).records

            val latest = sessions.maxByOrNull { it.endTime }
            if (latest == null) {
                return SleepSnapshot(
                    hours = 0f,
                    qualityLabel = "No data",
                    sourceLabel = "Health Connect (no sessions)",
                )
            }
            val minutes = ChronoUnit.MINUTES.between(latest.startTime, latest.endTime)
                .toInt().coerceAtLeast(0)
            val hours = minutes / 60f
            val quality = "Recorded session duration"
            SleepSnapshot(hours = hours, qualityLabel = quality, sourceLabel = "Health Connect")
        } catch (e: SecurityException) {
            SleepSnapshot(
                hours = 0f,
                qualityLabel = "—",
                sourceLabel = "Sleep permission needed",
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Soft failure — never crash UI; do not overwrite fitness lastError.
            recordMeaningfulError(
                "latestSleep: ${e.message ?: e.javaClass.simpleName}",
                soft = true,
            )
            SleepSnapshot(
                hours = 0f,
                qualityLabel = "—",
                sourceLabel = "Sleep read unavailable",
            )
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

/** Expected read failures may degrade gracefully; cancellation must always propagate. */
private inline fun <T> healthRead(block: () -> T): kotlin.Result<T> = try {
    kotlin.Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    kotlin.Result.failure(e)
}
