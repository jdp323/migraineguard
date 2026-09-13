package com.migraineguard.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.di.AppContainer
import com.migraineguard.app.feature.auth.SignInScreen
import com.migraineguard.app.feature.home.HomeScreen
import com.migraineguard.app.feature.insights.InsightsScreen
import com.migraineguard.app.feature.migraine.MigraineScreen
import com.migraineguard.app.feature.nutrition.NutritionScreen
import com.migraineguard.app.feature.onboarding.OnboardingScreen
import com.migraineguard.app.feature.settings.SettingsScreen
import com.migraineguard.app.feature.track.TrackScreen
import com.migraineguard.app.feature.water.WaterScreen
import kotlinx.coroutines.launch

@Composable
fun MigraineGuardNavHost(container: AppContainer) {
    val authState by container.authRepository.authState.collectAsStateWithLifecycle()
    val prefs by container.preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    val scope = rememberCoroutineScope()

    if (!prefs.onboardingDone) {
        OnboardingScreen(
            onFinished = { scope.launch { container.preferencesRepository.setOnboardingDone(true) } },
        )
        return
    }

    if (!authState.isSignedIn) {
        SignInScreen(authRepository = container.authRepository)
        return
    }

    val navController = rememberNavController()
    val bottomItems = listOf(
        BottomNavItem(BottomDest.Home, Icons.Filled.Home),
        BottomNavItem(BottomDest.Migraine, Icons.Outlined.Healing),
        BottomNavItem(BottomDest.Track, Icons.Filled.FitnessCenter),
        BottomNavItem(BottomDest.Food, Icons.Filled.LocalDining),
        BottomNavItem(BottomDest.Water, Icons.Filled.WaterDrop),
        BottomNavItem(BottomDest.Insights, Icons.Filled.Insights),
    )

    fun goBottom(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Routes.Settings) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.dest.route,
                            onClick = { goBottom(item.dest.route) },
                            icon = { Icon(item.icon, contentDescription = item.dest.label) },
                            label = { Text(item.dest.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    userName = authState.user?.displayName ?: "Friend",
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                    onLogMigraine = { goBottom(Routes.Migraine) },
                    onOpenWater = { goBottom(Routes.Water) },
                    onOpenFood = { goBottom(Routes.Food) },
                    onOpenFitness = { goBottom(Routes.Track) },
                    weatherRepository = container.weatherRepository,
                    healthDataRepository = container.healthDataRepository,
                    waterIntakeRepository = container.waterIntakeRepository,
                    preferencesRepository = container.preferencesRepository,
                    migraineRepository = container.migraineRepository,
                )
            }
            composable(Routes.Migraine) {
                MigraineScreen(repository = container.migraineRepository)
            }
            composable(Routes.Track) {
                TrackScreen(
                    healthDataRepository = container.healthDataRepository,
                    workoutRepository = container.workoutRepository,
                    preferencesRepository = container.preferencesRepository,
                )
            }
            composable(Routes.Food) {
                NutritionScreen(nutritionAnalyzer = container.nutritionAnalyzer)
            }
            composable(Routes.Water) {
                WaterScreen(
                    repository = container.waterIntakeRepository,
                    preferencesRepository = container.preferencesRepository,
                )
            }
            composable(Routes.Insights) {
                InsightsScreen(
                    insightsAnalyzer = container.insightsAnalyzer,
                    aiClient = container.aiClient,
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    preferencesRepository = container.preferencesRepository,
                    driveSyncRepository = container.driveSyncRepository,
                    healthDataRepository = container.healthDataRepository,
                    onSignOut = { container.authRepository.signOut() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private data class BottomNavItem(
    val dest: BottomDest,
    val icon: ImageVector,
)
