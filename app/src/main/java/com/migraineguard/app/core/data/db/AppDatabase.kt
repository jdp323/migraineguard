package com.migraineguard.app.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MigraineEpisodeEntity::class,
        NutritionScanEntity::class,
        WaterDayEntity::class,
        WorkoutEntity::class,
        WeightEntryEntity::class,
        MedicationEntity::class,
        AiSessionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun migraineDao(): MigraineDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun waterHistoryDao(): WaterHistoryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun medicationDao(): MedicationDao
    abstract fun aiSessionDao(): AiSessionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "migraineguard.db",
                ).addMigrations(*PreservingMigrations.ALL).build().also { instance = it }
            }
    }
}
