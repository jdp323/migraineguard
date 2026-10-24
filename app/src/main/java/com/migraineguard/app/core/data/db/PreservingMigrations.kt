package com.migraineguard.app.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Historical schemas 1–3 were reviewed from the repository; no tables are dropped. */
object PreservingMigrations {
    private val oldMigrations: Array<Migration> = arrayOf(1, 2, 3).map { old ->
        object : Migration(old, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, typeName TEXT NOT NULL, durationMinutes INTEGER NOT NULL, calories INTEGER, notes TEXT NOT NULL, fromExerciseOfDay INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS weight_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestampMillis INTEGER NOT NULL, weightKg REAL NOT NULL, notes TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS medications (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, dose TEXT NOT NULL, timeLabel TEXT NOT NULL, takenToday INTEGER NOT NULL, lastTakenMillis INTEGER NOT NULL, takenDateIso TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS ai_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, personaId TEXT NOT NULL, summary TEXT NOT NULL, fullResponse TEXT NOT NULL, userPrompt TEXT NOT NULL, timestampMillis INTEGER NOT NULL)")
                val columns = db.query("PRAGMA table_info(migraine_episodes)").use { cursor ->
                    buildSet { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) }
                }
                if ("triggers" !in columns) {
                    db.execSQL("ALTER TABLE migraine_episodes ADD COLUMN triggers TEXT NOT NULL DEFAULT '[]'")
                }
            }
        }
    }.toTypedArray()
    val FROM_4 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN exerciseName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE workouts ADD COLUMN setsJson TEXT NOT NULL DEFAULT '[]'")
            db.execSQL("ALTER TABLE workouts ADD COLUMN equipmentPhotoUri TEXT")
        }
    }
    val FROM_5 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN detailsJson TEXT NOT NULL DEFAULT '{}'")
            db.execSQL("CREATE TABLE IF NOT EXISTS water_action_receipts (token TEXT NOT NULL PRIMARY KEY, appliedAtMillis INTEGER NOT NULL)")
        }
    }
    val FROM_6 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS body_progress (id TEXT NOT NULL PRIMARY KEY, dateIso TEXT NOT NULL, pose TEXT NOT NULL, notes TEXT NOT NULL, weightKg REAL, waistCm REAL, bodyFatPercent REAL, jpegBase64 TEXT NOT NULL, modifiedAtMillis INTEGER NOT NULL, deleted INTEGER NOT NULL)")
        }
    }
    val ALL: Array<Migration> = oldMigrations + FROM_4 + FROM_5 + FROM_6
}
