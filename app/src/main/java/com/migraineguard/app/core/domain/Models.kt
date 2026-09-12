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

data class HealthSnapshot(
    val steps: Int,
    val heartRateBpm: Int?,
    val activeMinutes: Int,
)

data class SleepSnapshot(
    val hours: Float,
    val qualityLabel: String,
)

data class WeatherSnapshot(
    val temperatureC: Float,
    val pressureHpa: Float,
    val condition: String,
    val locationLabel: String,
)

data class NutritionScanResult(
    val foodLabel: String,
    val caloriesEstimate: Int?,
    val notes: String,
)

data class InsightReport(
    val title: String,
    val summary: String,
    val periodLabel: String,
)
