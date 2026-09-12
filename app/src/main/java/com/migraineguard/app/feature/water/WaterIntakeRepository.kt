package com.migraineguard.app.feature.water

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.waterDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "water_intake",
)

/**
 * Cup counter with DataStore persistence (resets logical day when date changes).
 */
class WaterIntakeRepository(context: Context) {
    private val dataStore = context.applicationContext.waterDataStore

    private val cupsKey = intPreferencesKey("cups")
    private val dateKey = stringPreferencesKey("date")

    val cupsToday: Flow<Int> = dataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        if (prefs[dateKey] != today) 0 else prefs[cupsKey] ?: 0
    }

    suspend fun addCup() {
        mutate(delta = 1)
    }

    suspend fun removeCup() {
        mutate(delta = -1)
    }

    private suspend fun mutate(delta: Int) {
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val current = if (prefs[dateKey] != today) 0 else prefs[cupsKey] ?: 0
            prefs[dateKey] = today
            prefs[cupsKey] = (current + delta).coerceAtLeast(0)
        }
    }
}
