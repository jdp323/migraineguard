package com.migraineguard.app.feature.settings

import com.migraineguard.app.core.ui.AppTopBar

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.CancellationException
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
import com.migraineguard.app.core.ui.FeatureHeader
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.reminders.ReminderWorker
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    backupRepository: BackupRepository,
    healthDataRepository: HealthDataRepository,
    aiClient: FallbackAiClient? = null,
    driveRepository: DriveSyncRepository? = null,
    onConnectDrive: () -> Unit = {},
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onOpenBody: () -> Unit = {},
    onOpenMeds: () -> Unit = {},
    onConnectGemini: () -> Unit = {},
    initialGroup: String = "Account",
    focusGoogle: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var settingsGroup by androidx.compose.runtime.saveable.rememberSaveable(initialGroup) { mutableStateOf(initialGroup) }
    var googleId by remember { mutableStateOf("") }
    var showSha by remember { mutableStateOf(false) }
    var showGoogleSetup by remember { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }
    var signOutError by remember { mutableStateOf<String?>(null) }
    var weightGoalText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(prefs.googleWebClientId) {
        googleId = prefs.googleWebClientId
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

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
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
            Text("Make it yours.", style = MaterialTheme.typography.headlineSmall)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Account", "AI", "Reminders", "Data", "App").forEach { group -> FilterChip(selected = settingsGroup == group, onClick = { settingsGroup = group }, label = { Text(group) }) }
            }
            if (settingsGroup == "App") AppUpdateCard()
            if (settingsGroup == "Account") {
            driveRepository?.let { DriveBackupCard(it, onConnectDrive) }

            }
            if (settingsGroup == "Account") {
            SectionCard(title = "Google Sign-In", icon = Icons.Filled.Key) {
                Text("Google sign-in keeps your profile separate from other accounts on this phone.")
                TextButton(onClick = { showGoogleSetup = !showGoogleSetup }) {
                    Text(if (showGoogleSetup) "Hide advanced setup" else "Advanced sign-in setup")
                }
                if (showGoogleSetup) {
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

                }

            }
            if (settingsGroup == "AI") {
            AiProviderSettings(preferencesRepository)

            }
            if (settingsGroup == "Account") {
            SectionCard(title = "Profile", icon = Icons.Filled.Person) {
                Text(
                    prefs.bodyProfile.displayName.ifBlank { "Not set" } +
                        (prefs.bodyProfile.bmi?.let { " · BMI %.1f".format(it) } ?: ""),
                )
                OutlinedButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit profile")
                }
                OutlinedButton(onClick = onOpenBody, modifier = Modifier.fillMaxWidth()) {
                    Text("Body progress / photos / weight")
                }
                OutlinedButton(onClick = onOpenMeds, modifier = Modifier.fillMaxWidth()) {
                    Text("Medications & supplements")
                }
            }

            }
            if (settingsGroup == "App") {
            SectionCard(title = "Appearance", icon = Icons.Filled.DarkMode) {
                PrefSwitch("Reduce transparency", prefs.reduceTransparency) { scope.launch { preferencesRepository.setReduceTransparency(it) } }
                Text("Glass surfaces keep text sharp. Reduce transparency gives cards a solid fill.",style=MaterialTheme.typography.bodySmall)
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

            }
            if (settingsGroup == "App") {
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

            }
            if (settingsGroup == "App") {
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

            }
            if (settingsGroup == "Reminders") {
            ReminderSettings(preferencesRepository)

            }
            if (settingsGroup == "Account") {
            SectionCard(title = "Health Connect / devices", icon = Icons.Filled.Watch) {
                Text(
                    "Connect compatible Samsung, Garmin, Fitbit / Google Health and other apps through Android Health Connect. Enable each source to write records, then grant MigraineGuard read access in Fitness. All Health Connect sources is the default for new profiles.",
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

            }
            if (settingsGroup == "Data") {
            com.migraineguard.app.feature.backup.DataExportCard(backupRepository)
            BackupCard(backupRepository)

            }
            if (settingsGroup == "App") {
            SectionCard(title = "Help & documentation", icon = Icons.Filled.Info) {
                listOf("User guide" to "USER_GUIDE.md", "AI prompts & permissions" to "AI_AND_PERMISSIONS.md", "Errors & fixes" to "TROUBLESHOOTING.md").forEach { (label, file) ->
                    OutlinedButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jdp323/migraineguard/blob/main/docs/"+file))) } }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            }
            SectionCard(title = "About", icon = Icons.Filled.Info) {
                Text("MigraineGuard v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Text("Package ${BuildConfig.APPLICATION_ID}")
                Text("AI: your selected provider and key; on-device fallback available")
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

            }
            if (settingsGroup == "Account") {
            SectionCard(title = "Privacy", icon = Icons.Filled.PrivacyTip) {
                Text(
                    "Logs stay on this device unless you enable Drive backup, export a backup or choose to share a health summary with cloud AI. " +
                        "Body progress photos remain local unless you separately enable including them in your own Drive backup; they are never sent to AI. There is no developer health-data server. Weather shares approximate coordinates with Open-Meteo. Cloud AI uses your encrypted stored keys; photos are sent to your selected image-capable provider only when you choose. " +
                        "On-device assistance is always available. Not a medical device — for personal awareness only.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            }
            if (settingsGroup == "Account") {
            Button(onClick = {
                signingOut = true
                scope.launch {
                    try {
                        if (driveRepository?.state?.value?.connected == true) driveRepository.backupNow()
                        onSignOut()
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) { signOutError = e.message ?: "Drive backup could not finish." }
                    finally { signingOut = false }
                }
            }, enabled = !signingOut, modifier = Modifier.fillMaxWidth()) {
                Text(if (signingOut) "Saving before sign-out…" else "Sign out")
            }
            signOutError?.let { message ->
                AlertDialog(onDismissRequest = { signOutError = null }, title = { Text("Backup needs attention") },
                    text = { Text(message + " Your details are still saved on this phone. Reconnect Drive and try again, or sign out using the local copy.") },
                    confirmButton = { TextButton(onClick = { signOutError = null }) { Text("Keep me signed in") } },
                    dismissButton = { TextButton(onClick = { signOutError = null; onSignOut() }) { Text("Sign out with local copy") } })
            }
            }
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
