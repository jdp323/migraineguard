package com.migraineguard.app.feature.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository

/** Combined Fitness + Sleep tracking stub (bottom-nav "Track"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    healthDataRepository: HealthDataRepository,
    modifier: Modifier = Modifier,
) {
    var fitness by remember { mutableStateOf<HealthSnapshot?>(null) }
    var sleep by remember { mutableStateOf<SleepSnapshot?>(null) }

    LaunchedEffect(Unit) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Fitness & Sleep (stubs via HealthDataRepository)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SectionCard(title = "Fitness") {
                val h = fitness
                if (h != null) {
                    Text("Steps: ${h.steps}")
                    Text("Heart rate: ${h.heartRateBpm ?: "—"} bpm")
                    Text("Active minutes: ${h.activeMinutes}")
                } else Text("Loading…")
            }
            SectionCard(title = "Sleep") {
                val s = sleep
                if (s != null) {
                    Text("Duration: ${s.hours} hours")
                    Text("Quality: ${s.qualityLabel}")
                } else Text("Loading…")
            }
        }
    }
}
