package com.migraineguard.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.feature.backup.BackupRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AppReliabilityTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Test fun migrationsPreserveOldJournalAndWater() = runBlocking {
        for (version in 1..3) {
            val name = "migration-fixture-$version-${UUID.randomUUID()}.db"
            val path = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
            SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
                old.execSQL("CREATE TABLE migraine_episodes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, overallIntensity INTEGER NOT NULL, notes TEXT NOT NULL, symptoms TEXT NOT NULL, markers TEXT NOT NULL" +
                    (if (version >= 3) ", triggers TEXT NOT NULL" else "") + ")")
                old.execSQL("CREATE TABLE nutrition_scans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, foodLabel TEXT NOT NULL, caloriesEstimate INTEGER, proteinG REAL, carbsG REAL, fatG REAL, notes TEXT NOT NULL, imageUri TEXT, migraineTriggerHint TEXT)")
                old.execSQL("CREATE TABLE water_history (dateIso TEXT NOT NULL PRIMARY KEY, cups INTEGER NOT NULL)")
                if (version >= 2) old.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, typeName TEXT NOT NULL, durationMinutes INTEGER NOT NULL, calories INTEGER, notes TEXT NOT NULL, fromExerciseOfDay INTEGER NOT NULL)")
                if (version >= 3) {
                    old.execSQL("CREATE TABLE weight_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, weightKg REAL NOT NULL, notes TEXT NOT NULL)")
                    old.execSQL("CREATE TABLE medications (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, dose TEXT NOT NULL, timeLabel TEXT NOT NULL, takenToday INTEGER NOT NULL, lastTakenMillis INTEGER NOT NULL, takenDateIso TEXT NOT NULL)")
                }
                old.execSQL("INSERT INTO migraine_episodes (timestampMillis,overallIntensity,notes,symptoms,markers" +
                    (if (version >= 3) ",triggers" else "") + ") VALUES (1000,6,'migration record','[]','[]'" +
                    (if (version >= 3) ",'[]'" else "") + ")")
                old.execSQL("INSERT INTO water_history VALUES ('2026-10-01',7)")
                old.version = version
            }
            val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(*PreservingMigrations.ALL).build()
            try {
                assertEquals("migration record", db.migraineDao().inRange(0, 2000).single().notes)
                assertEquals(7, db.waterHistoryDao().get("2026-10-01")!!.cups)
                assertTrue(db.backupDao().sessions().isEmpty())
            } finally { db.close(); context.deleteDatabase(name) }
        }
    }

    @Test fun backupRestoreIsEncryptedAndIdempotent() = runBlocking {
        val first = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val second = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val file = File(context.filesDir, "backup-fixture-${UUID.randomUUID()}.mgbak")
        val prefs = UserPreferencesRepository(context)
        try {
            first.migraineDao().insert(MigraineEpisodeEntity(timestampMillis = 1000,
                overallIntensity = 5, notes = "fixture private journal", symptoms = emptyList(), markers = emptyList()))
            first.waterHistoryDao().upsert(WaterDayEntity("2026-10-01", 4))
            first.nutritionDao().insert(NutritionScanEntity(timestampMillis = 1000, foodLabel = "Test meal",
                caloriesEstimate = null, proteinG = null, carbsG = null, fatG = null, notes = "fixture food",
                imageUri = "content://fixture/photo", migraineTriggerHint = null))
            first.workoutDao().insert(WorkoutEntity(timestampMillis = 1000, typeName = "WALK",
                durationMinutes = 10, calories = null, notes = "fixture workout"))
            first.weightDao().insert(WeightEntryEntity(timestampMillis = 1000, weightKg = 70f))
            first.medicationDao().insert(MedicationEntity(name = "Fixture medicine", dose = "fixture label",
                timeLabel = "08:00"))
            first.aiSessionDao().insert(AiSessionEntity(title = "Fixture AI review", personaId = "coach",
                summary = "fixture summary", fullResponse = "fixture response", timestampMillis = 1000))
            val uri = Uri.fromFile(file)
            BackupRepository(context, first, prefs).export(uri, "fixture backup password".toCharArray())
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("fixture private journal"))
            val target = BackupRepository(context, second, prefs)
            assertEquals(7, target.restore(uri, "fixture backup password".toCharArray(), false))
            assertEquals(0, target.restore(uri, "fixture backup password".toCharArray(), false))
            assertEquals("fixture private journal", second.backupDao().migraines().single().notes)
            assertNull(second.backupDao().nutrition().single().imageUri)
            assertEquals(70f, second.backupDao().weights().single().weightKg, 0f)
            assertEquals("fixture response", second.backupDao().sessions().single().fullResponse)
        } finally { first.close(); second.close(); file.delete() }
    }

    @Test fun apiKeyIsEncryptedOnDiskAndCanBeRead() = runBlocking {
        val prefs = UserPreferencesRepository(context)
        val fixture = "fixture-value-for-keystore-test"
        try {
            prefs.setGeminiApiKey(fixture)
            assertEquals(fixture, prefs.getGeminiApiKey())
            val file = File(context.filesDir, "datastore/user_settings.preferences_pb")
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains(fixture))
        } finally { prefs.clearAllAiKeys() }
    }

    @Test fun mainActivityStarts() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { assertFalse(it.isFinishing) }
        }
    }
    @Test fun legacyOAuthOverrideMigratesButCustomClientIsPreserved() = runBlocking {
        val prefs = UserPreferencesRepository(context)
        val previous = prefs.getGoogleWebClientId()
        try {
            prefs.setGoogleWebClientId("555526836064-aria393bjpcgr17j6fatl0nbeqtvqri6.apps.googleusercontent.com")
            assertEquals("", prefs.getGoogleWebClientId())
            val custom = "fixture-custom.apps.googleusercontent.com"
            prefs.setGoogleWebClientId(custom)
            assertEquals(custom, prefs.getGoogleWebClientId())
        } finally { prefs.setGoogleWebClientId(previous) }
    }

}
