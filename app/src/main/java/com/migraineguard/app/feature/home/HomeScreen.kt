package com.migraineguard.app.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.domain.WeatherSnapshot
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.WeatherRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onOpenSettings: () -> Unit,
    onLogMigraine: () -> Unit,
    onOpenWater: () -> Unit,
    onOpenFood: () -> Unit,
    onOpenFitness: () -> Unit,
    weatherRepository: WeatherRepository,
    healthDataRepository: HealthDataRepository,
    waterIntakeRepository: WaterIntakeRepository,
    preferencesRepository: UserPreferencesRepository,
    migraineRepository: MigraineRepository,
    modifier: Modifier = Modifier,
) {
    var weather by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var health by remember { mutableStateOf<HealthSnapshot?>(null) }
    var sleep by remember { mutableStateOf<SleepSnapshot?>(null) }
    val cups by waterIntakeRepository.cupsToday.collectAsStateWithLifecycle(0)
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(
        com.migraineguard.app.core.data.prefs.UserPreferences(),
    )
    val episodes by migraineRepository.observeEpisodes().collectAsStateWithLifecycle(emptyList())

    val scope = rememberCoroutineScope()
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        scope.launch {
            weather = weatherRepository.currentWeather(forceRefresh = true)
        }
    }
    LaunchedEffect(Unit) {
        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
        )
        weather = weatherRepository.currentWeather(forceRefresh = true)
        health = healthDataRepository.latestFitness()
        sleep = healthDataRepository.latestSleep()
    }

    val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val todayMigraines = episodes.count { it.timestampMillis >= todayStart }
    val riskHint = buildString {
        val w = weather
        when {
            todayMigraines > 0 -> append("You already logged a migraine today — rest & hydrate.")
            w != null && w.pressureHpa < 1005 -> append("Low pressure (${w.pressureHpa} hPa) — elevated migraine risk hint.")
            cups < prefs.waterGoalCups / 2 -> append("Hydration is low so far — a common migraine trigger.")
            sleep != null && sleep!!.hours < 6f -> append("Short sleep last night — watch for tension headaches.")
            else -> append("Conditions look steady. Keep logging habits for better insights.")
        }
    }
    val waterProgress = (cups.toFloat() / prefs.waterGoalCups.coerceAtLeast(1)).coerceAtMost(1f)
    val stepGoal = prefs.dailyStepGoal.coerceAtLeast(1)
    val stepProgress = ((health?.steps ?: 0).toFloat() / stepGoal).coerceAtMost(1f)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Hello, $userName", style = MaterialTheme.typography.headlineSmall)
            Text(
                Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                    .let { "Today · $it · MigraineGuard v1.1" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionCard(title = "Migraine risk hint", icon = Icons.Filled.Healing) {
                Text(riskHint)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onLogMigraine, label = { Text("Log migraine") })
                    AssistChip(onClick = onOpenWater, label = { Text("Add water") })
                    AssistChip(onClick = onOpenFood, label = { Text("Scan food") })
                    AssistChip(onClick = onOpenFitness, label = { Text("Fitness") })
                }
            }

            SectionCard(title = "Water today", icon = Icons.Filled.WaterDrop) {
                Text("$cups / ${prefs.waterGoalCups} cups")
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { waterProgress }, modifier = Modifier.fillMaxWidth())
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionCard(title = "Steps", modifier = Modifier.weight(1f), icon = Icons.Filled.DirectionsWalk) {
                    val h = health
                    Text(if (h != null) "${h.steps}" else "—")
                    Text("Goal $stepGoal · ${(stepProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Text(h?.sourceLabel ?: "", style = MaterialTheme.typography.bodySmall)
                }
                SectionCard(title = "Sleep", modifier = Modifier.weight(1f), icon = Icons.Filled.Bedtime) {
                    val s = sleep
                    Text(if (s != null) "${s.hours}h" else "—")
                    Text(s?.qualityLabel ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }

            SectionCard(title = "Weather", icon = Icons.Filled.Thermostat) {
                val w = weather
                if (w != null) {
                    Text("${w.condition} · ${w.temperatureC}°C")
                    Text("${w.pressureHpa} hPa · ${w.humidityPercent}% humidity")
                    Text(w.locationLabel, style = MaterialTheme.typography.bodySmall)
                } else Text("Loading…")
            }

            SectionCard(title = "Quick actions", icon = Icons.Filled.FitnessCenter) {
                Text("Use the bottom bar for Migraine, Fitness, Food, Water, and Insights.")
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = onOpenFitness, label = { Text("Open Fitness hub") })
            }
        }
    }
}
