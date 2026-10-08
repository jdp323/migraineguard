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
import androidx.compose.material.icons.filled.SmartToy
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
import com.migraineguard.app.feature.backup.BackupRepository
import com.migraineguard.app.feature.backup.BackupCard
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.security.installedSigningSha1
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.data.Result
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
    backupRepository: BackupRepository,
    healthDataRepository: HealthDataRepository,
    aiClient: FallbackAiClient? = null,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onOpenMeds: () -> Unit = {},
    onConnectGemini: () -> Unit = {},
    focusGoogle: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var googleId by remember { mutableStateOf("") }
    var showSha by remember { mutableStateOf(false) }
    var weightGoalText by remember { mutableStateOf("") }
    var geminiKey by remember { mutableStateOf("") }
    var groqKey by remember { mutableStateOf("") }
    var openRouterKey by remember { mutableStateOf("") }
    var aiTestStatus by remember { mutableStateOf("") }
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

    LaunchedEffect(prefs.googleWebClientId) {
        googleId = prefs.googleWebClientId
    }
    LaunchedEffect(prefs.geminiApiKey, prefs.groqApiKey, prefs.openRouterApiKey) {
        geminiKey = prefs.geminiApiKey
        groqKey = prefs.groqApiKey
        openRouterKey = prefs.openRouterApiKey
    }
    LaunchedEffect(prefs.weightGoalKg) {
        weightGoalText = prefs.weightGoalKg?.let { String.format("%.1f", it) } ?: ""
    }
    LaunchedEffect(focusGoogle) {
        // scroll cue via snackbar-ish toast
        if (focusGoogle) {
            Toast.makeText(context, "Optional: override Web Client ID below (baked ID already works)", Toast.LENGTH_LONG).show()
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
                    "A default Google Web client ID is included. The app owner must register the installed APK certificate in the same Google project. " +
                        "Optional override below. Google Sign-In is not Gemini access. " +
                        "If Sign-In fails with SHA-1 / certificate errors, register this APK SHA-1 on the Android OAuth client.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Baked default: " + BuildConfig.GOOGLE_WEB_CLIENT_ID.take(36) + "…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = googleId,
                    onValueChange = { googleId = it },
                    label = { Text("Override Google Web Client ID (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setGoogleWebClientId(googleId)
                            Toast.makeText(context, "Client ID override saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save override") }
                OutlinedButton(
                    onClick = { showSha = !showSha },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (showSha) "Hide SHA-1 instructions" else "Show SHA-1 (fix mismatch)") }
                if (showSha) {
                    Text(
                        "Installed APK SHA-1:\n${installedSigningSha1(context)}\n\n" +
                            "Package: ${BuildConfig.APPLICATION_ID}\n\n" +
                            "Register this certificate with an Android OAuth client in the same Google project " +
                            "as the Web client ID. Changing signing keys requires another registration.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SectionCard(title = "AI providers", icon = Icons.Filled.SmartToy) {
                Text(
                    "Uses YOUR keys only — never developer tokens or the Firebase api_key. Order: Gemini → Groq → OpenRouter → OfflineHealthBrain. " +
                        "Cloud AI runs only with keys you paste. Without a key: OfflineHealthBrain + Connect Gemini CTA. Google Sign-In ≠ Gemini.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini API key") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Text(
                    "Get free Gemini key: aistudio.google.com/apikey",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Google AI Studio") }
                OutlinedTextField(
                    value = groqKey,
                    onValueChange = { groqKey = it },
                    label = { Text("Groq API key") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = openRouterKey,
                    onValueChange = { openRouterKey = it },
                    label = { Text("OpenRouter API key") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        scope.launch {
                            preferencesRepository.setGeminiApiKey(geminiKey)
                            preferencesRepository.setGroqApiKey(groqKey)
                            preferencesRepository.setOpenRouterApiKey(openRouterKey)
                            Toast.makeText(context, "AI keys saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save AI keys") }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            aiTestStatus = "Testing…"
                            preferencesRepository.setGeminiApiKey(geminiKey)
                            val client = aiClient
                            if (client == null) {
                                aiTestStatus = "AI client unavailable"
                                return@launch
                            }
                            when (val r = client.testGemini { preferencesRepository.getGeminiApiKey() }) {
                                is Result.Success -> aiTestStatus = "Gemini OK: ${r.data.take(80)}"
                                is Result.Error -> {
                                    // Offline still counts as usable
                                    val offline = client.complete("Say hi offline briefly")
                                    aiTestStatus = when (offline) {
                                        is Result.Success -> "Gemini failed (${r.message}); OfflineHealthBrain OK"
                                        is Result.Error -> "Test failed: ${r.message}"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Test connection") }
                if (aiTestStatus.isNotBlank()) {
                    Text(aiTestStatus, style = MaterialTheme.typography.bodySmall)
                }
                val gConfigured = prefs.geminiApiKey.isNotBlank()
                val qConfigured = prefs.groqApiKey.isNotBlank()
                val oConfigured = prefs.openRouterApiKey.isNotBlank()
                Text(
                    "Your keys: Gemini=$gConfigured · Groq=$qConfigured · OpenRouter=$oConfigured · Offline=always",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(
                    onClick = onConnectGemini,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Connect Gemini screen") }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            preferencesRepository.clearAllAiKeys()
                            geminiKey = ""
                            groqKey = ""
                            openRouterKey = ""
                            Toast.makeText(context, "AI keys removed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Remove all my AI keys") }
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

            BackupCard(backupRepository)

            SectionCard(title = "Cloud backup", icon = Icons.Filled.Cloud) {
                Text("Use Export encrypted backup above and choose a Google Drive folder in Android's file picker. Import that file to restore it on another installation.")
                Text("This is a manual encrypted backup. Automatic background Drive synchronization is not enabled.",
                    style = MaterialTheme.typography.bodySmall)
            }

            SectionCard(title = "About", icon = Icons.Filled.Info) {
                Text("MigraineGuard v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Text("Package ${BuildConfig.APPLICATION_ID}")
                Text("AI: your Gemini/Groq/OpenRouter keys only; OfflineHealthBrain always on")
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
                    "Logs stay on this device unless you export a backup or choose to share a health summary with cloud AI. " +
                        "Weather shares approximate coordinates with Open-Meteo. Cloud AI uses your encrypted stored keys; food photos are sent to Gemini only when you select that option. " +
                        "OfflineHealthBrain always available. Not a medical device — for personal awareness only.",
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
