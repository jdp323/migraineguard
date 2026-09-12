package com.migraineguard.app.di

import android.content.Context
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.ai.GeminiFreeProvider
import com.migraineguard.app.core.ai.GroqFreeProvider
import com.migraineguard.app.core.ai.OpenRouterFreeProvider
import com.migraineguard.app.feature.auth.AuthRepository
import com.migraineguard.app.feature.auth.FakeAuthRepository
import com.migraineguard.app.feature.fitness.FakeHealthDataRepository
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.insights.FakeInsightsAnalyzer
import com.migraineguard.app.feature.insights.InsightsAnalyzer
import com.migraineguard.app.feature.nutrition.FakeNutritionAnalyzer
import com.migraineguard.app.feature.nutrition.NutritionAnalyzer
import com.migraineguard.app.feature.settings.DriveSyncRepository
import com.migraineguard.app.feature.settings.FakeDriveSyncRepository
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.FakeWeatherRepository
import com.migraineguard.app.feature.weather.WeatherRepository

/**
 * Manual composition root. Swap fake implementations for real ones as features mature.
 * Prefer constructor injection into ViewModels / screens via this container.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val authRepository: AuthRepository = FakeAuthRepository()
    val healthDataRepository: HealthDataRepository = FakeHealthDataRepository()
    val nutritionAnalyzer: NutritionAnalyzer = FakeNutritionAnalyzer()
    val waterIntakeRepository: WaterIntakeRepository = WaterIntakeRepository(appContext)
    val weatherRepository: WeatherRepository = FakeWeatherRepository()
    val insightsAnalyzer: InsightsAnalyzer = FakeInsightsAnalyzer()
    val driveSyncRepository: DriveSyncRepository = FakeDriveSyncRepository()

    /** Tries Gemini → Groq → OpenRouter stubs in order when one "fails". */
    val aiClient: FallbackAiClient = FallbackAiClient(
        providers = listOf(
            GeminiFreeProvider(),
            GroqFreeProvider(),
            OpenRouterFreeProvider(),
        )
    )
}
