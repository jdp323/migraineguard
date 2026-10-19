package com.migraineguard.app.feature.setup

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.R
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.ActivityLevel
import com.migraineguard.app.feature.profile.ProfileSetupScreen
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.SexGender
import com.migraineguard.app.core.ui.DateOfBirthField
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.auth.SignInScreen
import com.migraineguard.app.feature.auth.AuthRepository
import com.migraineguard.app.feature.fitness.HealthConnectPackages
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.findActivity
import com.migraineguard.app.feature.fitness.startHealthActivity
import kotlinx.coroutines.launch

private enum class WizardStep(val title: String, val index: Int) {
    WELCOME("Welcome", 0),
    SIGN_IN("Sign in", 1),
    HEALTH("Health Connect", 2),
    GEMINI("Gemini AI", 3),
    PROFILE("Profile", 4),
    DONE("Done", 5),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    authRepository: AuthRepository,
    preferencesRepository: UserPreferencesRepository,
    healthDataRepository: HealthDataRepository,
    aiClient: FallbackAiClient,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authState by authRepository.authState.collectAsStateWithLifecycle()
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var step by remember { mutableStateOf(if (authState.isSignedIn) WizardStep.HEALTH else WizardStep.WELCOME) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Advance past sign-in once authenticated
    LaunchedEffect(authState.isSignedIn, step) {
        if (step == WizardStep.SIGN_IN && authState.isSignedIn) {
            step = WizardStep.HEALTH
        }
    }

    val progress = (step.index + 1).toFloat() / WizardStep.entries.size

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Setup · ${step.title}")
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (step) {
            WizardStep.WELCOME -> WelcomeStep(
                modifier = Modifier.padding(padding),
                onNext = { step = WizardStep.SIGN_IN },
            )
            WizardStep.SIGN_IN -> SignInStep(
                modifier = Modifier.padding(padding),
                authRepository = authRepository,
                onNeedGoogleSetup = {
                    scope.launch {
                        snackbar.showSnackbar(
                            "Google sign-in needs project setup. Continue locally while it is configured.",
                        )
                    }
                },
            )
            WizardStep.HEALTH -> HealthConnectStep(
                modifier = Modifier.padding(padding),
                healthDataRepository = healthDataRepository,
                snackbar = snackbar,
                onNext = { step = WizardStep.GEMINI },
                onSkip = {
                    scope.launch {
                        preferencesRepository.setHealthConnectSkipped(true)
                        step = WizardStep.GEMINI
                    }
                },
            )
            WizardStep.GEMINI -> GeminiStep(
                modifier = Modifier.padding(padding),
                preferencesRepository = preferencesRepository,
                aiClient = aiClient,
                onNext = { step = WizardStep.PROFILE },
                onSkip = { step = WizardStep.PROFILE },
            )
            WizardStep.PROFILE -> ProfileStep(
                modifier = Modifier.padding(padding),
                preferencesRepository = preferencesRepository,
                prefilledName = authState.user?.displayName ?: prefs.bodyProfile.displayName,
                initial = prefs.bodyProfile,
                onNext = { step = WizardStep.DONE },
            )
            WizardStep.DONE -> DoneStep(
                modifier = Modifier.padding(padding),
                onFinish = {
                    scope.launch {
                        preferencesRepository.setSetupComplete(true)
                        onFinished()
                    }
                },
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Healing,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.app_version_label, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "MigraineGuard helps you map migraine episodes, hydrate with intention, " +
                "pull fitness & sleep from Health Connect / Samsung Health, spot weather & food patterns, " +
                "and chat with an AI coach — using only your own Gemini key when you want cloud AI.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Personal / educational tool — not a medical device. Not a substitute for professional care.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Start setup") }
    }
}

@Composable
private fun SignInStep(
    authRepository: AuthRepository,
    onNeedGoogleSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SignInScreen(
        authRepository = authRepository,
        onNeedGoogleSetup = onNeedGoogleSetup,
        modifier = modifier,
    )
}

