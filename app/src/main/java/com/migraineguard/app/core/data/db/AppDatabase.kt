package com.migraineguard.app.core.data.db

import android.content.Context
import com.migraineguard.app.core.security.AccountStorage
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
        WaterActionReceipt::class,
        BodyProgressEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bodyProgressDao(): BodyProgressDao
    abstract fun waterActionDao(): WaterActionDao
    abstract fun backupDao(): BackupDao
    abstract fun migraineDao(): MigraineDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun waterHistoryDao(): WaterHistoryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun medicationDao(): MedicationDao
    abstract fun aiSessionDao(): AiSessionDao

    companion object {
        private val instances = mutableMapOf<String, AppDatabase>()

        @Synchronized fun get(context: Context, account: String = AccountStorage.LOCAL): AppDatabase {
            val name = AccountStorage.databaseName(account)
            val key = context.getDatabasePath(name).absolutePath
            return instances.getOrPut(key) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                    .addMigrations(*PreservingMigrations.ALL).build()
            }
        }
    }
}
