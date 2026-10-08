package com.migraineguard.app.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.migraineguard.app.core.domain.ActivityLevel
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.SexGender
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.domain.WaterUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import com.migraineguard.app.core.security.ApiKeyVault

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("user_settings")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val waterGoalCups: Int = 8,
    val waterUnit: WaterUnit = WaterUnit.CUPS,
    val dailyStepGoal: Int = 8000,
    val sleepGoalHours: Float = 8f,
    val weightGoalKg: Float? = null,
    val notificationsEnabled: Boolean = true,
    val waterReminders: Boolean = true,
    val migraineReminders: Boolean = false,
    val medicationReminders: Boolean = false,
    val driveSyncEnabled: Boolean = false,
    val onboardingDone: Boolean = false,
    val useCelsius: Boolean = true,
    val googleWebClientId: String = "",
    val energyLevel: Int = 3,
    val energyDateIso: String = "",
    val bodyProfile: BodyProfile = BodyProfile(),
    val geminiApiKey: String = "",
    val groqApiKey: String = "",
    val openRouterApiKey: String = "",
    val setupComplete: Boolean = false,
    val healthConnectSkipped: Boolean = false,
)

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore
    private val apiKeyVault = ApiKeyVault()

    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val waterGoalKey = intPreferencesKey("water_goal_cups")
    private val waterUnitKey = stringPreferencesKey("water_unit")
    private val stepGoalKey = intPreferencesKey("daily_step_goal")
    private val sleepGoalKey = floatPreferencesKey("sleep_goal_hours")
    private val weightGoalKey = floatPreferencesKey("weight_goal_kg")
    private val notifKey = booleanPreferencesKey("notifications")
    private val waterRemindKey = booleanPreferencesKey("water_reminders")
    private val migraineRemindKey = booleanPreferencesKey("migraine_reminders")
    private val medRemindKey = booleanPreferencesKey("medication_reminders")
    private val driveKey = booleanPreferencesKey("drive_sync")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val celsiusKey = booleanPreferencesKey("use_celsius")
    private val googleClientKey = stringPreferencesKey("google_web_client_id")
    private val energyKey = intPreferencesKey("energy_level")
    private val energyDateKey = stringPreferencesKey("energy_date_iso")
    private val geminiKey = stringPreferencesKey("gemini_api_key")
    private val groqKey = stringPreferencesKey("groq_api_key")
    private val openRouterKey = stringPreferencesKey("openrouter_api_key")
    private val setupCompleteKey = booleanPreferencesKey("setup_complete")
    private val hcSkippedKey = booleanPreferencesKey("health_connect_skipped")

    private val profileNameKey = stringPreferencesKey("profile_display_name")
    private val profileDobKey = stringPreferencesKey("profile_dob")
    private val profileSexKey = stringPreferencesKey("profile_sex")
    private val profileHeightKey = floatPreferencesKey("profile_height_cm")
    private val profileWeightKey = floatPreferencesKey("profile_weight_kg")
    private val profileActivityKey = stringPreferencesKey("profile_activity")
    private val profileMetricKey = booleanPreferencesKey("profile_use_metric")
    private val profileCompleteKey = booleanPreferencesKey("profile_complete")

    val preferences: Flow<UserPreferences> = dataStore.data.transform { p ->
        val apiKeys = listOf(geminiKey, groqKey, openRouterKey)
        if (apiKeys.any { !p[it].isNullOrBlank() && !p[it]!!.startsWith(ApiKeyVault.PREFIX) }) {
            dataStore.edit { values ->
                apiKeys.forEach { key ->
                    val value = values[key].orEmpty()
                    if (value.isNotBlank() && !value.startsWith(ApiKeyVault.PREFIX)) {
                        values[key] = apiKeyVault.encrypt(value)
                    }
                }
            }
            return@transform
        }
        emit(UserPreferences(
            themeMode = runCatching { ThemeMode.valueOf(p[themeKey] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = p[dynamicKey] ?: false,
            waterGoalCups = p[waterGoalKey] ?: 8,
            waterUnit = runCatching { WaterUnit.valueOf(p[waterUnitKey] ?: WaterUnit.CUPS.name) }
                .getOrDefault(WaterUnit.CUPS),
            dailyStepGoal = p[stepGoalKey] ?: 8000,
            sleepGoalHours = p[sleepGoalKey] ?: 8f,
            weightGoalKg = p[weightGoalKey],
            notificationsEnabled = p[notifKey] ?: true,
            waterReminders = p[waterRemindKey] ?: true,
            migraineReminders = p[migraineRemindKey] ?: false,
            medicationReminders = p[medRemindKey] ?: false,
            driveSyncEnabled = p[driveKey] ?: false,
            onboardingDone = p[onboardingKey] ?: false,
            useCelsius = p[celsiusKey] ?: true,
            googleWebClientId = p[googleClientKey] ?: "",
            energyLevel = p[energyKey] ?: 3,
            energyDateIso = p[energyDateKey] ?: "",
            geminiApiKey = apiKeyVault.decrypt(p[geminiKey].orEmpty()),
            groqApiKey = apiKeyVault.decrypt(p[groqKey].orEmpty()),
            openRouterApiKey = apiKeyVault.decrypt(p[openRouterKey].orEmpty()),
            // Migrate v1.5 users who already finished onboarding+profile
            setupComplete = p[setupCompleteKey]
                ?: ((p[onboardingKey] == true) && (p[profileCompleteKey] == true)),
            healthConnectSkipped = p[hcSkippedKey] ?: false,
            bodyProfile = BodyProfile(
                displayName = p[profileNameKey] ?: "",
                dateOfBirthIso = p[profileDobKey] ?: "",
                sex = p[profileSexKey]?.let { runCatching { SexGender.valueOf(it) }.getOrNull() },
                heightCm = p[profileHeightKey],
                weightKg = p[profileWeightKey],
                activityLevel = p[profileActivityKey]?.let {
                    runCatching { ActivityLevel.valueOf(it) }.getOrNull()
                },
                useMetric = p[profileMetricKey] ?: true,
                profileComplete = p[profileCompleteKey] ?: false,
            ),
        ))
    }

    suspend fun getGoogleWebClientId(): String =
        preferences.first().googleWebClientId

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

    suspend fun setDailyStepGoal(goal: Int) {
        dataStore.edit { it[stepGoalKey] = goal.coerceIn(1000, 30000) }
    }

    suspend fun setSleepGoalHours(hours: Float) {
        dataStore.edit { it[sleepGoalKey] = hours.coerceIn(4f, 12f) }
    }

    suspend fun setWeightGoalKg(kg: Float?) {
        dataStore.edit {
            if (kg == null) it.remove(weightGoalKey) else it[weightGoalKey] = kg
        }
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

    suspend fun setMedicationReminders(enabled: Boolean) {
        dataStore.edit { it[medRemindKey] = enabled }
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

    suspend fun setGoogleWebClientId(id: String) {
        dataStore.edit { it[googleClientKey] = id.trim() }
    }

    suspend fun setEnergyLevel(level: Int, dateIso: String) {
        dataStore.edit {
            it[energyKey] = level.coerceIn(1, 5)
            it[energyDateKey] = dateIso
        }
    }

    suspend fun saveBodyProfile(profile: BodyProfile) {
        dataStore.edit {
            it[profileNameKey] = profile.displayName
            it[profileDobKey] = profile.dateOfBirthIso
            profile.sex?.let { s -> it[profileSexKey] = s.name } ?: it.remove(profileSexKey)
            profile.heightCm?.let { h -> it[profileHeightKey] = h } ?: it.remove(profileHeightKey)
            profile.weightKg?.let { w -> it[profileWeightKey] = w } ?: it.remove(profileWeightKey)
            profile.activityLevel?.let { a -> it[profileActivityKey] = a.name }
                ?: it.remove(profileActivityKey)
            it[profileMetricKey] = profile.useMetric
            it[profileCompleteKey] = profile.profileComplete
        }
    }

    suspend fun seedProfileNameIfBlank(name: String) {
        dataStore.edit {
            val current = it[profileNameKey]
            if (current.isNullOrBlank() && name.isNotBlank()) {
                it[profileNameKey] = name
            }
        }
    }

    suspend fun setGeminiApiKey(key: String) {
        dataStore.edit { it[geminiKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearGeminiApiKey() {
        dataStore.edit { it[geminiKey] = "" }
    }

    suspend fun setGroqApiKey(key: String) {
        dataStore.edit { it[groqKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearGroqApiKey() {
        dataStore.edit { it[groqKey] = "" }
    }

    suspend fun setOpenRouterApiKey(key: String) {
        dataStore.edit { it[openRouterKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearOpenRouterApiKey() {
        dataStore.edit { it[openRouterKey] = "" }
    }

    suspend fun clearAllAiKeys() {
        dataStore.edit {
            it[geminiKey] = ""
            it[groqKey] = ""
            it[openRouterKey] = ""
        }
    }

    suspend fun setSetupComplete(done: Boolean) {
        dataStore.edit {
            it[setupCompleteKey] = done
            if (done) it[onboardingKey] = true
        }
    }

    suspend fun setHealthConnectSkipped(skipped: Boolean) {
        dataStore.edit { it[hcSkippedKey] = skipped }
    }

    suspend fun getGeminiApiKey(): String = preferences.first().geminiApiKey
    suspend fun getGroqApiKey(): String = preferences.first().groqApiKey
    suspend fun getOpenRouterApiKey(): String = preferences.first().openRouterApiKey
}
