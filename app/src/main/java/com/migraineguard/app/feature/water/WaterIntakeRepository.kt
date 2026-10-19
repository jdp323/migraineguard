package com.migraineguard.app.feature.water

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.migraineguard.app.core.data.db.WaterDayEntity
import com.migraineguard.app.core.data.db.WaterHistoryDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.migraineguard.app.core.time.localDates
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

private val Context.waterDataStore: DataStore<Preferences> by preferencesDataStore(name = "water_intake")

data class WaterDay(val dateIso: String, val cups: Int)

/** Room is the single source of truth; older DataStore counters migrate without lowering totals. */
class WaterIntakeRepository(context: Context, private val historyDao: WaterHistoryDao) {
    private val legacy = context.applicationContext.waterDataStore
    private val cupsKey = intPreferencesKey("cups")
    private val dateKey = stringPreferencesKey("date")
    private val migratedKey = booleanPreferencesKey("room_migrated_v1")
    private val migrationMutex = Mutex()

    private suspend fun migrateLegacy() = migrationMutex.withLock {
        val prefs = legacy.data.first()
        if (prefs[migratedKey] != true) {
            val date = prefs[dateKey]
            val cups = prefs[cupsKey] ?: 0
            if (date != null && runCatching { LocalDate.parse(date) }.isSuccess) {
                historyDao.keepMaximum(WaterDayEntity(date, cups.coerceAtLeast(0)))
            }
            legacy.edit { it[migratedKey] = true; it.remove(cupsKey); it.remove(dateKey) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val cupsToday: Flow<Int> = flow {
        migrateLegacy()
        emitAll(localDates().flatMapLatest { day ->
            historyDao.observeDay(day.toString()).map { it?.cups ?: 0 }
        })
    }

    fun observeHistory(limit: Int = 14): Flow<List<WaterDay>> =
        historyDao.observeRecent(limit).map { list -> list.map { WaterDay(it.dateIso, it.cups) } }

    suspend fun addCup() { migrateLegacy(); historyDao.changeCups(LocalDate.now().toString(), 1) }
    suspend fun removeCup() { migrateLegacy(); historyDao.changeCups(LocalDate.now().toString(), -1) }
    suspend fun setCups(cups: Int) {
        migrateLegacy()
        historyDao.upsert(WaterDayEntity(LocalDate.now().toString(), cups.coerceIn(0, 1000)))
    }
    suspend fun averageCups(start: LocalDate, end: LocalDate): Float {
        val rows = historyDao.inRange(start.toString(), end.toString())
        return if (rows.isEmpty()) 0f else rows.map { it.cups }.average().toFloat()
    }
}
