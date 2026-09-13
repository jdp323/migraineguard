package com.migraineguard.app.di

import android.content.Context
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.ai.GeminiFreeProvider
import com.migraineguard.app.core.ai.GroqFreeProvider
import com.migraineguard.app.core.ai.OpenRouterFreeProvider
import com.migraineguard.app.core.data.db.AppDatabase
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.feature.auth.AuthRepository
import com.migraineguard.app.feature.auth.DefaultAuthRepository
import com.migraineguard.app.feature.fitness.HealthConnectHealthDataRepository
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.WorkoutRepository
import com.migraineguard.app.feature.insights.DefaultInsightsAnalyzer
import com.migraineguard.app.feature.insights.InsightsAnalyzer
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.nutrition.AiNutritionAnalyzer
import com.migraineguard.app.feature.nutrition.NutritionAnalyzer
import com.migraineguard.app.feature.settings.DriveSyncRepository
import com.migraineguard.app.feature.settings.DriveSyncRepositoryImpl
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.OpenMeteoWeatherRepository
import com.migraineguard.app.feature.weather.WeatherRepository

/**
 * Manual composition root — simple AppContainer (no Hilt).
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext)

    val preferencesRepository = UserPreferencesRepository(appContext)
    val authRepository: AuthRepository = DefaultAuthRepository()
    val migraineRepository = MigraineRepository(db.migraineDao())
    val waterIntakeRepository = WaterIntakeRepository(appContext, db.waterHistoryDao())
    val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(appContext)
    val healthDataRepository: HealthDataRepository = HealthConnectHealthDataRepository(appContext)
    val workoutRepository = WorkoutRepository(db.workoutDao())

    val aiClient: FallbackAiClient = FallbackAiClient(
        providers = listOf(
            GeminiFreeProvider(),
            GroqFreeProvider(),
            OpenRouterFreeProvider(),
        ),
    )

    val nutritionAnalyzer: NutritionAnalyzer = AiNutritionAnalyzer(aiClient, db.nutritionDao())
    val insightsAnalyzer: InsightsAnalyzer = DefaultInsightsAnalyzer(
        migraineRepository = migraineRepository,
        waterIntakeRepository = waterIntakeRepository,
        weatherRepository = weatherRepository,
        healthDataRepository = healthDataRepository,
        workoutRepository = workoutRepository,
        nutritionAnalyzer = nutritionAnalyzer,
        aiClient = aiClient,
    )
    val driveSyncRepository: DriveSyncRepository = DriveSyncRepositoryImpl(preferencesRepository)
}
