package com.migraineguard.app.feature.track

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.R
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.domain.WorkoutEntry
import com.migraineguard.app.core.domain.WorkoutType
import com.migraineguard.app.core.ui.ProgressRing
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthConnectStatus
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.WorkoutRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackScreen(
    healthDataRepository: HealthDataRepository,
    workoutRepository: WorkoutRepository,
    preferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier,
) {
    var fitness by remember { mutableStateOf<HealthSnapshot?>(null) }
    var sleep by remember { mutableStateOf<SleepSnapshot?>(null) }
    var status by remember { mutableStateOf(HealthConnectStatus.USING_FALLBACK) }
    var permitted by remember { mutableStateOf(false) }
    val workouts by workoutRepository.observeWorkouts().collectAsStateWithLifecycle(emptyList())
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    val eod = remember { workoutRepository.exerciseOfTheDay() }
    var eodDoneToday by remember { mutableStateOf(false) }

    var logType by remember { mutableStateOf(WorkoutType.WALK) }
    var logMinutes by remember { mutableStateOf("20") }
    var logCalories by remember { mutableStateOf("") }
    var logNotes by remember { mutableStateOf("") }
    var manualSteps by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        scope.launch {
            refresh(healthDataRepository) { s, f, sl, p ->
                status = s; fitness = f; sleep = sl; permitted = p
            }
            if (granted.isNotEmpty() && healthDataRepository.hasAllPermissions()) {
                Toast.makeText(context, "Health Connect connected ✓", Toast.LENGTH_SHORT).show()
                snackbar.showSnackbar("Permissions granted — MigraineGuard should appear in Health Connect Connected apps.")
            } else if (granted.isNotEmpty()) {
                snackbar.showSnackbar("Some permissions granted. You can grant the rest anytime.")
            } else {
                snackbar.showSnackbar("No permissions granted. Sideloaded debug apps appear after the first successful request.")
            }
        }
    }

    fun openHcFallback() {
        val intents = healthDataRepository.openHealthConnectInstallOrSettingsIntents()
        var opened = false
        for (intent in intents) {
            try {
                context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                opened = true
                break
            } catch (_: Exception) {
            }
        }
        if (!opened) {
            scope.launch { snackbar.showSnackbar("Could not open Health Connect / Play Store") }
        }
        healthDataRepository.openSamsungHealthSettingsIntent()?.let {
            runCatching { context.startActivity(it) }
        }
    }

    fun connectHealth() {
        scope.launch {
            val avail = healthDataRepository.availabilityStatus()
            status = avail
            when (avail) {
                HealthConnectStatus.NOT_INSTALLED -> {
                    snackbar.showSnackbar("Health Connect not installed — opening Play Store / settings…")
                    openHcFallback()
                }
                HealthConnectStatus.UNSUPPORTED -> {
                    snackbar.showSnackbar("Health Connect unsupported on this device — opening settings…")
                    openHcFallback()
                }
                HealthConnectStatus.AVAILABLE, HealthConnectStatus.USING_FALLBACK -> {
                    var perms = healthDataRepository.requiredPermissions()
                    if (perms.isEmpty()) {
                        perms = healthDataRepository.requiredPermissions()
                    }
                    if (perms.isEmpty()) {
                        snackbar.showSnackbar("Could not build permission set — opening Health Connect settings…")
                        openHcFallback()
                    } else {
                        snackbar.showSnackbar("Opening Health Connect permissions…")
                        permissionLauncher.launch(perms)
                    }
                }
            }
        }
    }

    fun reload() {
        scope.launch {
            refresh(healthDataRepository) { s, f, sl, p ->
                status = s; fitness = f; sleep = sl; permitted = p
            }
            val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            eodDoneToday = workouts.any { it.fromExerciseOfDay && it.timestampMillis >= todayStart && it.type == eod.type }
        }
    }

    LaunchedEffect(Unit) { reload() }
    LaunchedEffect(workouts) {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        eodDoneToday = workouts.any { it.fromExerciseOfDay && it.timestampMillis >= todayStart }
    }

    val steps = fitness?.steps ?: 0
    val stepGoal = prefs.dailyStepGoal.coerceAtLeast(1)
    val stepProgress = (steps.toFloat() / stepGoal).coerceAtMost(1f)

    val weekStart = LocalDate.now().minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val weekWorkouts = workouts.filter { it.timestampMillis >= weekStart }
    val weekMinutes = weekWorkouts.sumOf { it.durationMinutes }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text("Fitness") }) },
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
                    "MigraineGuard appears under Health Connect → Connected apps after the first successful permission request (including sideloaded debug builds). " +
                        "Enable Samsung Health → Health Connect sharing so Galaxy Watch data flows here.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Status: $status · Permissions: ${if (permitted) "granted" else "needed"}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Button(
                    onClick = { connectHealth() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) { Text(stringResource(R.string.connect_health_connect)) }
                OutlinedButton(
                    onClick = { openHcFallback() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.open_health_connect_settings)) }
                healthDataRepository.openSamsungHealthSettingsIntent()?.let {
                    OutlinedButton(
                        onClick = { runCatching { context.startActivity(it) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open Samsung Health") }
                }
            }

            SectionCard(title = stringResource(R.string.steps_today), icon = Icons.Filled.DirectionsWalk) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProgressRing(
                        progress = stepProgress,
                        label = "$steps",
                        size = 120.dp,
                        stroke = 12.dp,
                    )
                    Column {
                        Text("Goal $stepGoal", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${(stepProgress * 100).toInt()}% · ${fitness?.sourceLabel ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        fitness?.distanceMeters?.let {
                            Text("%.1f km".format(it / 1000.0), style = MaterialTheme.typography.bodyMedium)
                        }
                        fitness?.caloriesBurned?.let {
                            Text("%.0f kcal".format(it), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = manualSteps,
                        onValueChange = { manualSteps = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("Manual steps") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                    OutlinedButton(
                        onClick = {
                            manualSteps.toIntOrNull()?.let { s ->
                                fitness = (fitness ?: HealthSnapshot(0, null, 0)).copy(
                                    steps = s,
                                    sourceLabel = "Manual",
                                )
                            }
                        },
                    ) { Text("Apply") }
                }
            }

            SectionCard(title = stringResource(R.string.exercise_of_the_day), icon = Icons.Filled.FitnessCenter) {
                Text(eod.title, style = MaterialTheme.typography.titleMedium)
                Text("${eod.type.label} · ${eod.durationMinutes} min")
                Text(eod.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                workoutRepository.save(
                                    WorkoutEntry(
                                        timestampMillis = System.currentTimeMillis(),
                                        type = eod.type,
                                        durationMinutes = eod.durationMinutes,
                                        notes = "Exercise of the Day: ${eod.title}",
                                        fromExerciseOfDay = true,
                                    ),
                                )
                                eodDoneToday = true
                            }
                        },
                        enabled = !eodDoneToday,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (eodDoneToday) "Done ✓" else "Start / Done") }
                }
            }

            SectionCard(title = stringResource(R.string.workout_log), icon = Icons.Filled.MonitorHeart) {
                Text("Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutType.entries.forEach { t ->
                        FilterChip(
                            selected = logType == t,
                            onClick = { logType = t },
                            label = { Text(t.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = logMinutes,
                    onValueChange = { logMinutes = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Duration (min)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = logCalories,
                    onValueChange = { logCalories = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Calories (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = logNotes,
                    onValueChange = { logNotes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 1,
                )
                Button(
                    onClick = {
                        val mins = logMinutes.toIntOrNull() ?: return@Button
                        scope.launch {
                            workoutRepository.save(
                                WorkoutEntry(
                                    timestampMillis = System.currentTimeMillis(),
                                    type = logType,
                                    durationMinutes = mins.coerceIn(1, 600),
                                    calories = logCalories.toIntOrNull(),
                                    notes = logNotes.trim(),
                                ),
                            )
                            logNotes = ""
                            logCalories = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (logMinutes.toIntOrNull() ?: 0) > 0,
                ) { Text("Save workout") }

                if (workouts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Recent", style = MaterialTheme.typography.labelLarge)
                    workouts.take(8).forEach { w ->
                        Text(
                            "${formatTs(w.timestampMillis)} · ${w.type.label} · ${w.durationMinutes} min" +
                                (w.calories?.let { " · ${it} kcal" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            SectionCard(title = "This week", icon = Icons.Filled.FitnessCenter) {
                Text("${weekWorkouts.size} workouts · $weekMinutes minutes")
                Text("Steps today: $steps / $stepGoal")
                Text("Active minutes (est.): ${fitness?.activeMinutes ?: 0}")
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
                Text("Latest BPM: ${fitness?.heartRateBpm ?: "—"}")
                Text(
                    "From Health Connect when permitted; otherwise a calm demo value keeps the UI usable offline.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OutlinedButton(
                onClick = { reload() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Refresh data") }
        }
    }
}

private suspend fun refresh(
    repo: HealthDataRepository,
    apply: (HealthConnectStatus, HealthSnapshot?, SleepSnapshot?, Boolean) -> Unit,
) {
    val status = repo.availabilityStatus()
    apply(status, repo.latestFitness(), repo.latestSleep(), repo.hasAllPermissions())
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("EEE HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}
