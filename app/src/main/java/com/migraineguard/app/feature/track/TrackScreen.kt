package com.migraineguard.app.feature.track

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthConnectStatus
import com.migraineguard.app.feature.fitness.HealthDataRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    healthDataRepository: HealthDataRepository,
    modifier: Modifier = Modifier,
) {
    var fitness by remember { mutableStateOf<HealthSnapshot?>(null) }
    var sleep by remember { mutableStateOf<SleepSnapshot?>(null) }
    var status by remember { mutableStateOf(HealthConnectStatus.USING_FALLBACK) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) {
        scope.launch {
            status = healthDataRepository.availabilityStatus()
            fitness = healthDataRepository.latestFitness()
            sleep = healthDataRepository.latestSleep()
        }
    }

    LaunchedEffect(Unit) {
        status = healthDataRepository.availabilityStatus()
        fitness = healthDataRepository.latestFitness()
        sleep = healthDataRepository.latestSleep()
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Track") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Samsung Health / Health Connect", icon = Icons.Filled.Watch) {
                Text(
                    "MigraineGuard reads steps, heart rate, and sleep through Android Health Connect. " +
                        "On Samsung phones, open Samsung Health → Settings → Health Connect (or Data permissions) " +
                        "and allow sharing with Health Connect so Galaxy Watch data appears here.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Status: $status",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Button(
                    onClick = {
                        val perms = healthDataRepository.requiredPermissions()
                        if (perms.isNotEmpty()) {
                            permissionLauncher.launch(perms)
                        } else {
                            scope.launch {
                                status = healthDataRepository.availabilityStatus()
                                fitness = healthDataRepository.latestFitness()
                                sleep = healthDataRepository.latestSleep()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) { Text("Request Health Connect permissions") }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(healthDataRepository.openHealthConnectSettingsIntent())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open Health Connect")
                }
            }

            SectionCard(title = "Fitness", icon = Icons.Filled.MonitorHeart) {
                val h = fitness
                if (h != null) {
                    Text("Steps: ${h.steps}")
                    Text("Heart rate: ${h.heartRateBpm ?: "—"} bpm")
                    Text("Active minutes (est.): ${h.activeMinutes}")
                    Text("Source: ${h.sourceLabel}", style = MaterialTheme.typography.bodySmall)
                } else Text("Loading…")
            }

            SectionCard(title = "Sleep", icon = Icons.Filled.Bedtime) {
                val s = sleep
                if (s != null) {
                    Text("Duration: ${s.hours} hours")
                    Text("Quality: ${s.qualityLabel}")
                    Text("Source: ${s.sourceLabel}", style = MaterialTheme.typography.bodySmall)
                } else Text("Loading…")
            }

            SectionCard(title = "Heart", icon = Icons.Filled.Favorite) {
                Text("Latest BPM comes from Health Connect when permitted; otherwise a calm demo value is shown so the UI stays usable offline.")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        status = healthDataRepository.availabilityStatus()
                        fitness = healthDataRepository.latestFitness()
                        sleep = healthDataRepository.latestSleep()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Refresh data") }
        }
    }
}
