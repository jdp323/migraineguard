package com.migraineguard.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Surface
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.migraineguard.app.core.ai.hasConfiguredCloud
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
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
fun MigraineGuardNavHost(container: AppContainer, requestedRoute: String? = null, onRouteConsumed: () -> Unit = {}) {
    val authState by container.authRepository.authState.collectAsStateWithLifecycle()
    val loadedPrefs by container.preferencesRepository.preferences.collectAsStateWithLifecycle<UserPreferences?>(null)
    val prefs = loadedPrefs ?: return
    val scope = rememberCoroutineScope()
    var settingsInitialGroup by remember { mutableStateOf("Account") }
    var coachPersona by remember { mutableStateOf(AiPersona.COACH) }

    var driveChecked by remember { mutableStateOf(false) }
    if (authState.isSignedIn && !authState.isDemo && (!prefs.setupComplete && !driveChecked)) {
        DriveBackupScreen(container.driveSyncRepository, authState.user?.email.orEmpty(),
            onContinue = { driveChecked = true })
        return
    }
    if (authState.isSignedIn && !authState.isDemo && prefs.setupComplete && prefs.driveSyncEnabled) {
        com.migraineguard.app.feature.settings.SilentDriveReconnect(container.driveSyncRepository, authState.user?.email.orEmpty())
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
            onFinished = {},
        )
        return
    }

    // After setup, sign-out only needs Sign-In (not the full wizard again)
    if (!authState.isSignedIn) {
        SignInScreen(authRepository = container.authRepository)
        return
    }

    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val bottomItems = MainTabs
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { bottomItems.size })
    val pageStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
    var tabJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    fun goBottom(route: String) {
        val page = bottomItems.indexOfFirst { it.dest.route == route }
        if (page < 0) return
        if (currentRoute != null && currentRoute != Routes.Home) navController.popBackStack(Routes.Home,false)
        tabJob?.cancel()
        tabJob = scope.launch { pagerState.animateScrollToPage(page, animationSpec = androidx.compose.animation.core.tween(320)) }
    }
    androidx.activity.compose.BackHandler(enabled = currentRoute == Routes.Home && pagerState.currentPage != 0) { goBottom(Routes.Home) }
    LaunchedEffect(pagerState.settledPage) { container.healthRefreshCoordinator.refresh(force = false) }

    fun openCoach(persona: AiPersona) {
        coachPersona = persona
        goBottom(Routes.Coach)
    }

    LaunchedEffect(requestedRoute) {
        if (requestedRoute in setOf(Routes.Food, Routes.Migraine, Routes.Track)) {
            goBottom(requestedRoute!!); onRouteConsumed()
        } else if (requestedRoute in setOf(Routes.Water, Routes.Medications, Routes.Settings)) {
            navController.navigate(requestedRoute!!) { launchSingleTop = true }; onRouteConsumed()
        }
    }

    // Initial refresh when entering main app
    LaunchedEffect(Unit) {
        container.healthRefreshCoordinator.refresh(force = true)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute == Routes.Home) ModernTabDock(pagerState) { goBottom(it) }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(340)) { it } },
            exitTransition = { fadeOut(tween(220)) + slideOutHorizontally(tween(340)) { -it / 4 } },
            popEnterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(340)) { -it / 4 } },
            popExitTransition = { fadeOut(tween(220)) + slideOutHorizontally(tween(340)) { it } },
        ) {
            composable(Routes.Home) {
                androidx.compose.foundation.pager.HorizontalPager(state = pagerState,
                    beyondViewportPageCount = 1, key = { bottomItems[it].dest.route },
                    modifier = Modifier.fillMaxSize().testTag("main-pager")) { page ->
                    val active = currentRoute == Routes.Home && pagerState.currentPage == page
                    pageStateHolder.SaveableStateProvider(bottomItems[page].dest.route) {
                    when (bottomItems[page].dest.route) {
                        Routes.Home -> {
                HomeScreen(
                    userName = authState.user?.displayName ?: "Friend",
                    isActive = active,
                    onOpenSettings = { settingsInitialGroup = "Account"; navController.navigate(Routes.Settings) },
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
                        Routes.Migraine -> MigraineScreen(repository = container.migraineRepository, preferences = container.preferencesRepository,
                            weatherRepository = container.weatherRepository, onOpenMeds = { navController.navigate(Routes.Medications) },
                            onAnalyst = { openCoach(AiPersona.ANALYST) }, isActive = active)
                        Routes.Track -> TrackScreen(healthDataRepository = container.healthDataRepository,
                            healthRefreshCoordinator = container.healthRefreshCoordinator, workoutRepository = container.workoutRepository,
                            preferencesRepository = container.preferencesRepository, trainerRepository = container.trainerRepository, isActive = active)
                        Routes.Food -> NutritionScreen(nutritionAnalyzer = container.nutritionAnalyzer)
                        Routes.Insights -> InsightsScreen(insightsAnalyzer = container.insightsAnalyzer, aiClient = container.aiClient,
                            onOpenCoach = { openCoach(AiPersona.COACH) }, onOpenAnalyst = { openCoach(AiPersona.ANALYST) },
                            hasCloudKey = prefs.hasConfiguredCloud(), onConnectGemini = { settingsInitialGroup = "AI"; navController.navigate(Routes.Settings) })
                        Routes.Coach -> CoachScreen(coachRepository = container.aiCoachRepository, initialPersona = coachPersona,
                            hasCloudKey = prefs.hasConfiguredCloud(), onConnectGemini = { settingsInitialGroup = "AI"; navController.navigate(Routes.Settings) })
                    }
                    }
                }
            }
            composable(Routes.Water) {
                WaterScreen(repository = container.waterIntakeRepository, preferencesRepository = container.preferencesRepository,
                    onBack = { navController.popBackStack() })
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    initialGroup = settingsInitialGroup,
                    preferencesRepository = container.preferencesRepository,
                    backupRepository = container.backupRepository,
                    healthDataRepository = container.healthDataRepository,
                    aiClient = container.aiClient,
                    driveRepository = if (authState.isDemo) null else container.driveSyncRepository,
                    onConnectDrive = { navController.navigate(Routes.DriveBackup) },
                    onSignOut = { container.authRepository.signOut() },
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate(Routes.ProfileSetup) },
                    onOpenBody = { navController.navigate(Routes.BodyMetrics) },
                    onOpenMeds = { navController.navigate(Routes.Medications) },
                    onConnectGemini = { settingsInitialGroup = "AI"; navController.navigate(Routes.Settings) },
                )
            }
            composable(Routes.ProfileSetup) {
                ProfileSetupScreen(preferencesRepository=container.preferencesRepository,
                    prefilledName=authState.user?.displayName ?: prefs.bodyProfile.displayName,initial=prefs.bodyProfile,
                    onComplete={navController.popBackStack()},onBack={navController.popBackStack()})
            }
            composable(Routes.DriveBackup) {
                DriveBackupScreen(container.driveSyncRepository,authState.user?.email.orEmpty(),
                    onContinue={navController.popBackStack()},onBack={navController.popBackStack()})
            }
            composable(Routes.BodyMetrics) {
                BodyMetricsScreen(
                    progressRepository = container.bodyProgressRepository,
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
