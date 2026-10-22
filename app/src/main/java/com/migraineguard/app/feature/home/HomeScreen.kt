package com.migraineguard.app.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.core.ui.*
import com.migraineguard.app.feature.fitness.*
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.WeatherRepository
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(userName: String, onOpenSettings: () -> Unit, onLogMigraine: () -> Unit,
    onOpenWater: () -> Unit, onOpenFood: () -> Unit, onOpenFitness: () -> Unit,
    onOpenBody: () -> Unit = {}, onOpenMeds: () -> Unit = {}, onAskCoach: () -> Unit = {},
    onHealthAnalyst: () -> Unit = {}, weatherRepository: WeatherRepository,
    healthDataRepository: HealthDataRepository, healthRefreshCoordinator: HealthRefreshCoordinator? = null,
    waterIntakeRepository: WaterIntakeRepository, preferencesRepository: UserPreferencesRepository,
    migraineRepository: MigraineRepository, modifier: Modifier = Modifier, isActive: Boolean = true) {
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    val cups by waterIntakeRepository.cupsToday.collectAsStateWithLifecycle(0)
    val refresh = healthRefreshCoordinator?.state?.collectAsStateWithLifecycle()?.value
    if(isActive) HealthAutoRefresh(healthRefreshCoordinator)
    Scaffold(modifier = modifier, topBar = { AppTopBar(title = { Text("Today") }, actions = {
        IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "Settings") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Hello, "+prefs.bodyProfile.displayName.ifBlank { userName }.substringBefore(' '), style = MaterialTheme.typography.bodyMedium, color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Your day, at a glance.", style = MaterialTheme.typography.headlineSmall)
            Text(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")), color = MaterialTheme.colorScheme.onSurfaceVariant, style=MaterialTheme.typography.labelMedium)
            DailyOverview(refresh?.fitness?.observedSteps,prefs.dailyStepGoal,cups,prefs.waterGoalCups,
                refresh?.sleep?.recordedHours,prefs.sleepGoalHours,null,onOpenFitness,onOpenWater)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile("Train", "Log your workout", Icons.Default.FitnessCenter, onOpenFitness, Modifier.weight(1f))
                ActionTile("Meals", "Photo & food log", Icons.Default.LocalDining, onOpenFood, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile("Body", "Weight & progress", Icons.Default.MonitorWeight, onOpenBody, Modifier.weight(1f))
                ActionTile("Coach", "Ask your AI", Icons.Default.AutoAwesome, onAskCoach, Modifier.weight(1f))
            }
        }
    }
}
