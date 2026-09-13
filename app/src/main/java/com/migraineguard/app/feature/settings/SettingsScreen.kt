package com.migraineguard.app.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.domain.WaterUnit
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.reminders.ReminderWorker
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    driveSyncRepository: DriveSyncRepository,
    healthDataRepository: HealthDataRepository,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onOpenMeds: () -> Unit = {},
    focusGoogle: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var lastSync by remember { mutableStateOf("—") }
    var driveStatus by remember { mutableStateOf("") }
    var googleId by remember { mutableStateOf("") }
    var showSha by remember { mutableStateOf(false) }
    var weightGoalText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val notifPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            ReminderWorker.schedule(context, true)
            Toast.makeText(context, "Reminders enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(prefs.driveSyncEnabled) {
        lastSync = driveSyncRepository.lastSyncLabel()
        driveStatus = driveSyncRepository.statusMessage()
    }
    LaunchedEffect(prefs.googleWebClientId) {
        googleId = prefs.googleWebClientId
    }
    LaunchedEffect(prefs.weightGoalKg) {
        weightGoalText = prefs.weightGoalKg?.let { String.format("%.1f", it) } ?: ""
    }
    LaunchedEffect(focusGoogle) {
        // scroll cue via snackbar-ish toast
        if (focusGoogle) {
            Toast.makeText(context, "Paste Google Web Client ID below", Toast.LENGTH_LONG).show()
        }
    }

    fun rescheduleReminders() {
        val any = prefs.notificationsEnabled &&
            (prefs.waterReminders || prefs.migraineReminders || prefs.medicationReminders)
        if (any && Build.VERSION.SDK_INT >= 33 &&
            !ReminderWorker.canPostNotifications(context)
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ReminderWorker.schedule(context, any)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            SectionCard(title = "Google Sign-In", icon = Icons.Filled.Key) {
                Text(
                    "Google Sign-In is still demo/optional until a Web Client ID is baked later. Paste one here (…apps.googleusercontent.com) to try real sign-in — runtime value overrides BuildConfig. Also create an Android OAuth client for package com.migraineguard.app.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = googleId,
                    onValueChange = { googleId = it },
                    label = { Text("Google Web Client ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setGoogleWebClientId(googleId)
                            Toast.makeText(context, "Client ID saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save client ID") }
                OutlinedButton(
                    onClick = { showSha = !showSha },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (showSha) "Hide debug SHA-1 instructions" else "Show debug SHA-1 instructions") }
                if (showSha) {
                    Text(
                        "Box debug keystore SHA-1:\nFD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14\n\n" +
                            "Android Studio local builds often use a different debug keystore — run:\n" +
                            "keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android\n\n" +
                            "Add both SHA-1 fingerprints to your Android OAuth client if you switch machines.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SectionCard(title = "Profile", icon = Icons.Filled.Person) {
                Text(
                    prefs.bodyProfile.displayName.ifBlank { "Not set" } +
                        (prefs.bodyProfile.bmi?.let { " · BMI %.1f".format(it) } ?: ""),
                )
                OutlinedButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit profile")
                }
                OutlinedButton(onClick = onOpenBody, modifier = Modifier.fillMaxWidth()) {
                    Text("Body metrics / weight log")
                }
                OutlinedButton(onClick = onOpenMeds, modifier = Modifier.fillMaxWidth()) {
                    Text("Medications & supplements")
                }
            }

            SectionCard(title = "Appearance", icon = Icons.Filled.DarkMode) {
                Text("Theme", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { scope.launch { preferencesRepository.setThemeMode(mode) } },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase() }) },
                        )
                    }
                }
                PrefSwitch(
                    label = "Dynamic color (Android 12+)",
                    checked = prefs.dynamicColor,
                    onCheckedChange = { scope.launch { preferencesRepository.setDynamicColor(it) } },
                )
            }

            SectionCard(title = "Goals hub", icon = Icons.Filled.FitnessCenter) {
                Text("Step goal: ${prefs.dailyStepGoal}")
                Slider(
                    value = prefs.dailyStepGoal.toFloat(),
                    onValueChange = {
                        scope.launch { preferencesRepository.setDailyStepGoal(it.roundToInt()) }
                    },
                    valueRange = 3000f..15000f,
                    steps = 11,
                )
                Text("Water goal: ${prefs.waterGoalCups} cups")
                Slider(
                    value = prefs.waterGoalCups.toFloat(),
                    onValueChange = {
                        scope.launch { preferencesRepository.setWaterGoalCups(it.roundToInt()) }
                    },
                    valueRange = 4f..16f,
                    steps = 11,
                )
                Text("Sleep goal: ${"%.1f".format(prefs.sleepGoalHours)} hours")
                Slider(
                    value = prefs.sleepGoalHours,
                    onValueChange = {
                        scope.launch { preferencesRepository.setSleepGoalHours(it) }
                    },
                    valueRange = 5f..10f,
                    steps = 9,
                )
                OutlinedTextField(
                    value = weightGoalText,
                    onValueChange = { weightGoalText = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                    label = { Text("Weight goal kg (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setWeightGoalKg(weightGoalText.toFloatOrNull())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save weight goal") }
            }

            SectionCard(title = "Water & units", icon = Icons.Filled.WaterDrop) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WaterUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = prefs.waterUnit == unit,
                            onClick = { scope.launch { preferencesRepository.setWaterUnit(unit) } },
                            label = { Text(unit.name) },
                        )
                    }
                }
                PrefSwitch(
                    label = "Temperature in °C",
                    checked = prefs.useCelsius,
                    onCheckedChange = { scope.launch { preferencesRepository.setUseCelsius(it) } },
                )
            }

            SectionCard(title = "Reminders", icon = Icons.Filled.Notifications) {
                PrefSwitch(
                    label = "Enable notifications",
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = {
                        scope.launch {
                            preferencesRepository.setNotificationsEnabled(it)
                            rescheduleReminders()
                        }
                    },
                )
                PrefSwitch(
                    label = "Water reminders",
                    checked = prefs.waterReminders,
                    onCheckedChange = {
                        scope.launch {
                            preferencesRepository.setWaterReminders(it)
                            rescheduleReminders()
                        }
                    },
                )
                PrefSwitch(
                    label = "Medication reminders",
                    checked = prefs.medicationReminders,
                    onCheckedChange = {
                        scope.launch {
                            preferencesRepository.setMedicationReminders(it)
                            rescheduleReminders()
                        }
                    },
                )
                PrefSwitch(
                    label = "Migraine check-in reminders",
                    checked = prefs.migraineReminders,
                    onCheckedChange = {
                        scope.launch {
                            preferencesRepository.setMigraineReminders(it)
                            rescheduleReminders()
                        }
                    },
                )
                Text(
                    "Uses WorkManager periodic reminders (every ~6h) when toggles are on and POST_NOTIFICATIONS is granted on Android 13+.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Samsung Health / Health Connect", icon = Icons.Filled.Watch) {
                Text(
                    "Grant permissions on the Fitness tab. Sideloaded debug apps appear in Connected apps after the first successful permission request.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = {
                        runCatching { context.startActivity(healthDataRepository.openHealthConnectSettingsIntent()) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Health Connect") }
                healthDataRepository.openSamsungHealthSettingsIntent()?.let { intent ->
                    OutlinedButton(
                        onClick = { runCatching { context.startActivity(intent) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open Samsung Health") }
                }
            }

            SectionCard(title = "Google Drive sync", icon = Icons.Filled.Cloud) {
                Text(driveStatus, style = MaterialTheme.typography.bodyMedium)
                Text("Last sync: $lastSync", modifier = Modifier.padding(top = 8.dp))
                PrefSwitch(
                    label = "Enable Drive sync",
                    checked = prefs.driveSyncEnabled,
                    onCheckedChange = { checked ->
                        scope.launch {
                            driveSyncRepository.setSyncEnabled(checked)
                            lastSync = driveSyncRepository.lastSyncLabel()
                            driveStatus = driveSyncRepository.statusMessage()
                        }
                    },
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            lastSync = driveSyncRepository.requestSyncNow()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Sync now (queued)") }
            }

            SectionCard(title = "About", icon = Icons.Filled.Info) {
                Text("MigraineGuard v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Text("Package ${BuildConfig.APPLICATION_ID}")
                Text("AI keys: Gemini=${BuildConfig.GEMINI_API_KEY.isNotBlank()} · Groq=${BuildConfig.GROQ_API_KEY.isNotBlank()} · OpenRouter=${BuildConfig.OPENROUTER_API_KEY.isNotBlank()}")
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jdp323/migraineguard")),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("GitHub") }
            }

            SectionCard(title = "Privacy", icon = Icons.Filled.PrivacyTip) {
                Text(
                    "Logs stay on-device (Room + DataStore) unless you enable Drive sync. " +
                        "Weather uses Open-Meteo without an API key. AI calls only happen when you analyze food or request insight narratives. " +
                        "Not a medical device — for personal awareness only.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

@Composable
private fun PrefSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
