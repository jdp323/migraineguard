package com.migraineguard.app.feature.backup

import com.migraineguard.app.core.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Account-scoped journal size: excludes the APK, caches and external gallery. */
data class JournalStorageUsage(val databaseBytes: Long, val photoBytes: Long, val measurementLogs: Long)
class JournalStorageRepository(private val db: AppDatabase) {
    private val mutex = Mutex()
    suspend fun usage(): JournalStorageUsage = withContext(Dispatchers.IO) { mutex.withLock { readUsage() } }
    private fun readUsage(): JournalStorageUsage {
        val sql = db.openHelper.writableDatabase
        val file = File(sql.path)
        val bytes = file.length() + File(sql.path + "-wal").length()
        val photos = sql.query("SELECT COALESCE(SUM(length(jpegBytes)),0) FROM body_progress").use { it.moveToFirst(); it.getLong(0) }
        val logs = sql.query("SELECT COUNT(*) FROM body_measurements WHERE deleted = 0").use { it.moveToFirst(); it.getLong(0) }
        return JournalStorageUsage(bytes, photos, logs)
    }
    suspend fun optimize(): JournalStorageUsage = withContext(Dispatchers.IO) { mutex.withLock {
        val sql = db.openHelper.writableDatabase
        check(!sql.inTransaction()) { "Storage is busy. Try again after the current save." }
        sql.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        sql.execSQL("VACUUM")
        sql.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
        readUsage()
    } }
}
