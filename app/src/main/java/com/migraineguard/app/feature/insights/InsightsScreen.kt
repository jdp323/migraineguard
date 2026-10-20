package com.migraineguard.app.feature.insights

import com.migraineguard.app.core.ui.AppTopBar

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.domain.InsightReport
import com.migraineguard.app.core.ui.EmptyState
import com.migraineguard.app.core.ui.FeatureHeader
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class RangeMode(val label: String, val days: Long?) {
    Week("7 days", 7),
    Days14("14 days", 14),
    Days30("30 days", 30),
    Custom("Custom 21d", 21),
}

private enum class InsightMode(val label: String) {
    Overall("Overall"),
    Migraine("Migraine"),
    Food("Food"),
    Water("Water"),
    Fitness("Fitness"),
    Sleep("Sleep"),
    Weather("Weather"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    insightsAnalyzer: InsightsAnalyzer,
    aiClient: FallbackAiClient,
    onOpenCoach: () -> Unit = {},
    onOpenAnalyst: () -> Unit = {},
    hasUserGeminiKey: Boolean = false,
    onConnectGemini: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var range by remember { mutableStateOf(RangeMode.Week) }
    var mode by remember { mutableStateOf(InsightMode.Overall) }
    var report by remember { mutableStateOf<InsightReport?>(null) }
    var aiText by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            val end = LocalDate.now()
            val days = (range.days ?: 7) - 1
            report = insightsAnalyzer.customRange(end.minusDays(days), end).copy(
                title = when (range) {
                    RangeMode.Week -> "Weekly trends"
                    else -> "${range.label} trends"
                },
            )
            aiText = null
            loading = false
        }
    }

    LaunchedEffect(range) { load() }

    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = { Text("Insights") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureHeader("See the bigger picture.", "Reflect on your journal, habits and recorded trends.", Icons.Filled.Insights, eyebrow = "Your insights")
            Text(
                "Modes for migraines, food, water, fitness, sleep, and weather. AI via ${aiClient.providerNames().joinToString(" → ")}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionCard(title = "AI hub", icon = Icons.Filled.AutoAwesome) {
                Text("Open Coach for habits or Health Analyst for an educational migraine review.")
                if (!hasUserGeminiKey) {
                    Text(
                        "Cloud AI needs YOUR Gemini key. Without it, OfflineHealthBrain only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OutlinedButton(onClick = onConnectGemini, modifier = Modifier.fillMaxWidth()) {
                        Text("Connect Gemini")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenCoach) { Text("Ask Coach") }
                    OutlinedButton(onClick = onOpenAnalyst) { Text("Health Analyst") }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InsightMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = { mode = m },
                        label = { Text(m.label) },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeMode.entries.forEach { r ->
                    FilterChip(
                        selected = range == r,
                        onClick = { range = r },
                        label = { Text(r.label) },
                    )
                }
            }

            Button(onClick = { load() }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                Text(if (loading) "Loading…" else "Refresh report")
            }

            val r = report
            if (r == null && !loading) {
                EmptyState(
                    title = "No insights yet",
                    subtitle = "Log migraines, water, food, and workouts — then refresh.",
                    icon = Icons.Filled.Insights,
                )
            } else if (r != null) {
                when (mode) {
                    InsightMode.Overall -> {
                        SectionCard(title = r.title, icon = Icons.Filled.Insights) {
                            Text(r.periodLabel, style = MaterialTheme.typography.labelLarge)
                            Text(r.summary)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionCard(title = "Migraines", modifier = Modifier.weight(1f), icon = Icons.Filled.Healing) {
                                Text("${r.migraineCount}")
                                Text("avg ${"%.1f".format(r.avgIntensity)}/10", style = MaterialTheme.typography.bodySmall)
                            }
                            SectionCard(title = "Water", modifier = Modifier.weight(1f), icon = Icons.Filled.WaterDrop) {
                                Text("${"%.1f".format(r.avgWaterCups)}")
                                Text("cups/recorded day", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionCard(title = "Fitness", modifier = Modifier.weight(1f), icon = Icons.Filled.FitnessCenter) {
                                Text("${r.workoutCount} workouts")
                                Text("${r.workoutMinutes} min · ${r.totalSteps ?: "—"} steps today", style = MaterialTheme.typography.bodySmall)
                            }
                            SectionCard(title = "Latest sleep", modifier = Modifier.weight(1f), icon = Icons.Filled.Bedtime) {
                                Text(r.avgSleepHours?.let { "%.1fh".format(it) } ?: "Unavailable")
                            }
                        }
                        OutlinedButton(
                            onClick = { scope.launch { aiText = insightsAnalyzer.aiNarrative(r) } },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("AI narrative") }
                    }
                    InsightMode.Migraine -> {
                        if (r.migraineCount == 0) {
                            EmptyState("No migraines in range", "Log episodes with the head map.", icon = Icons.Filled.Healing)
                        } else {
                            SectionCard(title = "Migraine analysis", icon = Icons.Filled.Healing) {
                                Text("${r.migraineCount} episodes · avg intensity ${"%.1f".format(r.avgIntensity)}/10")
                                Text("Head sides: ${r.sideSummary}")
                                Text(r.summary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    InsightMode.Food -> {
                        if (r.foodScanCount == 0) {
                            EmptyState("No food scans", "Use the Food tab to log and describe meals.", icon = Icons.Filled.LocalDining)
                        } else {
                            SectionCard(title = "Food log", icon = Icons.Filled.LocalDining) {
                                Text("${r.foodScanCount} scans in period")
                                if (r.topFoods.isEmpty()) Text("No labeled foods yet")
                                else r.topFoods.forEach { Text("· $it") }
                            }
                        }
                    }
                    InsightMode.Water -> {
                        SectionCard(title = "Hydration", icon = Icons.Filled.WaterDrop) {
                            if (r.avgWaterCups <= 0f) {
                                Text("No water logged in this range. Aim for steady cups each day.")
                            } else {
                                Text("Average ${"%.1f".format(r.avgWaterCups)} cups per recorded day")
                                Text(
                                    if (r.avgWaterCups < 6f) "Below a common 6–8 cup target — dehydration can worsen migraines."
                                    else "Solid hydration average for this window.",
                                )
                            }
                        }
                    }
                    InsightMode.Fitness -> {
                        if (r.workoutCount == 0 && (r.totalSteps == null || r.totalSteps == 0)) {
                            EmptyState("No fitness data", "Connect Health Connect or log workouts on the Fitness tab.", icon = Icons.Filled.FitnessCenter)
                        } else {
                            SectionCard(title = "Fitness", icon = Icons.Filled.FitnessCenter) {
                                Text("${r.workoutCount} workouts · ${r.workoutMinutes} minutes")
                                Text("Steps (latest day snapshot): ${r.totalSteps ?: "Unavailable"}")
                            }
                        }
                    }
                    InsightMode.Sleep -> {
                        SectionCard(title = "Sleep", icon = Icons.Filled.Bedtime) {
                            if (r.avgSleepHours == null) {
                                Text("No sleep data yet. Grant Health Connect sleep permission or wait for sync.")
                            } else {
                                Text("Recent sleep ~ ${"%.1f".format(r.avgSleepHours)} hours")
                                Text(
                                    when {
                                        r.avgSleepHours < 6f -> "Short sleep — a frequent migraine amplifier."
                                        r.avgSleepHours >= 7.5f -> "Recorded session duration; sleep quality is not measured here."
                                        else -> "Fair sleep window; consistency matters as much as duration."
                                    },
                                )
                            }
                        }
                    }
                    InsightMode.Weather -> {
                        SectionCard(title = "Weather & pressure", icon = Icons.Filled.Thermostat) {
                            Text(r.pressureTrend)
                            Text("Historical pressure and personal correlations are not available yet.")
                        }
                    }
                }
            }

            aiText?.let { text ->
                SectionCard(title = "AI narrative", icon = Icons.Filled.AutoAwesome) {
                    Text(text)
                }
            }
        }
    }
}
