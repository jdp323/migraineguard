package com.migraineguard.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.migraineguard.app.core.ai.AiPersona
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.di.AppContainer
import com.migraineguard.app.feature.auth.SignInScreen
import com.migraineguard.app.feature.body.BodyMetricsScreen
import com.migraineguard.app.feature.coach.CoachScreen
import com.migraineguard.app.feature.gemini.ConnectGeminiScreen
import com.migraineguard.app.feature.home.HomeScreen
import com.migraineguard.app.feature.insights.InsightsScreen
import com.migraineguard.app.feature.meds.MedicationsScreen
import com.migraineguard.app.feature.migraine.MigraineScreen
import com.migraineguard.app.feature.nutrition.NutritionScreen
import com.migraineguard.app.feature.profile.ProfileSetupScreen
import com.migraineguard.app.feature.settings.SettingsScreen
import com.migraineguard.app.feature.setup.SetupWizardScreen
import com.migraineguard.app.feature.track.TrackScreen
import com.migraineguard.app.feature.water.WaterScreen
import kotlinx.coroutines.launch
import com.migraineguard.app.feature.settings.DriveBackupScreen
import com.migraineguard.app.feature.settings.DriveBackupCard
import com.migraineguard.app.feature.setup.HealthConnectStep
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun MigraineGuardNavHost(container: AppContainer) {
    val authState by container.authRepository.authState.collectAsStateWithLifecycle()
    val loadedPrefs by container.preferencesRepository.preferences.collectAsStateWithLifecycle<UserPreferences?>(null)
    val prefs = loadedPrefs ?: return
    val scope = rememberCoroutineScope()
    var forceProfileEdit by remember { mutableStateOf(false) }
    var coachPersona by remember { mutableStateOf(AiPersona.COACH) }
    var forceConnectGemini by remember { mutableStateOf(false) }

    var driveChecked by remember { mutableStateOf(false) }
    var healthChecked by remember { mutableStateOf(false) }
    var openDrive by remember { mutableStateOf(false) }
    if (authState.isSignedIn && !authState.isDemo && (!driveChecked || openDrive)) {
        DriveBackupScreen(container.driveSyncRepository, authState.user?.email.orEmpty(),
            onContinue = { driveChecked = true; openDrive = false })
        return
    }
    if (authState.isSignedIn && !authState.isDemo && prefs.setupComplete && !healthChecked) {
        var checked by remember { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(Unit) { checked = container.healthDataRepository.hasAllPermissions() }
        if (checked == true) { LaunchedEffect(Unit) { healthChecked = true } }
        else if (checked == false) HealthConnectStep(container.healthDataRepository,
            remember { SnackbarHostState() }, onNext = { healthChecked = true },
            onSkip = { healthChecked = true }, modifier = Modifier.fillMaxSize())
        return
    }

    // Signed-out storage is empty; old local data is opened only by Continue locally.
    if (!authState.isSignedIn && container.account == com.migraineguard.app.core.security.AccountStorage.SIGNED_OUT) {
        SetupWizardScreen(container.authRepository, container.preferencesRepository, container.healthDataRepository, container.aiClient, {})
        return
    }

    // First-run: single ordered SetupWizard until setupComplete
    if (!prefs.setupComplete) {
        SetupWizardScreen(
            authRepository = container.authRepository,
            preferencesRepository = container.preferencesRepository,
            healthDataRepository = container.healthDataRepository,
            aiClient = container.aiClient,
            onFinished = { healthChecked = true },
        )
        return
    }

    // Settings-driven Gemini reconnect (post-setup)
    if (forceConnectGemini) {
        ConnectGeminiScreen(
            preferencesRepository = container.preferencesRepository,
            aiClient = container.aiClient,
            onConnected = { forceConnectGemini = false },
            onSkipOffline = { forceConnectGemini = false },
        )
        return
    }

    // Settings-driven profile edit (post-setup)
    if (forceProfileEdit) {
        ProfileSetupScreen(
            preferencesRepository = container.preferencesRepository,
            prefilledName = authState.user?.displayName ?: prefs.bodyProfile.displayName,
            initial = prefs.bodyProfile,
            onComplete = { forceProfileEdit = false },
        )
        return
    }

    // After setup, sign-out only needs Sign-In (not the full wizard again)
    if (!authState.isSignedIn) {
        SignInScreen(authRepository = container.authRepository)
        return
    }

    val navController = rememberNavController()
    val bottomItems = listOf(
        BottomNavItem(BottomDest.Home, Icons.Filled.Home),
        BottomNavItem(BottomDest.Migraine, Icons.Outlined.Healing),
        BottomNavItem(BottomDest.Track, Icons.Filled.FitnessCenter),
        BottomNavItem(BottomDest.Coach, Icons.Filled.AutoAwesome),
        BottomNavItem(BottomDest.Food, Icons.Filled.LocalDining),
        BottomNavItem(BottomDest.Insights, Icons.Filled.Insights),
    )

    fun goBottom(route: String) {
        // Debounced HC refresh on tab switch
        container.healthRefreshCoordinator.refresh(force = false)
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openCoach(persona: AiPersona) {
        coachPersona = persona
        goBottom(Routes.Coach)
    }

    val hideBottom = setOf(Routes.Settings, Routes.BodyMetrics, Routes.Medications, Routes.ProfileSetup, Routes.Water)

    // Initial refresh when entering main app
    LaunchedEffect(Unit) {
        container.healthRefreshCoordinator.refresh(force = true)
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute !in hideBottom) {
                NavigationBar(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).clip(RoundedCornerShape(28.dp)),
                    containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp,
                ) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.dest.route,
                            onClick = { goBottom(item.dest.route) },
                            icon = { Icon(item.icon, contentDescription = item.dest.label) },
                            label = { Text(item.dest.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
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
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(120)) },
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    userName = authState.user?.displayName ?: "Friend",
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                    onLogMigraine = { goBottom(Routes.Migraine) },
                    onOpenWater = { navController.navigate(Routes.Water) },
                    onOpenFood = { goBottom(Routes.Food) },
                    onOpenFitness = { goBottom(Routes.Track) },
                    onOpenBody = { navController.navigate(Routes.BodyMetrics) },
                    onOpenMeds = { navController.navigate(Routes.Medications) },
                    onAskCoach = { openCoach(AiPersona.COACH) },
                    onHealthAnalyst = { openCoach(AiPersona.ANALYST) },
                    weatherRepository = container.weatherRepository,
                    healthDataRepository = container.healthDataRepository,
                    healthRefreshCoordinator = container.healthRefreshCoordinator,
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
                    healthRefreshCoordinator = container.healthRefreshCoordinator,
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
                    onOpenCoach = { openCoach(AiPersona.COACH) },
                    onOpenAnalyst = { openCoach(AiPersona.ANALYST) },
                    hasUserGeminiKey = prefs.geminiApiKey.isNotBlank(),
                    onConnectGemini = { forceConnectGemini = true },
                )
            }
            composable(Routes.Coach) {
                CoachScreen(
                    coachRepository = container.aiCoachRepository,
                    initialPersona = coachPersona,
                    hasUserGeminiKey = prefs.geminiApiKey.isNotBlank(),
                    onConnectGemini = { forceConnectGemini = true },
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    preferencesRepository = container.preferencesRepository,
                    backupRepository = container.backupRepository,
                    healthDataRepository = container.healthDataRepository,
                    aiClient = container.aiClient,
                    driveRepository = if (authState.isDemo) null else container.driveSyncRepository,
                    onConnectDrive = { openDrive = true },
                    onSignOut = { container.authRepository.signOut() },
                    onBack = { navController.popBackStack() },
                    onEditProfile = { forceProfileEdit = true },
                    onOpenBody = { navController.navigate(Routes.BodyMetrics) },
                    onOpenMeds = { navController.navigate(Routes.Medications) },
                    onConnectGemini = { forceConnectGemini = true },
                )
            }
            composable(Routes.BodyMetrics) {
                BodyMetricsScreen(
                    weightRepository = container.weightRepository,
                    preferencesRepository = container.preferencesRepository,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Medications) {
                MedicationsScreen(
                    medicationRepository = container.medicationRepository,
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
