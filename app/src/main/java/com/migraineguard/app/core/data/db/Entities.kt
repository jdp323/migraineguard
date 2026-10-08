package com.migraineguard.app.core.data.db

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.migraineguard.app.core.domain.MedicationEntry
import com.migraineguard.app.core.domain.MigraineEpisode
import com.migraineguard.app.core.domain.MigraineSymptom
import com.migraineguard.app.core.domain.MigraineTrigger
import com.migraineguard.app.core.domain.NutritionScanRecord
import com.migraineguard.app.core.domain.NutritionScanResult
import com.migraineguard.app.core.domain.PainMarker
import com.migraineguard.app.core.domain.WeightEntry
import com.migraineguard.app.core.domain.WorkoutEntry
import com.migraineguard.app.core.domain.WorkoutType

@Serializable
@Entity(tableName = "migraine_episodes")
data class MigraineEpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val overallIntensity: Int,
    val notes: String,
    val symptoms: List<MigraineSymptom>,
    val markers: List<PainMarker>,
    val triggers: List<MigraineTrigger> = emptyList(),
) {
    fun toDomain() = MigraineEpisode(
        id = id,
        timestampMillis = timestampMillis,
        overallIntensity = overallIntensity,
        notes = notes,
        symptoms = symptoms,
        markers = markers,
        triggers = triggers,
    )

    companion object {
        fun fromDomain(e: MigraineEpisode) = MigraineEpisodeEntity(
            id = e.id,
            timestampMillis = e.timestampMillis,
            overallIntensity = e.overallIntensity,
            notes = e.notes,
            symptoms = e.symptoms,
            markers = e.markers,
            triggers = e.triggers,
        )
    }
}

@Serializable
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
    private fun wasPlaceholder(): Boolean = notes.startsWith("Structured estimate (")

    fun toDomain() = NutritionScanRecord(
        id = id,
        timestampMillis = timestampMillis,
        result = NutritionScanResult(
            foodLabel = foodLabel,
            caloriesEstimate = caloriesEstimate.takeUnless { wasPlaceholder() },
            proteinG = proteinG.takeUnless { wasPlaceholder() },
            carbsG = carbsG.takeUnless { wasPlaceholder() },
            fatG = fatG.takeUnless { wasPlaceholder() },
            notes = if (wasPlaceholder()) "Legacy placeholder: nutrition and trigger estimates were not analyzed. Description preserved." else notes,
            imageUri = imageUri,
            migraineTriggerHint = migraineTriggerHint.takeUnless { wasPlaceholder() },
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

@Serializable
@Entity(tableName = "water_history")
data class WaterDayEntity(
    @PrimaryKey val dateIso: String,
    val cups: Int,
)

@Serializable
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

@Serializable
@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val weightKg: Float,
    val notes: String = "",
) {
    fun toDomain() = WeightEntry(id, timestampMillis, weightKg, notes)

    companion object {
        fun fromDomain(e: WeightEntry) = WeightEntryEntity(
            id = e.id,
            timestampMillis = e.timestampMillis,
            weightKg = e.weightKg,
            notes = e.notes,
        )
    }
}

@Serializable
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val timeLabel: String,
    val takenToday: Boolean = false,
    val lastTakenMillis: Long = 0,
    val takenDateIso: String = "",
) {
    fun toDomain(todayIso: String) = MedicationEntry(
        id = id,
        name = name,
        dose = dose,
        timeLabel = timeLabel,
        takenToday = takenToday && takenDateIso == todayIso,
        lastTakenMillis = lastTakenMillis,
    )

    companion object {
        fun fromDomain(e: MedicationEntry, todayIso: String) = MedicationEntity(
            id = e.id,
            name = e.name,
            dose = e.dose,
            timeLabel = e.timeLabel,
            takenToday = e.takenToday,
            lastTakenMillis = e.lastTakenMillis,
            takenDateIso = if (e.takenToday) todayIso else "",
        )
    }
}


@Serializable
@Entity(tableName = "ai_sessions")
data class AiSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val personaId: String,
    val summary: String,
    val fullResponse: String,
    val userPrompt: String = "",
    val timestampMillis: Long,
)
