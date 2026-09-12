package com.migraineguard.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.domain.WaterUnit
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository
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
    modifier: Modifier = Modifier,
) {
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var lastSync by remember { mutableStateOf("—") }
    var driveStatus by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(prefs.driveSyncEnabled) {
        lastSync = driveSyncRepository.lastSyncLabel()
        driveStatus = driveSyncRepository.statusMessage()
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

            SectionCard(title = "Water & units", icon = Icons.Filled.WaterDrop) {
                Text("Daily goal: ${prefs.waterGoalCups} cups")
                Slider(
                    value = prefs.waterGoalCups.toFloat(),
                    onValueChange = {
                        scope.launch { preferencesRepository.setWaterGoalCups(it.roundToInt()) }
                    },
                    valueRange = 4f..16f,
                    steps = 11,
                )
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

            SectionCard(title = "Notifications", icon = Icons.Filled.Notifications) {
                PrefSwitch(
                    label = "Enable notifications (preference)",
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = { scope.launch { preferencesRepository.setNotificationsEnabled(it) } },
                )
                PrefSwitch(
                    label = "Water reminders",
                    checked = prefs.waterReminders,
                    onCheckedChange = { scope.launch { preferencesRepository.setWaterReminders(it) } },
                )
                PrefSwitch(
                    label = "Migraine check-in reminders",
                    checked = prefs.migraineReminders,
                    onCheckedChange = { scope.launch { preferencesRepository.setMigraineReminders(it) } },
                )
                Text(
                    "Reminder delivery is preference-backed in v1; wire WorkManager / notifications later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionCard(title = "Samsung Health / Health Connect", icon = Icons.Filled.Watch) {
                Text(
                    "Link Samsung Health → Health Connect so Galaxy Watch steps, HR, and sleep sync into MigraineGuard.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = {
                        runCatching { context.startActivity(healthDataRepository.openHealthConnectSettingsIntent()) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Open Health Connect") }
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
                Text("MigraineGuard v${BuildConfig.VERSION_NAME}")
                Text("Package ${BuildConfig.APPLICATION_ID}")
                Text("AI keys: Gemini=${BuildConfig.GEMINI_API_KEY.isNotBlank()} · Groq=${BuildConfig.GROQ_API_KEY.isNotBlank()} · OpenRouter=${BuildConfig.OPENROUTER_API_KEY.isNotBlank()}")
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
