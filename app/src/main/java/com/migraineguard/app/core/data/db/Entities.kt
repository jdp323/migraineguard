package com.migraineguard.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.migraineguard.app.core.domain.MigraineEpisode
import com.migraineguard.app.core.domain.MigraineSymptom
import com.migraineguard.app.core.domain.NutritionScanRecord
import com.migraineguard.app.core.domain.NutritionScanResult
import com.migraineguard.app.core.domain.PainMarker
import com.migraineguard.app.core.domain.WorkoutEntry
import com.migraineguard.app.core.domain.WorkoutType

@Entity(tableName = "migraine_episodes")
data class MigraineEpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val overallIntensity: Int,
    val notes: String,
    val symptoms: List<MigraineSymptom>,
    val markers: List<PainMarker>,
) {
    fun toDomain() = MigraineEpisode(
        id = id,
        timestampMillis = timestampMillis,
        overallIntensity = overallIntensity,
        notes = notes,
        symptoms = symptoms,
        markers = markers,
    )

    companion object {
        fun fromDomain(e: MigraineEpisode) = MigraineEpisodeEntity(
            id = e.id,
            timestampMillis = e.timestampMillis,
            overallIntensity = e.overallIntensity,
            notes = e.notes,
            symptoms = e.symptoms,
            markers = e.markers,
        )
    }
}

@Entity(tableName = "nutrition_scans")
data class NutritionScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val foodLabel: String,
    val caloriesEstimate: Int?,
    val proteinG: Float?,
    val carbsG: Float?,
    val fatG: Float?,
    val notes: String,
    val imageUri: String?,
    val migraineTriggerHint: String?,
) {
    fun toDomain() = NutritionScanRecord(
        id = id,
        timestampMillis = timestampMillis,
        result = NutritionScanResult(
            foodLabel = foodLabel,
            caloriesEstimate = caloriesEstimate,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            notes = notes,
            imageUri = imageUri,
            migraineTriggerHint = migraineTriggerHint,
        ),
    )

    companion object {
        fun from(result: NutritionScanResult, timestampMillis: Long) = NutritionScanEntity(
            timestampMillis = timestampMillis,
            foodLabel = result.foodLabel,
            caloriesEstimate = result.caloriesEstimate,
            proteinG = result.proteinG,
            carbsG = result.carbsG,
            fatG = result.fatG,
            notes = result.notes,
            imageUri = result.imageUri,
            migraineTriggerHint = result.migraineTriggerHint,
        )
    }
}

@Entity(tableName = "water_history")
data class WaterDayEntity(
    @PrimaryKey val dateIso: String,
    val cups: Int,
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val typeName: String,
    val durationMinutes: Int,
    val calories: Int?,
    val notes: String,
    val fromExerciseOfDay: Boolean = false,
) {
    fun toDomain() = WorkoutEntry(
        id = id,
        timestampMillis = timestampMillis,
        type = runCatching { WorkoutType.valueOf(typeName) }.getOrDefault(WorkoutType.OTHER),
        durationMinutes = durationMinutes,
        calories = calories,
        notes = notes,
        fromExerciseOfDay = fromExerciseOfDay,
    )

    companion object {
        fun fromDomain(e: WorkoutEntry) = WorkoutEntity(
            id = e.id,
            timestampMillis = e.timestampMillis,
            typeName = e.type.name,
            durationMinutes = e.durationMinutes,
            calories = e.calories,
            notes = e.notes,
            fromExerciseOfDay = e.fromExerciseOfDay,
        )
    }
}
