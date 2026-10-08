package com.migraineguard.app.core.data.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BackupDao {
    @Query("SELECT * FROM migraine_episodes") suspend fun migraines(): List<MigraineEpisodeEntity>
    @Query("SELECT * FROM nutrition_scans") suspend fun nutrition(): List<NutritionScanEntity>
    @Query("SELECT * FROM water_history") suspend fun water(): List<WaterDayEntity>
    @Query("SELECT * FROM workouts") suspend fun workouts(): List<WorkoutEntity>
    @Query("SELECT * FROM weight_entries") suspend fun weights(): List<WeightEntryEntity>
    @Query("SELECT * FROM medications") suspend fun medications(): List<MedicationEntity>
    @Query("SELECT * FROM ai_sessions") suspend fun sessions(): List<AiSessionEntity>
}
