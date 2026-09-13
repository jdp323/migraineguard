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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.migraineguard.app.BuildConfig
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
import java.time.LocalDate
import java.time.LocalTime
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
    onOpenBody: () -> Unit = {},
    onOpenMeds: () -> Unit = {},
    onAskCoach: () -> Unit = {},
    onHealthAnalyst: () -> Unit = {},
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
    val waterHistory by waterIntakeRepository.observeHistory(14).collectAsStateWithLifecycle(emptyList())

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

    val today = LocalDate.now()
    val todayIso = today.toString()
    val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val todayMigraines = episodes.count { it.timestampMillis >= todayStart }
    val displayName = prefs.bodyProfile.displayName.ifBlank { userName }.let { n ->
        n.trim().substringBefore(' ').ifBlank { n }
    }
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
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
    val sleepGoal = prefs.sleepGoalHours.coerceAtLeast(1f)
    val sleepProgress = (((sleep?.hours ?: 0f) / sleepGoal).coerceAtMost(1f))
    val streak = remember(waterHistory, health) {
        var s = 0
        var d = today
        val waterDates = waterHistory.filter { it.cups > 0 }.map { it.dateIso }.toSet()
        while (true) {
            val iso = d.toString()
            val hasWater = iso in waterDates || (iso == todayIso && cups > 0)
            val hasSteps = iso == todayIso && (health?.steps ?: 0) > 0
            if (hasWater || hasSteps) {
                s++
                d = d.minusDays(1)
            } else break
            if (s > 60) break
        }
        s
    }
    val energyToday = if (prefs.energyDateIso == todayIso) prefs.energyLevel else 0
    val bmi = prefs.bodyProfile.bmi

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
            Text("$greeting, $displayName", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Today · $today · MigraineGuard v${BuildConfig.VERSION_NAME} · streak $streak day${if (streak == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionCard(title = "Daily summary", icon = Icons.Filled.LocalFireDepartment) {
                Text("Steps ${(health?.steps ?: 0)} / $stepGoal")
                LinearProgressIndicator(progress = { stepProgress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Water $cups / ${prefs.waterGoalCups} cups")
                LinearProgressIndicator(progress = { waterProgress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Sleep ${sleep?.hours ?: "—"}h / ${prefs.sleepGoalHours}h goal")
                LinearProgressIndicator(progress = { sleepProgress }, modifier = Modifier.fillMaxWidth())
                health?.caloriesBurned?.let {
                    Spacer(Modifier.height(6.dp))
                    Text("Calories ~%.0f kcal".format(it))
                }
                Spacer(Modifier.height(6.dp))
                Text(riskHint, style = MaterialTheme.typography.bodyMedium)
            }

            if (bmi != null) {
                SectionCard(title = "BMI", icon = Icons.Filled.MonitorWeight) {
                    Text("%.1f · %s".format(bmi, prefs.bodyProfile.bmiLabel), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "From your profile height/weight. Not a medical diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    AssistChip(onClick = onOpenBody, label = { Text("Body metrics") })
                }
            }

            SectionCard(title = "Energy check-in", icon = Icons.Filled.Bolt) {
                Text("How’s your energy today? (1–5)")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { level ->
                        FilterChip(
                            selected = energyToday == level,
                            onClick = {
                                scope.launch {
                                    preferencesRepository.setEnergyLevel(level, todayIso)
                                }
                            },
                            label = { Text("$level") },
                        )
                    }
                }
            }

            SectionCard(title = "Migraine risk hint", icon = Icons.Filled.Healing) {
                Text(riskHint)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onLogMigraine, label = { Text("Log migraine") })
                    AssistChip(onClick = onOpenWater, label = { Text("Add water") })
                    AssistChip(onClick = onOpenFood, label = { Text("Scan food") })
                    AssistChip(onClick = onOpenFitness, label = { Text("Fitness") })
                    AssistChip(onClick = onOpenMeds, label = { Text("Meds") })
                }
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

            SectionCard(title = "AI Coach & Health Analyst", icon = Icons.Filled.AutoAwesome) {
                Text(
                    "Motivational coaching or a doctor-style educational review of your logs. Not medical advice — offline brain always works.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAskCoach, modifier = Modifier.fillMaxWidth()) {
                    Text("Ask Coach")
                }
                OutlinedButton(onClick = onHealthAnalyst, modifier = Modifier.fillMaxWidth()) {
                    Text("Health Analyst review")
                }
            }

            SectionCard(title = "Quick actions", icon = Icons.Filled.FitnessCenter) {
                Text("Use the bottom bar for Migraine, Fitness, Coach, Food, and Insights. Water is on Home.")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onAskCoach, label = { Text("Coach") })
                    AssistChip(onClick = onOpenFitness, label = { Text("Fitness hub") })
                    AssistChip(onClick = onOpenWater, label = { Text("Water") })
                    AssistChip(onClick = onOpenBody, label = { Text("Weight log") })
                    AssistChip(onClick = onOpenMeds, label = { Text("Medications") })
                }
            }
        }
    }
}
