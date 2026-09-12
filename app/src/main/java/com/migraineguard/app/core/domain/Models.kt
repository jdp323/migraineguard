package com.migraineguard.app.core.domain

/**
 * Shared domain models used across features.
 * Keep these free of Android / Compose dependencies.
 */

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String? = null,
)

data class PainMarker(
    val id: String,
    /** Normalized X on head silhouette, 0f..1f */
    val x: Float,
    /** Normalized Y on head silhouette, 0f..1f */
    val y: Float,
    /** 1..10 */
    val intensity: Int,
)

enum class MigraineSymptom(val label: String) {
    NAUSEA("Nausea"),
    LIGHT_SENSITIVITY("Light"),
    SOUND_SENSITIVITY("Sound"),
    AURA("Aura"),
    DIZZINESS("Dizziness"),
    FATIGUE("Fatigue"),
}

data class MigraineEpisode(
    val id: Long = 0,
    val timestampMillis: Long,
    val overallIntensity: Int,
    val notes: String,
    val symptoms: List<MigraineSymptom>,
    val markers: List<PainMarker>,
)

data class HealthSnapshot(
    val steps: Int,
    val heartRateBpm: Int?,
    val activeMinutes: Int,
    val sourceLabel: String = "Local",
)

data class SleepSnapshot(
    val hours: Float,
    val qualityLabel: String,
    val sourceLabel: String = "Local",
)

data class WeatherSnapshot(
    val temperatureC: Float,
    val pressureHpa: Float,
    val humidityPercent: Int,
    val condition: String,
    val locationLabel: String,
    val isCachedOrFake: Boolean = false,
)

data class NutritionScanResult(
    val foodLabel: String,
    val caloriesEstimate: Int?,
    val proteinG: Float? = null,
    val carbsG: Float? = null,
    val fatG: Float? = null,
    val notes: String,
    val imageUri: String? = null,
    val migraineTriggerHint: String? = null,
)

data class NutritionScanRecord(
    val id: Long = 0,
    val timestampMillis: Long,
    val result: NutritionScanResult,
)

data class InsightReport(
    val title: String,
    val summary: String,
    val periodLabel: String,
    val migraineCount: Int = 0,
    val avgIntensity: Float = 0f,
    val avgWaterCups: Float = 0f,
    val avgSleepHours: Float = 0f,
    val pressureTrend: String = "",
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class WaterUnit {
    CUPS,
    ML,
}
