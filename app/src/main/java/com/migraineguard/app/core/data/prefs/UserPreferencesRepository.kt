package com.migraineguard.app.core.data.prefs

import kotlinx.serialization.encodeToString
import com.migraineguard.app.BuildConfig
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.migraineguard.app.core.security.AccountStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

private object AccountPreferenceStores {
    private val stores = mutableMapOf<String, DataStore<Preferences>>()
    @Synchronized fun get(context: Context, account: String): DataStore<Preferences> {
        val file = context.applicationContext.preferencesDataStoreFile(AccountStorage.settingsName(account))
        return stores.getOrPut(file.absolutePath) {
            PreferenceDataStoreFactory.create(scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)) { file }
        }
    }
}

@kotlinx.serialization.Serializable
data class UserPreferences(
    val reduceTransparency: Boolean = false,
    val trainerProfile: com.migraineguard.app.feature.fitness.TrainerProfile = com.migraineguard.app.feature.fitness.TrainerProfile(),
    val trainingPlans: List<com.migraineguard.app.feature.fitness.TrainingWeek> = emptyList(),
    val aiConnections: Map<String, com.migraineguard.app.core.ai.AiConnection> = emptyMap(),
    val preferredAiProvider: String = "auto",
    val weeklyWorkoutGoal: Int = 3,
    val movementReminders: Boolean = false,
    val movementReminderMinutes: Int = 60,
    val bodyPhotoDriveBackup: Boolean = false,
    val foodReminders: Boolean = false,
    val waterReminderMinutes: Int = 120,
    val foodReminderMinutes: Int = 240,
    val medicationReminderMinutes: Int = 360,
    val migraineReminderMinutes: Int = 360,
    val reminderStartHour: Int = 8,
    val reminderEndHour: Int = 22,
    val updateChecksEnabled: Boolean = true,
    val modifiedAtMillis: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val waterGoalCups: Int = 8,
    val waterUnit: WaterUnit = WaterUnit.CUPS,
    val dailyStepGoal: Int = 8000,
    val stepSource: String = "combined",
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
    val deviceHealthEnabled: Boolean = false,
)

class UserPreferencesRepository(context: Context, account: String = AccountStorage.LOCAL) {
    private val dataStore = AccountPreferenceStores.get(context, account)
    private val apiKeyVault = ApiKeyVault()

