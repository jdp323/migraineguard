package com.migraineguard.app.feature.track

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.health.connect.client.HealthConnectClient
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
import com.migraineguard.app.feature.fitness.HealthConnectDiagnostics
import com.migraineguard.app.feature.fitness.HealthConnectPackages
import com.migraineguard.app.feature.fitness.HealthConnectStatus
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.WorkoutRepository
import com.migraineguard.app.feature.fitness.findActivity
import com.migraineguard.app.feature.fitness.startHealthActivity
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
    var sdkRaw by remember { mutableIntStateOf(-1) }
    var lastError by remember { mutableStateOf("") }
    var providerPkg by remember { mutableStateOf(HealthConnectPackages.GOOGLE) }
    var diag by remember {
        mutableStateOf(
            HealthConnectDiagnostics(
                sdkStatusRaw = -1,
                sdkStatusName = "—",
                providerPackage = HealthConnectPackages.GOOGLE,
                lastError = "",
                googleHcInstalled = false,
                samsungHealthInstalled = false,
            ),
        )
    }
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
    val activity = context.findActivity()
    val launchContext = activity ?: context
    val snackbar = remember { SnackbarHostState() }

    fun applyDiag() {
        val d = healthDataRepository.diagnostics(launchContext)
        diag = d
        sdkRaw = d.sdkStatusRaw
        providerPkg = d.providerPackage
        lastError = d.lastError
    }

    fun handleGranted(granted: Set<String>) {
        scope.launch {
            refresh(healthDataRepository) { s, f, sl, p ->
                status = s; fitness = f; sleep = sl; permitted = p
            }
            applyDiag()
            if (granted.isNotEmpty() && healthDataRepository.hasAllPermissions()) {
                Toast.makeText(launchContext, "Health Connect connected ✓", Toast.LENGTH_SHORT).show()
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar("Permissions granted — MigraineGuard should appear in Health Connect Connected apps.")
            } else if (granted.isNotEmpty()) {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar("Some permissions granted. You can grant the rest anytime.")
            } else {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar("No permissions granted. Sideloaded debug apps appear after the first successful request.")
            }
        }
    }

    val providerLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(
            providerPackageName = HealthConnectPackages.GOOGLE,
        ),
    ) { granted -> handleGranted(granted) }

    val fallbackLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted -> handleGranted(granted) }

    fun startIntents(intents: List<Intent>, alsoSamsung: Boolean = false) {
        var opened = false
        for (intent in intents) {
            try {
                launchContext.startHealthActivity(intent)
                opened = true
                break
            } catch (e: Exception) {
                healthDataRepository.recordError("${e::class.simpleName}: ${e.message ?: e}")
            }
        }
        if (alsoSamsung) {
            healthDataRepository.openSamsungHealthSettingsIntent()?.let { samsung ->
                runCatching { launchContext.startHealthActivity(samsung) }
                    .onFailure { healthDataRepository.recordError("Samsung Health: ${it.message ?: it}") }
            }
        }
        if (!opened) {
            scope.launch { snackbar.showSnackbar("Could not open Health Connect / Play Store") }
        }
        applyDiag()
    }

    fun openHcFallback() {
        startIntents(
            healthDataRepository.openHealthConnectInstallOrSettingsIntents() +
                healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName),
            alsoSamsung = true,
        )
    }

    fun launchPermissions(perms: Set<String>) {
        try {
            providerLauncher.launch(perms)
        } catch (e: ActivityNotFoundException) {
            val msg = "ActivityNotFoundException: ${e.message ?: e}"
            healthDataRepository.recordError(msg)
            lastError = msg
            try {
                fallbackLauncher.launch(perms)
            } catch (e2: Exception) {
                val msg2 = "${e2::class.simpleName}: ${e2.message ?: e2}"
                healthDataRepository.recordError(msg2)
                lastError = msg2
                Toast.makeText(launchContext, msg2, Toast.LENGTH_LONG).show()
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar(msg2)
                }
                startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
            }
        } catch (e: IllegalStateException) {
            val msg = "IllegalStateException: ${e.message ?: e}"
            healthDataRepository.recordError(msg)
            lastError = msg
            Toast.makeText(launchContext, msg, Toast.LENGTH_LONG).show()
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(msg)
            }
            startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message ?: e}"
            healthDataRepository.recordError(msg)
            lastError = msg
            Toast.makeText(launchContext, msg, Toast.LENGTH_LONG).show()
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(msg)
            }
            startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
        }
    }

    fun connectHealth() {
        // Immediate feedback — never a dead button.
        Toast.makeText(launchContext, "Checking Health Connect…", Toast.LENGTH_SHORT).show()
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar("Checking Health Connect…")
        }
        scope.launch {
            val raw = healthDataRepository.sdkStatusRaw(launchContext)
            sdkRaw = raw
            providerPkg = healthDataRepository.providerPackage()
            status = healthDataRepository.availabilityStatus()
            applyDiag()
            when (raw) {
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    val msg = "Health Connect needs install/update — opening Play Store…"
                    healthDataRepository.recordError("SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED")
                    lastError = healthDataRepository.lastError()
                    Toast.makeText(launchContext, msg, Toast.LENGTH_LONG).show()
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(msg)
                    }
                    startIntents(healthDataRepository.playStoreUpdateIntents(), alsoSamsung = true)
                }
                HealthConnectClient.SDK_AVAILABLE -> {
                    val perms = healthDataRepository.requiredPermissions()
                    if (perms.isEmpty()) {
                        val msg = "Could not build permission set — opening Health Connect settings…"
                        healthDataRepository.recordError(msg)
                        lastError = msg
                        Toast.makeText(launchContext, msg, Toast.LENGTH_LONG).show()
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            snackbar.showSnackbar(msg)
                        }
                        startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
                    } else {
                        scope.launch {
                            snackbar.currentSnackbarData?.dismiss()
                            snackbar.showSnackbar("Opening Health Connect permissions…")
                        }
                        launchPermissions(perms)
                    }
                }
                else -> {
                    val msg = "Health Connect ${healthDataRepository.sdkStatusName(raw)} ($raw)"
                    healthDataRepository.recordError(msg)
                    lastError = msg
                    Toast.makeText(launchContext, msg, Toast.LENGTH_LONG).show()
                    scope.launch {
                        snackbar.currentSnackbarData?.dismiss()
                        snackbar.showSnackbar(msg)
                    }
                    openHcFallback()
                }
            }
            applyDiag()
        }
    }

    fun reload() {
        scope.launch {
            refresh(healthDataRepository) { s, f, sl, p ->
                status = s; fitness = f; sleep = sl; permitted = p
            }
            applyDiag()
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
                        onClick = {
                            runCatching { launchContext.startHealthActivity(it) }
                                .onFailure { e ->
                                    healthDataRepository.recordError("Samsung Health: ${e.message ?: e}")
                                    applyDiag()
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open Samsung Health") }
                }
            }

            SectionCard(title = "Health Connect status", icon = Icons.Filled.Info) {
                Text("SDK status: ${diag.sdkStatusName}  (raw $sdkRaw)", style = MaterialTheme.typography.bodyMedium)
                Text("Provider package: $providerPkg", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Google HC installed: ${diag.googleHcInstalled} · Samsung Health: ${diag.samsungHealthInstalled}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (lastError.isNotBlank()) {
                    Text(
                        "Last error: $lastError",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        "No blocking errors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                    val soft = s.sourceLabel.contains("permission", ignoreCase = true) ||
                        s.sourceLabel.contains("unavailable", ignoreCase = true) ||
                        s.sourceLabel.contains("no sessions", ignoreCase = true)
                    if (s.hours > 0f) {
                        Text("Duration: ${"%.1f".format(s.hours)} hours")
                        Text("Quality: ${s.qualityLabel}")
                    } else {
                        Text(
                            if (soft) s.sourceLabel else "No sleep data yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "Source: ${s.sourceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (soft) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    if (soft) {
                        Text(
                            "Sleep is optional — fitness data still works. Grant Sleep in Health Connect when ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
