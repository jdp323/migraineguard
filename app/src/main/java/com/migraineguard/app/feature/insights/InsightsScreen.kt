package com.migraineguard.app.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.domain.InsightReport
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class RangeMode { Week, Days14, Days30 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    insightsAnalyzer: InsightsAnalyzer,
    aiClient: FallbackAiClient,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(RangeMode.Week) }
    var report by remember { mutableStateOf<InsightReport?>(null) }
    var aiText by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            report = when (mode) {
                RangeMode.Week -> insightsAnalyzer.weeklyTrends()
                RangeMode.Days14 -> {
                    val end = LocalDate.now()
                    insightsAnalyzer.customRange(end.minusDays(13), end)
                }
                RangeMode.Days30 -> {
                    val end = LocalDate.now()
                    insightsAnalyzer.customRange(end.minusDays(29), end)
                }
            }
            aiText = null
            loading = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Insights") }) },
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
                "Aggregate migraines, water, sleep, and weather. AI narrative via ${aiClient.providerNames().joinToString(" → ")}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == RangeMode.Week, onClick = { mode = RangeMode.Week }, label = { Text("7 days") })
                FilterChip(selected = mode == RangeMode.Days14, onClick = { mode = RangeMode.Days14 }, label = { Text("14 days") })
                FilterChip(selected = mode == RangeMode.Days30, onClick = { mode = RangeMode.Days30 }, label = { Text("30 days") })
            }
            Button(onClick = { load() }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                Text(if (loading) "Loading…" else "Build trend report")
            }

            report?.let { r ->
                SectionCard(title = r.title, icon = Icons.Filled.Insights) {
                    Text(r.periodLabel, style = MaterialTheme.typography.labelLarge)
                    Text(r.summary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionCard(title = "Migraines", modifier = Modifier.weight(1f)) {
                        Text("${r.migraineCount}")
                        Text("avg ${"%.1f".format(r.avgIntensity)}/10", style = MaterialTheme.typography.bodySmall)
                    }
                    SectionCard(title = "Water", modifier = Modifier.weight(1f), icon = Icons.Filled.WaterDrop) {
                        Text("${"%.1f".format(r.avgWaterCups)}")
                        Text("cups/day", style = MaterialTheme.typography.bodySmall)
                    }
                }
                SectionCard(title = "Sleep & pressure") {
                    Text("Sleep ~ ${"%.1f".format(r.avgSleepHours)}h")
                    Text(r.pressureTrend)
                }
                OutlinedButton(
                    onClick = {
                        scope.launch { aiText = insightsAnalyzer.aiNarrative(r) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("AI summary") }
            }

            aiText?.let { text ->
                SectionCard(title = "AI narrative", icon = Icons.Filled.AutoAwesome) {
                    Text(text)
                }
            }
        }
    }
}