    private val reduceTransparencyKey = booleanPreferencesKey("reduce_transparency")
    private val trainerProfileKey = stringPreferencesKey("trainer_profile_v1")
    private val trainingPlansKey = stringPreferencesKey("training_plans_v1")
    private val trainerJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val connectionsKey = stringPreferencesKey("ai_connections_v1")
    private val preferredAiKey = stringPreferencesKey("preferred_ai_provider")
    private val weeklyWorkoutGoalKey = intPreferencesKey("weekly_workout_goal")
    private val waterReminderMinutesKey = intPreferencesKey("water_reminder_minutes")
    private val foodReminderMinutesKey = intPreferencesKey("food_reminder_minutes")
    private val medicationReminderMinutesKey = intPreferencesKey("medication_reminder_minutes")
    private val migraineReminderMinutesKey = intPreferencesKey("migraine_reminder_minutes")
    private val reminderStartHourKey = intPreferencesKey("reminder_start_hour")
    private val reminderEndHourKey = intPreferencesKey("reminder_end_hour")
    private val movementRemindersKey = booleanPreferencesKey("movement_reminders")
    private val movementMinutesKey = intPreferencesKey("movement_reminder_minutes")
    private val bodyPhotoDriveKey = booleanPreferencesKey("body_photo_drive_backup")
    private val foodRemindersKey = booleanPreferencesKey("food_reminders")
    private val updateChecksKey = booleanPreferencesKey("update_checks")
    private val modifiedKey = androidx.datastore.preferences.core.longPreferencesKey("modified_at_millis")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val waterGoalKey = intPreferencesKey("water_goal_cups")
    private val waterUnitKey = stringPreferencesKey("water_unit")
    private val stepSourceKey = stringPreferencesKey("step_source")
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
    private val deviceHealthKey = booleanPreferencesKey("device_health_enabled")
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
        // Retire only the earlier shipped default. Deliberate custom clients remain intact.
        val legacyClient = "555526836064-aria393bjpcgr17j6fatl0nbeqtvqri6.apps.googleusercontent.com"
        if (p[googleClientKey] == legacyClient && BuildConfig.GOOGLE_WEB_CLIENT_ID != legacyClient) {
            dataStore.edit { values ->
                if (values[googleClientKey] == legacyClient) values.remove(googleClientKey)
            }
            return@transform
        }
        val apiKeys = listOf(geminiKey, groqKey, openRouterKey, connectionsKey)
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
            reduceTransparency = p[reduceTransparencyKey] ?: false,
            trainerProfile = p[trainerProfileKey]?.let { trainerJson.decodeFromString<com.migraineguard.app.feature.fitness.TrainerProfile>(it) } ?: com.migraineguard.app.feature.fitness.TrainerProfile(),
            trainingPlans = p[trainingPlansKey]?.let { trainerJson.decodeFromString<List<com.migraineguard.app.feature.fitness.TrainingWeek>>(it) } ?: emptyList(),
            aiConnections = decodeConnections(apiKeyVault.decrypt(p[connectionsKey].orEmpty())),
            preferredAiProvider = p[preferredAiKey] ?: "auto",
            movementReminders = p[movementRemindersKey] ?: false,
            movementReminderMinutes = p[movementMinutesKey] ?: 60,
            bodyPhotoDriveBackup = p[bodyPhotoDriveKey] ?: false,
            foodReminders = p[foodRemindersKey] ?: false,
            updateChecksEnabled = p[updateChecksKey] ?: true,
            weeklyWorkoutGoal = p[weeklyWorkoutGoalKey] ?: 3,
            waterReminderMinutes = p[waterReminderMinutesKey] ?: 120,
            foodReminderMinutes = p[foodReminderMinutesKey] ?: 240,
            medicationReminderMinutes = p[medicationReminderMinutesKey] ?: 360,
            migraineReminderMinutes = p[migraineReminderMinutesKey] ?: 360,
            reminderStartHour = p[reminderStartHourKey] ?: 8,
            reminderEndHour = p[reminderEndHourKey] ?: 22,
            modifiedAtMillis = p[modifiedKey] ?: 0L,
            themeMode = runCatching { ThemeMode.valueOf(p[themeKey] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = p[dynamicKey] ?: false,
            waterGoalCups = p[waterGoalKey] ?: 8,
            waterUnit = runCatching { WaterUnit.valueOf(p[waterUnitKey] ?: WaterUnit.CUPS.name) }
                .getOrDefault(WaterUnit.CUPS),
            dailyStepGoal = p[stepGoalKey] ?: 8000,
            stepSource = p[stepSourceKey] ?: "combined",
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
            deviceHealthEnabled = p[deviceHealthKey] ?: false,
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

    private suspend fun editUser(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { block(it); it[modifiedKey] = maxOf(System.currentTimeMillis(), (it[modifiedKey] ?: 0L) + 1L) }
    }

    suspend fun getGoogleWebClientId(): String =
        preferences.first().googleWebClientId

    suspend fun setReduceTransparency(enabled: Boolean) { editUser { it[reduceTransparencyKey] = enabled } }
    suspend fun saveTrainerProfile(profile: com.migraineguard.app.feature.fitness.TrainerProfile) { profile.validate(); editUser { it[trainerProfileKey] = trainerJson.encodeToString(profile) } }
    suspend fun saveTrainingPlan(plan: com.migraineguard.app.feature.fitness.TrainingWeek) {
        plan.validate()
        editUser { p ->
            val old = p[trainingPlansKey]?.let { trainerJson.decodeFromString<List<com.migraineguard.app.feature.fitness.TrainingWeek>>(it) }.orEmpty()
            p[trainingPlansKey] = trainerJson.encodeToString((listOf(plan) + old.filter { it.id != plan.id }).take(5))
        }
    }
    suspend fun deleteTrainingPlan(id: String) { editUser { p ->
        val plans = p[trainingPlansKey]?.let { trainerJson.decodeFromString<List<com.migraineguard.app.feature.fitness.TrainingWeek>>(it) }.orEmpty()
        p[trainingPlansKey] = trainerJson.encodeToString(plans.filter { it.id != id })
    } }
    suspend fun setThemeMode(mode: ThemeMode) {
        editUser { it[themeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        editUser { it[dynamicKey] = enabled }
    }

    suspend fun setWaterGoalCups(goal: Int) {
        editUser { it[waterGoalKey] = goal.coerceIn(1, 20) }
    }

    suspend fun setWaterUnit(unit: WaterUnit) {
        editUser { it[waterUnitKey] = unit.name }
    }

    suspend fun setStepSource(source: String) {
        require(source in setOf("samsung", "combined"))
        editUser { it[stepSourceKey] = source }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        editUser { it[stepGoalKey] = goal.coerceIn(1000, 30000) }
    }

    suspend fun setSleepGoalHours(hours: Float) {
        editUser { it[sleepGoalKey] = hours.coerceIn(4f, 12f) }
    }

    suspend fun setWeightGoalKg(kg: Float?) {
        editUser {
            if (kg == null) it.remove(weightGoalKey) else it[weightGoalKey] = kg
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        editUser { it[notifKey] = enabled }
    }

    suspend fun setWaterReminders(enabled: Boolean) {
        editUser { it[waterRemindKey] = enabled }
    }

    suspend fun setMigraineReminders(enabled: Boolean) {
        editUser { it[migraineRemindKey] = enabled }
    }

    suspend fun setMedicationReminders(enabled: Boolean) {
        editUser { it[medRemindKey] = enabled }
    }

    suspend fun setDriveSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[driveKey] = enabled }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        editUser { it[onboardingKey] = done }
    }

    suspend fun setUseCelsius(use: Boolean) {
        editUser { it[celsiusKey] = use }
    }

    suspend fun setGoogleWebClientId(id: String) {
        editUser { it[googleClientKey] = id.trim() }
    }

    suspend fun setEnergyLevel(level: Int, dateIso: String) {
        editUser {
            it[energyKey] = level.coerceIn(1, 5)
            it[energyDateKey] = dateIso
        }
    }

    suspend fun saveBodyProfile(profile: BodyProfile) {
        editUser {
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
        editUser { it[geminiKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearGeminiApiKey() {
        editUser { it[geminiKey] = "" }
    }

    suspend fun setGroqApiKey(key: String) {
        editUser { it[groqKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearGroqApiKey() {
        editUser { it[groqKey] = "" }
    }

    suspend fun setOpenRouterApiKey(key: String) {
        editUser { it[openRouterKey] = apiKeyVault.encrypt(key.trim()) }
    }

    suspend fun clearOpenRouterApiKey() {
        editUser { it[openRouterKey] = "" }
    }

    suspend fun clearAllAiKeys() {
        editUser {
            it[geminiKey] = ""
            it[groqKey] = ""
            it[openRouterKey] = ""
            it[connectionsKey] = ""
            it[preferredAiKey] = "auto"
        }
    }

    suspend fun setSetupComplete(done: Boolean) {
        editUser {
            it[setupCompleteKey] = done
            if (done) it[onboardingKey] = true
        }
    }

    suspend fun setHealthConnectSkipped(skipped: Boolean) {
        dataStore.edit { it[hcSkippedKey] = skipped }
    }

    suspend fun setDeviceHealthEnabled(enabled: Boolean) {
        dataStore.edit { it[deviceHealthKey] = enabled }
    }

    /** Restores account data atomically; OS permissions and device consent never come from Drive. */
    suspend fun restoreCloudSettings(value: UserPreferences): Boolean {
        var applied = false
        dataStore.edit { p ->
            if ((p[modifiedKey] ?: 0L) > value.modifiedAtMillis) return@edit
            p[reduceTransparencyKey] = value.reduceTransparency
            p[trainerProfileKey] = trainerJson.encodeToString(value.trainerProfile)
            p[trainingPlansKey] = trainerJson.encodeToString(value.trainingPlans)
            p[themeKey] = value.themeMode.name; p[dynamicKey] = value.dynamicColor
            p[waterGoalKey] = value.waterGoalCups; p[waterUnitKey] = value.waterUnit.name
            p[stepSourceKey] = value.stepSource; p[stepGoalKey] = value.dailyStepGoal; p[sleepGoalKey] = value.sleepGoalHours
            value.weightGoalKg?.let { p[weightGoalKey] = it } ?: p.remove(weightGoalKey)
            p[notifKey] = value.notificationsEnabled; p[waterRemindKey] = value.waterReminders
            p[migraineRemindKey] = value.migraineReminders; p[medRemindKey] = value.medicationReminders
            p[celsiusKey] = value.useCelsius; p[energyKey] = value.energyLevel; p[energyDateKey] = value.energyDateIso
            val body = value.bodyProfile
            p[profileNameKey] = body.displayName; p[profileDobKey] = body.dateOfBirthIso
            body.sex?.let { p[profileSexKey] = it.name } ?: p.remove(profileSexKey)
            body.heightCm?.let { p[profileHeightKey] = it } ?: p.remove(profileHeightKey)
            body.weightKg?.let { p[profileWeightKey] = it } ?: p.remove(profileWeightKey)
            body.activityLevel?.let { p[profileActivityKey] = it.name } ?: p.remove(profileActivityKey)
            p[profileMetricKey] = body.useMetric; p[profileCompleteKey] = body.profileComplete
            p[setupCompleteKey] = value.setupComplete || p[setupCompleteKey] == true
            p[onboardingKey] = value.onboardingDone || value.setupComplete
            p[geminiKey] = apiKeyVault.encrypt(value.geminiApiKey)
            p[groqKey] = apiKeyVault.encrypt(value.groqApiKey)
            p[openRouterKey] = apiKeyVault.encrypt(value.openRouterApiKey)
            p[connectionsKey] = apiKeyVault.encrypt(kotlinx.serialization.json.Json.encodeToString(value.aiConnections))
            p[preferredAiKey] = value.preferredAiProvider
            p[movementRemindersKey] = value.movementReminders; p[movementMinutesKey] = value.movementReminderMinutes
            p[bodyPhotoDriveKey] = value.bodyPhotoDriveBackup
            p[foodRemindersKey] = value.foodReminders; p[updateChecksKey] = value.updateChecksEnabled
            p[weeklyWorkoutGoalKey] = value.weeklyWorkoutGoal
            p[waterReminderMinutesKey] = value.waterReminderMinutes
            p[foodReminderMinutesKey] = value.foodReminderMinutes
            p[medicationReminderMinutesKey] = value.medicationReminderMinutes
            p[migraineReminderMinutesKey] = value.migraineReminderMinutes
            p[reminderStartHourKey] = value.reminderStartHour
            p[reminderEndHourKey] = value.reminderEndHour
            p[modifiedKey] = value.modifiedAtMillis
            applied = true
        }
        return applied
    }

    suspend fun getGeminiApiKey(): String = preferences.first().geminiApiKey
    suspend fun getGroqApiKey(): String = preferences.first().groqApiKey
    suspend fun getOpenRouterApiKey(): String = preferences.first().openRouterApiKey
    private fun decodeConnections(raw: String): Map<String, com.migraineguard.app.core.ai.AiConnection> =
        if (raw.isBlank()) emptyMap() else kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString(raw)

    suspend fun saveAiConnection(id: String, key: String, model: String) {
        require(com.migraineguard.app.core.ai.AiCatalog.service(id) != null && key.length <= 4096 && model.length <= 160)
        editUser { p ->
            val values = decodeConnections(apiKeyVault.decrypt(p[connectionsKey].orEmpty())).toMutableMap()
            values[id] = com.migraineguard.app.core.ai.AiConnection(key.trim(), model.trim())
            p[connectionsKey] = apiKeyVault.encrypt(kotlinx.serialization.json.Json.encodeToString(values))
            when (id) { "gemini" -> p[geminiKey] = apiKeyVault.encrypt(key.trim()); "groq" -> p[groqKey] = apiKeyVault.encrypt(key.trim()); "openrouter" -> p[openRouterKey] = apiKeyVault.encrypt(key.trim()) }
            p[preferredAiKey] = id
        }
    }
    suspend fun setPreferredAiProvider(id: String) { require(id in listOf("auto", "offline") || com.migraineguard.app.core.ai.AiCatalog.service(id) != null); editUser { it[preferredAiKey] = id } }
    suspend fun setWeeklyWorkoutGoal(days: Int) { editUser { it[weeklyWorkoutGoalKey] = days.coerceIn(1, 7) } }
    suspend fun setMovementReminders(enabled: Boolean, minutes: Int) {
        require(minutes in 15..1440)
        editUser { it[movementRemindersKey] = enabled; it[movementMinutesKey] = minutes }
    }
    suspend fun setBodyPhotoDriveBackup(enabled: Boolean) { editUser { it[bodyPhotoDriveKey] = enabled } }
    suspend fun setUpdateChecks(enabled: Boolean) { editUser { it[updateChecksKey] = enabled } }
    /** Updates only supplied fields, avoiding stale UI snapshots overwriting another reminder edit. */
    suspend fun setReminderOptions(food: Boolean? = null, waterMinutes: Int? = null, foodMinutes: Int? = null, medMinutes: Int? = null, migraineMinutes: Int? = null, startHour: Int? = null, endHour: Int? = null) {
        require(listOfNotNull(waterMinutes, foodMinutes, medMinutes, migraineMinutes).all { it in 15..1440 })
        editUser { p ->
            val start = startHour ?: p[reminderStartHourKey] ?: 8
            val end = endHour ?: p[reminderEndHourKey] ?: 22
            require(start in 0..23 && end in 0..23 && start != end)
            food?.let { p[foodRemindersKey] = it }
            waterMinutes?.let { p[waterReminderMinutesKey] = it }
            foodMinutes?.let { p[foodReminderMinutesKey] = it }
            medMinutes?.let { p[medicationReminderMinutesKey] = it }
            migraineMinutes?.let { p[migraineReminderMinutesKey] = it }
            startHour?.let { p[reminderStartHourKey] = it }
            endHour?.let { p[reminderEndHourKey] = it }
        }
    }
}
