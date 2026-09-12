package com.migraineguard.app.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.domain.WaterUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("user_settings")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val waterGoalCups: Int = 8,
    val waterUnit: WaterUnit = WaterUnit.CUPS,
    val notificationsEnabled: Boolean = true,
    val waterReminders: Boolean = true,
    val migraineReminders: Boolean = false,
    val driveSyncEnabled: Boolean = false,
    val onboardingDone: Boolean = false,
    val useCelsius: Boolean = true,
)

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val waterGoalKey = intPreferencesKey("water_goal_cups")
    private val waterUnitKey = stringPreferencesKey("water_unit")
    private val notifKey = booleanPreferencesKey("notifications")
    private val waterRemindKey = booleanPreferencesKey("water_reminders")
    private val migraineRemindKey = booleanPreferencesKey("migraine_reminders")
    private val driveKey = booleanPreferencesKey("drive_sync")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val celsiusKey = booleanPreferencesKey("use_celsius")

    val preferences: Flow<UserPreferences> = dataStore.data.map { p ->
        UserPreferences(
            themeMode = runCatching { ThemeMode.valueOf(p[themeKey] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = p[dynamicKey] ?: false,
            waterGoalCups = p[waterGoalKey] ?: 8,
            waterUnit = runCatching { WaterUnit.valueOf(p[waterUnitKey] ?: WaterUnit.CUPS.name) }
                .getOrDefault(WaterUnit.CUPS),
            notificationsEnabled = p[notifKey] ?: true,
            waterReminders = p[waterRemindKey] ?: true,
            migraineReminders = p[migraineRemindKey] ?: false,
            driveSyncEnabled = p[driveKey] ?: false,
            onboardingDone = p[onboardingKey] ?: false,
            useCelsius = p[celsiusKey] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[dynamicKey] = enabled }
    }

    suspend fun setWaterGoalCups(goal: Int) {
        dataStore.edit { it[waterGoalKey] = goal.coerceIn(1, 20) }
    }

    suspend fun setWaterUnit(unit: WaterUnit) {
        dataStore.edit { it[waterUnitKey] = unit.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[notifKey] = enabled }
    }

    suspend fun setWaterReminders(enabled: Boolean) {
        dataStore.edit { it[waterRemindKey] = enabled }
    }

    suspend fun setMigraineReminders(enabled: Boolean) {
        dataStore.edit { it[migraineRemindKey] = enabled }
    }

    suspend fun setDriveSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[driveKey] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[onboardingKey] = done }
    }

    suspend fun setUseCelsius(use: Boolean) {
        dataStore.edit { it[celsiusKey] = use }
    }
}
