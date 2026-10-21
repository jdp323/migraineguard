package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.WorkoutEntry
import java.time.*

object TrainingProgress {
    fun daysThisWeek(entries: List<WorkoutEntry>, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Int {
        val start = today.with(java.time.DayOfWeek.MONDAY)
        return entries.map { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
            .filter { !it.isBefore(start) && !it.isAfter(today) }.distinct().size
    }
    fun volumeKg(entry: WorkoutEntry): Double = entry.sets.filter { it.completed }.sumOf { it.weightKg.toDouble() * it.reps }
    fun exerciseKey(name: String) = name.trim().lowercase(java.util.Locale.ROOT).replace(Regex("\\s+"), " ")
    fun previous(entries: List<WorkoutEntry>, name: String): WorkoutEntry? = entries
        .filter { exerciseKey(it.exerciseName) == exerciseKey(name) }.maxByOrNull { it.timestampMillis }
}

val GymExerciseNames = listOf("Bench press", "Chest press machine", "Lat pulldown", "Seated cable row", "Leg press", "Squat", "Deadlift", "Shoulder press", "Leg extension", "Leg curl", "Cable triceps pushdown", "Biceps curl", "Calf raise", "Push-up", "Plank", "Treadmill walk", "Treadmill run", "Stationary bike", "Rowing machine", "Elliptical")
