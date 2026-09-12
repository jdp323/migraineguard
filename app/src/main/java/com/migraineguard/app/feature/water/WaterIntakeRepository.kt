package com.migraineguard.app.feature.water

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.migraineguard.app.core.data.db.WaterDayEntity
import com.migraineguard.app.core.data.db.WaterHistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.waterDataStore: DataStore<Preferences> by preferencesDataStore(name = "water_intake")

data class WaterDay(val dateIso: String, val cups: Int)

/**
 * Cup counter with DataStore for today + Room history for trends.
 */
class WaterIntakeRepository(
    context: Context,
    private val historyDao: WaterHistoryDao,
) {
    private val dataStore = context.applicationContext.waterDataStore
    private val cupsKey = intPreferencesKey("cups")
    private val dateKey = stringPreferencesKey("date")

    val cupsToday: Flow<Int> = dataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        if (prefs[dateKey] != today) 0 else prefs[cupsKey] ?: 0
    }

    fun observeHistory(limit: Int = 14): Flow<List<WaterDay>> =
        historyDao.observeRecent(limit).map { list -> list.map { WaterDay(it.dateIso, it.cups) } }

    suspend fun addCup() = mutate(1)

    suspend fun removeCup() = mutate(-1)

    suspend fun setCups(cups: Int) {
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val value = cups.coerceAtLeast(0)
            prefs[dateKey] = today
            prefs[cupsKey] = value
            historyDao.upsert(WaterDayEntity(today, value))
        }
    }

    suspend fun averageCups(start: LocalDate, end: LocalDate): Float {
        val rows = historyDao.inRange(start.toString(), end.toString())
        if (rows.isEmpty()) return 0f
        return rows.map { it.cups }.average().toFloat()
    }

    private suspend fun mutate(delta: Int) {
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val current = if (prefs[dateKey] != today) 0 else prefs[cupsKey] ?: 0
            val next = (current + delta).coerceAtLeast(0)
            prefs[dateKey] = today
            prefs[cupsKey] = next
            historyDao.upsert(WaterDayEntity(today, next))
        }
    }
}
