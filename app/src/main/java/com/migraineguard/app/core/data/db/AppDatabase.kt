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
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun migraineDao(): MigraineDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun waterHistoryDao(): WaterHistoryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "migraineguard.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
