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

enum class SexGender(val label: String) {
    MAN("Man"),
    WOMAN("Woman"),
    PREFER_NOT("Prefer not to say"),
    OTHER("Other"),
}

enum class ActivityLevel(val label: String) {
    SEDENTARY("Sedentary"),
    LIGHT("Light"),
    MODERATE("Moderate"),
    ACTIVE("Active"),
    VERY_ACTIVE("Very active"),
}

data class BodyProfile(
    val displayName: String = "",
    val dateOfBirthIso: String = "",
    val sex: SexGender? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val activityLevel: ActivityLevel? = null,
    val useMetric: Boolean = true,
    val profileComplete: Boolean = false,
) {
    val bmi: Float?
        get() {
            val h = heightCm ?: return null
            val w = weightKg ?: return null
            if (h <= 0f) return null
            val m = h / 100f
            return w / (m * m)
        }

    val bmiLabel: String
        get() {
            val v = bmi ?: return "—"
            return when {
                v < 18.5f -> "Underweight"
                v < 25f -> "Healthy"
                v < 30f -> "Overweight"
                else -> "Obese range"
            }
        }

    val firstName: String
        get() = displayName.trim().substringBefore(' ').ifBlank { displayName.ifBlank { "Friend" } }
}

enum class HeadView(val label: String) {
    FRONT("Front"),
    LEFT("Left"),
    RIGHT("Right"),
    BACK("Back"),
    TOP("Top"),
}

data class PainMarker(
    val id: String,
    /** Normalized X on head silhouette, 0f..1f */
    val x: Float,
    /** Normalized Y on head silhouette, 0f..1f */
    val y: Float,
    /** 1..10 */
    val intensity: Int,
    /** Which head view this marker belongs to */
    val viewId: String = HeadView.FRONT.name,
)

enum class MigraineSymptom(val label: String) {
    NAUSEA("Nausea"),
    LIGHT_SENSITIVITY("Light"),
    SOUND_SENSITIVITY("Sound"),
    AURA("Aura"),
    DIZZINESS("Dizziness"),
    FATIGUE("Fatigue"),
}

enum class MigraineTrigger(val label: String) {
    CAFFEINE("Caffeine"),
    STRESS("Stress"),
    SCREEN("Screen"),
    WEATHER("Weather"),
    SLEEP("Sleep"),
    FOOD("Food"),
    DEHYDRATION("Dehydration"),
    OTHER("Other"),
}

data class MigraineEpisode(
    val id: Long = 0,
    val timestampMillis: Long,
    val overallIntensity: Int,
    val notes: String,
    val symptoms: List<MigraineSymptom>,
    val markers: List<PainMarker>,
    val triggers: List<MigraineTrigger> = emptyList(),
)

data class HealthSnapshot(
    val steps: Int,
    val heartRateBpm: Int?,
    val activeMinutes: Int,
    val sourceLabel: String = "Local",
    val distanceMeters: Double? = null,
    val caloriesBurned: Double? = null,
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
    val foodScanCount: Int = 0,
    val topFoods: List<String> = emptyList(),
    val totalSteps: Int = 0,
    val workoutCount: Int = 0,
    val workoutMinutes: Int = 0,
    val sideSummary: String = "",
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

enum class WorkoutType(val label: String) {
    WALK("Walk"),
    RUN("Run"),
    STRENGTH("Strength"),
    YOGA("Yoga"),
    CYCLING("Cycling"),
    OTHER("Other"),
}

data class WorkoutEntry(
    val id: Long = 0,
    val timestampMillis: Long,
    val type: WorkoutType,
    val durationMinutes: Int,
    val calories: Int? = null,
    val notes: String = "",
    val fromExerciseOfDay: Boolean = false,
)

data class ExerciseSuggestion(
    val id: String,
    val title: String,
    val type: WorkoutType,
    val durationMinutes: Int,
    val description: String,
)

data class WeightEntry(
    val id: Long = 0,
    val timestampMillis: Long,
    val weightKg: Float,
    val notes: String = "",
)

data class MedicationEntry(
    val id: Long = 0,
    val name: String,
    val dose: String,
    val timeLabel: String,
    val takenToday: Boolean = false,
    val lastTakenMillis: Long = 0,
)


data class AiSession(
    val id: Long = 0,
    val title: String,
    val personaId: String,
    val summary: String,
    val fullResponse: String,
    val userPrompt: String = "",
    val timestampMillis: Long,
)
