package com.migraineguard.app.di

import android.content.Context
import com.migraineguard.app.feature.backup.BackupRepository
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.ai.GeminiFreeProvider
import com.migraineguard.app.core.ai.GroqFreeProvider
import com.migraineguard.app.core.ai.OfflineHealthBrain
import com.migraineguard.app.core.ai.OpenRouterFreeProvider
import com.migraineguard.app.core.data.db.AppDatabase
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.feature.auth.AuthRepository
import com.migraineguard.app.feature.auth.DefaultAuthRepository
import com.migraineguard.app.feature.body.WeightRepository
import com.migraineguard.app.feature.coach.AiCoachRepository
import com.migraineguard.app.feature.fitness.HealthConnectHealthDataRepository
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.HealthRefreshCoordinator
import com.migraineguard.app.feature.fitness.WorkoutRepository
import com.migraineguard.app.feature.insights.DefaultInsightsAnalyzer
import com.migraineguard.app.feature.insights.InsightsAnalyzer
import com.migraineguard.app.feature.meds.MedicationRepository
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.nutrition.AiNutritionAnalyzer
import com.migraineguard.app.feature.nutrition.NutritionAnalyzer
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.OpenMeteoWeatherRepository
import com.migraineguard.app.feature.weather.WeatherRepository
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.fitness.AccountHealthDataRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual composition root — simple AppContainer (no Hilt).
 */
class AppContainer(context: Context, val account: String = AccountStorage.LOCAL, sharedAuth: AuthRepository? = null) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext, account)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val preferencesRepository = UserPreferencesRepository(appContext, account)
    val backupRepository = BackupRepository(appContext, db, preferencesRepository)
    val driveSyncRepository = com.migraineguard.app.feature.settings.DriveSyncRepository(account, preferencesRepository, db, backupRepository, appScope)
    val authRepository: AuthRepository = sharedAuth ?: DefaultAuthRepository({ preferencesRepository })
    val migraineRepository = MigraineRepository(db.migraineDao())
    val waterIntakeRepository = WaterIntakeRepository(appContext, db.waterHistoryDao(), account == AccountStorage.LOCAL)
    val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(appContext)
    val healthDataRepository: HealthDataRepository = AccountHealthDataRepository(HealthConnectHealthDataRepository(appContext, stepSource = { preferencesRepository.preferences.first().stepSource }), preferencesRepository)
    val healthRefreshCoordinator = HealthRefreshCoordinator(healthDataRepository, appScope)
    val workoutRepository = WorkoutRepository(db.workoutDao())
    val weightRepository = WeightRepository(db.weightDao())
    val medicationRepository = MedicationRepository(db.medicationDao())

    fun saveCloudOnBackground() {
        if (driveSyncRepository.state.value.connected) appScope.launch {
            try { driveSyncRepository.backupNow() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* Visible backup status retains the error for the next foreground. */ }
        }
    }

    fun dispose() { driveSyncRepository.dispose(); appScope.cancel() }

    private val offlineHealthBrain = OfflineHealthBrain()

    val aiClient: FallbackAiClient = FallbackAiClient(
        providers = listOf(
            GeminiFreeProvider { preferencesRepository.getGeminiApiKey() },
            GroqFreeProvider { preferencesRepository.getGroqApiKey() },
            OpenRouterFreeProvider { preferencesRepository.getOpenRouterApiKey() },
            offlineHealthBrain,
        ),
        offlineBrain = offlineHealthBrain,
    )

    val nutritionAnalyzer: NutritionAnalyzer = AiNutritionAnalyzer(aiClient, db.nutritionDao(), appContext, GeminiFreeProvider { preferencesRepository.getGeminiApiKey() })
    val insightsAnalyzer: InsightsAnalyzer = DefaultInsightsAnalyzer(
        migraineRepository = migraineRepository,
        waterIntakeRepository = waterIntakeRepository,
        weatherRepository = weatherRepository,
        healthDataRepository = healthDataRepository,
        workoutRepository = workoutRepository,
        nutritionAnalyzer = nutritionAnalyzer,
        aiClient = aiClient,
    )

    val aiCoachRepository = AiCoachRepository(
        aiClient = aiClient,
        sessionDao = db.aiSessionDao(),
        preferencesRepository = preferencesRepository,
        migraineRepository = migraineRepository,
        waterIntakeRepository = waterIntakeRepository,
        healthDataRepository = healthDataRepository,
        workoutRepository = workoutRepository,
        nutritionAnalyzer = nutritionAnalyzer,
        weatherRepository = weatherRepository,
        medicationRepository = medicationRepository,
    )
}
