package com.migraineguard.app.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.domain.InsightReport
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    insightsAnalyzer: InsightsAnalyzer,
    aiClient: FallbackAiClient,
    modifier: Modifier = Modifier,
) {
    var report by remember { mutableStateOf<InsightReport?>(null) }
    var aiText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                text = "Weekly / custom AI trends stub. Providers: ${aiClient.providerNames().joinToString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    scope.launch { report = insightsAnalyzer.weeklyTrends() }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Load weekly trends")
            }
            Button(
                onClick = {
                    scope.launch {
                        val end = LocalDate.now()
                        val start = end.minusDays(14)
                        report = insightsAnalyzer.customRange(start, end)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Load custom (14 days)")
            }
            Button(
                onClick = {
                    scope.launch {
                        aiText = when (
                            val r = aiClient.complete("Summarize migraine triggers for the past week")
                        ) {
                            is Result.Success -> r.data
                            is Result.Error -> "AI error: ${r.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Run AI fallback stub")
            }

            report?.let { r ->
                SectionCard(title = r.title) {
                    Text(r.periodLabel, style = MaterialTheme.typography.labelLarge)
                    Text(r.summary)
                }
            }
            aiText?.let { text ->
                SectionCard(title = "AI narrative") {
                    Text(text)
                }
            }
        }
    }
}
