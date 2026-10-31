package com.migraineguard.app.feature.body

import com.migraineguard.app.core.ui.AppTopBar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.WeightEntry
import com.migraineguard.app.core.ui.FeatureHeader
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMetricsScreen(
    weightRepository: WeightRepository,
    progressRepository: BodyProgressRepository,
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit,
    measurementRepository: BodyMeasurementRepository? = null,
    modifier: Modifier = Modifier,
) {
    val entries by weightRepository.observeEntries().collectAsStateWithLifecycle(emptyList())
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var weightText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val metric = prefs.bodyProfile.useMetric
    val validWeightKg = weightText.toFloatOrNull()?.let { if(metric) it else it / 2.20462f }?.takeIf { it.isFinite() && it in 1f..700f }
    val lineColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = { Text("Body metrics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureHeader("Progress at your pace.", "Record measurements and reflect on your own trend.", Icons.Filled.MonitorWeight, eyebrow = "Your body")
            prefs.bodyProfile.bmi?.let { bmi ->
                SectionCard(title = "BMI", icon = Icons.Filled.MonitorWeight) {
                    Text("%.1f · %s".format(bmi, prefs.bodyProfile.bmiLabel), style = MaterialTheme.typography.titleLarge)
                }
            }

            measurementRepository?.let { BodyMeasurementsPanel(it, metric) }

            BodyProgressPanel(progressRepository, preferencesRepository, metric)

            SectionCard(title = "Log weight") {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                    label = { Text(if (metric) "Weight (kg)" else "Weight (lb)") },
                    modifier = Modifier.fillMaxWidth().testTag("body-weight-input"),
                    isError = weightText.isNotBlank() && validWeightKg == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Button(
                    onClick = {
                        val kg = validWeightKg ?: return@Button
                        scope.launch {
                            weightRepository.save(
                                WeightEntry(
                                    timestampMillis = System.currentTimeMillis(),
                                    weightKg = kg,
                                    notes = notes.trim(),
                                ),
                            )
                            val profile = prefs.bodyProfile.copy(weightKg = kg)
                            preferencesRepository.saveBodyProfile(profile)
                            weightText = ""
                            notes = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = validWeightKg != null,
                ) { Text("Save weight") }
            }

            if (entries.size >= 2) {
                SectionCard(title = "Trend") {
                    val spark = entries.take(14).reversed()
                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                    ) {
                        val min = spark.minOf { it.weightKg }
                        val max = spark.maxOf { it.weightKg }.coerceAtLeast(min + 0.1f)
                        val path = Path()
                        spark.forEachIndexed { i, e ->
                            val x = size.width * i / (spark.size - 1).coerceAtLeast(1)
                            val y = size.height * (1f - (e.weightKg - min) / (max - min))
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            drawCircle(lineColor, 5f, Offset(x, y))
                        }
                        drawPath(path, lineColor, style = Stroke(width = 4f))
                    }
                }
            }

            SectionCard(title = "History") {
                if (entries.isEmpty()) {
                    Text("No weight entries yet.")
                } else {
                    entries.take(20).forEach { e ->
                        val display = if (metric) "%.1f kg".format(e.weightKg)
                        else "%.1f lb".format(e.weightKg * 2.20462f)
                        Text("${formatTs(e.timestampMillis)} · $display" + if (e.notes.isNotBlank()) " · ${e.notes}" else "")
                    }
                }
            }
        }
    }
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("MMM d · HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}
