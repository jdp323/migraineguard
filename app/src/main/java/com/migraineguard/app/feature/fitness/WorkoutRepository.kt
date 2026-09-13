package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.data.db.WorkoutDao
import com.migraineguard.app.core.data.db.WorkoutEntity
import com.migraineguard.app.core.domain.ExerciseSuggestion
import com.migraineguard.app.core.domain.WorkoutEntry
import com.migraineguard.app.core.domain.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkoutRepository(private val dao: WorkoutDao) {
    fun observeWorkouts(): Flow<List<WorkoutEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun inRange(startMs: Long, endMs: Long): List<WorkoutEntry> =
        dao.inRange(startMs, endMs).map { it.toDomain() }

    suspend fun save(entry: WorkoutEntry): Long =
        dao.insert(WorkoutEntity.fromDomain(entry))

    suspend fun delete(id: Long) = dao.delete(id)

    fun exerciseOfTheDay(date: LocalDate = LocalDate.now()): ExerciseSuggestion {
        val catalog = ExerciseCatalog.ALL
        val index = (date.dayOfYear - 1) % catalog.size
        return catalog[index]
    }
}

object ExerciseCatalog {
    val ALL: List<ExerciseSuggestion> = listOf(
        ExerciseSuggestion("eod-walk", "Gentle outdoor walk", WorkoutType.WALK, 20, "Easy pace. Great for migraine recovery days."),
        ExerciseSuggestion("eod-yoga", "Neck & shoulder release yoga", WorkoutType.YOGA, 15, "Slow stretches to ease tension headaches."),
        ExerciseSuggestion("eod-strength", "Bodyweight strength circuit", WorkoutType.STRENGTH, 25, "Squats, wall push-ups, and core — keep intensity moderate."),
        ExerciseSuggestion("eod-cycle", "Steady cycling", WorkoutType.CYCLING, 30, "Indoor or outdoor. Stay hydrated."),
        ExerciseSuggestion("eod-run", "Easy jog or walk-run", WorkoutType.RUN, 20, "Conversational pace only."),
        ExerciseSuggestion("eod-mobility", "Mobility flow", WorkoutType.YOGA, 12, "Hip openers and thoracic twists."),
        ExerciseSuggestion("eod-stairs", "Stair intervals", WorkoutType.WALK, 15, "Short bursts with full recovery."),
        ExerciseSuggestion("eod-core", "Core stability", WorkoutType.STRENGTH, 18, "Planks and dead bugs — breathe calmly."),
        ExerciseSuggestion("eod-stretch", "Full-body stretch", WorkoutType.YOGA, 20, "Hold each stretch for 30–45 seconds."),
        ExerciseSuggestion("eod-other", "Mindful movement", WorkoutType.OTHER, 15, "Dance, pilates, or whatever feels good today."),
    )
}
