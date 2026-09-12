package com.migraineguard.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.domain.WeatherSnapshot
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.weather.WeatherRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onOpenSettings: () -> Unit,
    weatherRepository: WeatherRepository,
    healthDataRepository: HealthDataRepository,
    modifier: Modifier = Modifier,
) {
    var weather by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var health by remember { mutableStateOf<HealthSnapshot?>(null) }
    var sleep by remember { mutableStateOf<SleepSnapshot?>(null) }

    LaunchedEffect(Unit) {
        weather = weatherRepository.currentWeather()
        health = healthDataRepository.latestFitness()
        sleep = healthDataRepository.latestSleep()
    }

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
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Dashboard stub — cards pull from fake repositories for now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionCard(title = "Weather") {
                val w = weather
                if (w != null) {
                    Text("${w.condition} · ${w.temperatureC}°C · ${w.pressureHpa} hPa")
                    Text(w.locationLabel, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Loading…")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionCard(title = "Fitness", modifier = Modifier.weight(1f)) {
                    val h = health
                    Text(if (h != null) "${h.steps} steps" else "—")
                }
                SectionCard(title = "Sleep", modifier = Modifier.weight(1f)) {
                    val s = sleep
                    Text(if (s != null) "${s.hours}h · ${s.qualityLabel}" else "—")
                }
            }

            SectionCard(title = "Quick tip") {
                Text("Use Migraine to map pain, Water to log cups, Insights for weekly trends.")
            }
        }
    }
}
