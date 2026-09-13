package com.migraineguard.app.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MigraineDao {
    @Query("SELECT * FROM migraine_episodes ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<MigraineEpisodeEntity>>

    @Query("SELECT * FROM migraine_episodes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MigraineEpisodeEntity?

    @Query(
        "SELECT * FROM migraine_episodes WHERE timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC"
    )
    suspend fun inRange(start: Long, end: Long): List<MigraineEpisodeEntity>

    @Insert
    suspend fun insert(entity: MigraineEpisodeEntity): Long

    @Query("DELETE FROM migraine_episodes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_scans ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<NutritionScanEntity>>

    @Query(
        "SELECT * FROM nutrition_scans WHERE timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC"
    )
    suspend fun inRange(start: Long, end: Long): List<NutritionScanEntity>

    @Insert
    suspend fun insert(entity: NutritionScanEntity): Long

    @Query("DELETE FROM nutrition_scans WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface WaterHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: WaterDayEntity)

    @Query("SELECT * FROM water_history WHERE dateIso = :dateIso LIMIT 1")
    suspend fun get(dateIso: String): WaterDayEntity?

    @Query("SELECT * FROM water_history ORDER BY dateIso DESC LIMIT :limit")
    fun observeRecent(limit: Int = 30): Flow<List<WaterDayEntity>>

    @Query("SELECT * FROM water_history WHERE dateIso BETWEEN :start AND :end ORDER BY dateIso ASC")
    suspend fun inRange(start: String, end: String): List<WaterDayEntity>
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query(
        "SELECT * FROM workouts WHERE timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC"
    )
    suspend fun inRange(start: Long, end: Long): List<WorkoutEntity>

    @Insert
    suspend fun insert(entity: WorkoutEntity): Long

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<WeightEntryEntity>>

    @Insert
    suspend fun insert(entity: WeightEntryEntity): Long

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun latest(): WeightEntryEntity?
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY id ASC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Insert
    suspend fun insert(entity: MedicationEntity): Long

    @Update
    suspend fun update(entity: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun delete(id: Long)
}


@Dao
interface AiSessionDao {
    @Query("SELECT * FROM ai_sessions ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<AiSessionEntity>>

    @Query("SELECT * FROM ai_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AiSessionEntity?

    @Insert
    suspend fun insert(entity: AiSessionEntity): Long

    @Query("DELETE FROM ai_sessions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM ai_sessions")
    suspend fun clearAll()
}