@Composable
private fun HealthConnectStep(
    healthDataRepository: HealthDataRepository,
    snackbar: SnackbarHostState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val launchContext = activity ?: context
    val scope = rememberCoroutineScope()
    var sdkName by remember { mutableStateOf("—") }
    var permitted by remember { mutableStateOf(false) }
    var googleInstalled by remember { mutableStateOf(false) }
    var samsungInstalled by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf("") }

    fun refreshStatus() {
        val d = healthDataRepository.diagnostics(launchContext)
        sdkName = d.sdkStatusName
        googleInstalled = d.googleHcInstalled
        samsungInstalled = d.samsungHealthInstalled
        lastError = d.lastError
        scope.launch { permitted = healthDataRepository.hasAllPermissions() }
    }

    LaunchedEffect(Unit) { refreshStatus() }

    fun handleGranted(granted: Set<String>) {
        scope.launch {
            refreshStatus()
            permitted = healthDataRepository.hasAllPermissions()
            when {
                granted.isNotEmpty() && permitted ->
                    snackbar.showSnackbar("Permissions granted — MigraineGuard should appear in Connected apps.")
                granted.isNotEmpty() ->
                    snackbar.showSnackbar("Some permissions granted. You can grant the rest anytime.")
                else ->
                    snackbar.showSnackbar("No permissions granted. Try again or open Health Connect settings.")
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
            }
        }
        if (!opened) {
            scope.launch { snackbar.showSnackbar("Could not open Health Connect / Play Store") }
        }
        refreshStatus()
    }

    fun launchPermissions(perms: Set<String>) {
        try {
            providerLauncher.launch(perms)
        } catch (e: ActivityNotFoundException) {
            try {
                fallbackLauncher.launch(perms)
            } catch (e2: Exception) {
                val msg = "${e2::class.simpleName}: ${e2.message ?: e2}"
                healthDataRepository.recordError(msg)
                scope.launch { snackbar.showSnackbar(msg) }
                startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
            }
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message ?: e}"
            healthDataRepository.recordError(msg)
            scope.launch { snackbar.showSnackbar(msg) }
            startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
        }
    }

    fun connectHealth() {
        Toast.makeText(launchContext, "Checking Health Connect…", Toast.LENGTH_SHORT).show()
        scope.launch {
            healthDataRepository.allowDeviceHealth()
            snackbar.showSnackbar("Checking Health Connect…")
            val raw = healthDataRepository.sdkStatusRaw(launchContext)
            refreshStatus()
            when (raw) {
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    snackbar.showSnackbar("Health Connect needs install/update — opening Play Store…")
                    startIntents(healthDataRepository.playStoreUpdateIntents(), alsoSamsung = true)
                }
                HealthConnectClient.SDK_AVAILABLE -> {
                    val perms = healthDataRepository.requiredPermissions()
                    if (perms.isEmpty()) {
                        startIntents(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
                    } else {
                        snackbar.showSnackbar("Opening Health Connect permissions…")
                        launchPermissions(perms)
                    }
                }
                else -> {
                    snackbar.showSnackbar("Health Connect ${healthDataRepository.sdkStatusName(raw)}")
                    startIntents(
                        healthDataRepository.openHealthConnectInstallOrSettingsIntents(),
                        alsoSamsung = true,
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Filled.Watch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            "Health Connect / Samsung",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            "Health Connect belongs to this phone, independently of Google login. Connect only if the phone and watch records are yours. Skip anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        IllustratedStep(
            number = 1,
            title = "Install / open Health Connect",
            body = "If Health Connect isn’t installed, open Play Store. On many Samsung phones it’s a system app.",
        ) {
            OutlinedButton(
                onClick = {
                    startIntents(
                        healthDataRepository.playStoreUpdateIntents() +
                            healthDataRepository.openHealthConnectInstallOrSettingsIntents(),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Install / open Health Connect") }
        }

        IllustratedStep(
            number = 2,
            title = "Tap Connect / grant permissions",
            body = "Allow steps, heart rate, sleep, exercise, distance, and calories. Sleep is optional.",
        ) {
            Button(
                onClick = { connectHealth() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Connect / grant permissions") }
        }

        IllustratedStep(
            number = 3,
            title = "Enable Samsung Health → Health Connect sharing",
            body = "In Samsung Health: Settings → Health Connect → allow data sharing so Galaxy Watch data flows here.",
        ) {
            OutlinedButton(
                onClick = {
                    val samsung = healthDataRepository.openSamsungHealthSettingsIntent()
                    if (samsung != null) {
                        runCatching { launchContext.startHealthActivity(samsung) }
                    } else {
                        scope.launch {
                            snackbar.showSnackbar(
                                "Samsung Health not installed. Skip or install from Play Store.",
                            )
                        }
                        startIntents(
                            listOf(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=${HealthConnectPackages.SAMSUNG_HEALTH}"),
                                ),
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (samsungInstalled) "Open Samsung Health" else "Open Samsung Health (or Play Store)")
            }
        }

        IllustratedStep(
            number = 4,
            title = "Confirm status",
            body = "SDK: $sdkName · Permissions: ${if (permitted) "granted ✓" else "needed"} · " +
                "HC installed: ${if (googleInstalled) "yes" else "no"} · Samsung: ${if (samsungInstalled) "yes" else "no"}",
        ) {
            OutlinedButton(
                onClick = { refreshStatus() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Refresh status") }
            if (lastError.isNotBlank()) {
                Text(
                    lastError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text(if (permitted) "Continue" else "Continue anyway") }
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Skip Health Connect for now")
        }
    }
}

@Composable
private fun IllustratedStep(
    number: Int,
    title: String,
    body: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$number",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun GeminiStep(
    preferencesRepository: UserPreferencesRepository,
    aiClient: FallbackAiClient,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Icons.Filled.SmartToy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text("Your Gemini API key", style = MaterialTheme.typography.headlineSmall)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Text(
                "Cloud AI uses YOUR free Google AI Studio quota — never the app developer’s tokens. " +
                    "Google Sign-In does not grant Gemini. AI tips are educational, not medical advice.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(14.dp),
            )
        }

        IllustratedStep(
            number = 1,
            title = "Open AI Studio",
            body = "While logged into YOUR Google account, open the API keys page.",
        ) {
            OutlinedButton(
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Open AI Studio")
            }
        }
        IllustratedStep(
            number = 2,
            title = "Create & copy a key",
            body = "Tip: Create API key → copy the string (looks like AIza…). Stay on your own Google account — not a shared one.",
        ) {}
        IllustratedStep(
            number = 3,
            title = "Paste here · Save & Test",
            body = "We only store the key on-device in DataStore. Never ship developer keys.",
        ) {
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Paste Gemini API key") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !busy,
            )
            Button(
                onClick = {
                    scope.launch {
                        val trimmed = key.trim()
                        if (trimmed.isBlank()) {
                            statusIsError = true
                            status = "Paste a Gemini API key first"
                            return@launch
                        }
                        busy = true
                        status = "Saving & testing…"
                        statusIsError = false
                        preferencesRepository.setGeminiApiKey(trimmed)
                        when (val r = aiClient.testGemini { preferencesRepository.getGeminiApiKey() }) {
                            is Result.Success -> {
                                statusIsError = false
                                status = "Connected — Gemini OK"
                                busy = false
                                onNext()
                            }
                            is Result.Error -> {
                                statusIsError = true
                                status = "Saved, but test failed: ${r.message}. Fix later in Settings, or continue with OfflineHealthBrain."
                                busy = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save & Test")
            }
        }
        status?.let {
            Text(
                it,
                color = if (statusIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "Skip → OfflineHealthBrain only (on-device reports from your logs). Cloud AI needs your key.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onSkip, enabled = !busy) {
            Text("Skip — use offline brain only")
        }
        if (statusIsError) {
            OutlinedButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Continue anyway")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileStep(
    preferencesRepository: UserPreferencesRepository,
    prefilledName: String,
    initial: BodyProfile,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSetupScreen(
        preferencesRepository = preferencesRepository,
        prefilledName = prefilledName,
        initial = initial,
        onComplete = onNext,
        modifier = modifier,
    )
}

@Composable
private fun DoneStep(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("You’re all set", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "You can edit profile, Health Connect, and Gemini anytime in Settings. " +
                "AI tips remain educational — not medical advice.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Enter MigraineGuard") }
    }
}
