package com.migraineguard.app.feature.fitness
import com.migraineguard.app.core.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class TrainingProgressTest {
    private fun entry(date:String,name:String="Leg press",sets:List<ExerciseSet> = emptyList())=WorkoutEntry(timestampMillis=LocalDate.parse(date).atTime(12,0).toInstant(ZoneOffset.UTC).toEpochMilli(),type=WorkoutType.STRENGTH,durationMinutes=20,exerciseName=name,sets=sets)
    @Test fun weeklyGoalCountsDaysNotExerciseRows() { val today=LocalDate.parse("2026-10-21");assertEquals(2,TrainingProgress.daysThisWeek(listOf(entry("2026-10-19"),entry("2026-10-19"),entry("2026-10-20"),entry("2026-10-18"),entry("2026-10-22")),today,ZoneOffset.UTC)) }
    @Test fun volumeExcludesUnfinishedSets() { assertEquals(1000.0,TrainingProgress.volumeKg(entry("2026-10-20",sets=listOf(ExerciseSet(50f,10,true),ExerciseSet(50f,10,true),ExerciseSet(50f,10,false)))),0.01) }
    @Test fun previousNormalizesNameAndKeepsLatest() { val a=entry("2026-10-19","  Leg   Press ");val b=entry("2026-10-20");assertEquals(b,TrainingProgress.previous(listOf(a,b),"LEG PRESS")) }
    @Test(expected=IllegalArgumentException::class) fun invalidWeightsRejected() { ExerciseSet(Float.NaN,10).validate() }
    @Test(expected=IllegalArgumentException::class) fun invalidRepsRejected() { ExerciseSet(20f,0).validate() }
}
