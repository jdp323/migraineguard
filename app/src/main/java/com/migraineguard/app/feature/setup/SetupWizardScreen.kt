package com.migraineguard.app.feature.setup

import com.migraineguard.app.core.ui.AppTopBar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.heightIn
import com.migraineguard.app.core.ui.CheckState
import com.migraineguard.app.core.ui.VerificationRow
import com.migraineguard.app.core.ui.FeatureHeader
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
    val loadedPrefs by preferencesRepository.preferences.collectAsStateWithLifecycle<UserPreferences?>(null)
    val prefs = loadedPrefs ?: return
    var step by rememberSaveable { mutableStateOf(if (authState.isSignedIn) WizardStep.HEALTH else WizardStep.WELCOME) }
    var healthResult by rememberSaveable { mutableStateOf(CheckState.WAITING) }
    var aiResult by rememberSaveable { mutableStateOf(CheckState.WAITING) }
    var finishing by remember { mutableStateOf(false) }
    var finishError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Advance past sign-in once authenticated
    LaunchedEffect(authState.isSignedIn, step) {
        if (step == WizardStep.SIGN_IN && authState.isSignedIn) {
            step = WizardStep.HEALTH
        }
    }

    val checks = listOf(
        SetupCheck("Welcome", "Introduction reviewed", if (step != WizardStep.WELCOME) CheckState.VERIFIED else CheckState.WAITING),
        SetupCheck("Account", if (authState.isDemo) "Local profile selected; Google backup is off." else "Google account signed in.", if (authState.isSignedIn) CheckState.VERIFIED else CheckState.WAITING),
        SetupCheck("Health", if (healthResult == CheckState.VERIFIED) "Requested permissions checked; see Fitness for individual readings." else "Optional connection; readings may still be unavailable.", healthResult),
        SetupCheck("AI", if (aiResult == CheckState.VERIFIED) "Gemini answered the connection test." else "Cloud connection not verified; on-device assistant remains available.", aiResult),
        SetupCheck("Profile", "Your validated details are saved on this account.", if (prefs.bodyProfile.profileComplete) CheckState.VERIFIED else CheckState.WAITING),
    )
    val canGoBack = step in listOf(WizardStep.SIGN_IN, WizardStep.GEMINI, WizardStep.PROFILE, WizardStep.DONE)
    fun goBack() { step = when (step) {
        WizardStep.SIGN_IN -> WizardStep.WELCOME
        WizardStep.GEMINI -> WizardStep.HEALTH
        WizardStep.PROFILE -> WizardStep.GEMINI
        WizardStep.DONE -> WizardStep.PROFILE
        else -> step
    } }
    BackHandler(enabled = canGoBack && !finishing) { goBack() }
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        Column {
            AppTopBar(title = { Text(if (step == WizardStep.DONE) "Setup review" else "Setup · " + step.title) },
                navigationIcon = { if (canGoBack) IconButton(onClick = { goBack() }, enabled = !finishing) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous setup step")
                } })
            SetupProgressHeader(checks, step.index.coerceAtMost(4))
        }
    }) { padding ->
        AnimatedContent(step, modifier = Modifier.padding(padding), label = "setup step",
            transitionSpec = { (fadeIn(tween(240)) + slideInHorizontally(tween(240)) { it / 12 }) togetherWith fadeOut(tween(100)) }) { current ->
            when (current) {
                WizardStep.WELCOME -> WelcomeStep(onNext = { step = WizardStep.SIGN_IN })
                WizardStep.SIGN_IN -> SignInStep(authRepository = authRepository, onNeedGoogleSetup = {
                    scope.launch { snackbar.showSnackbar("Google sign-in is unavailable. You can continue locally.") }
                })
                WizardStep.HEALTH -> HealthConnectStep(healthDataRepository, snackbar,
                    onNext = { step = WizardStep.GEMINI },
                    onSkip = { scope.launch {
                        preferencesRepository.setHealthConnectSkipped(true)
                        healthResult = CheckState.SKIPPED; step = WizardStep.GEMINI
                    } }, onVerificationChanged = { healthResult = it })
                WizardStep.GEMINI -> GeminiStep(preferencesRepository, aiClient,
                    onNext = { verified -> aiResult = if (verified) CheckState.VERIFIED else CheckState.ATTENTION; step = WizardStep.PROFILE },
                    onSkip = { aiResult = CheckState.SKIPPED; step = WizardStep.PROFILE },
                    onVerificationChanged = { aiResult = it })
                WizardStep.PROFILE -> ProfileStep(preferencesRepository,
                    authState.user?.displayName ?: prefs.bodyProfile.displayName, prefs.bodyProfile,
                    onNext = { step = WizardStep.DONE })
                WizardStep.DONE -> DoneStep(checks = checks.drop(1), busy = finishing, error = finishError, onFinish = {
                    scope.launch {
                        finishing = true; finishError = null
                        try { preferencesRepository.setSetupComplete(true); onFinished() }
                        catch (e: kotlinx.coroutines.CancellationException) { throw e }
                        catch (_: Exception) { finishError = "Setup could not be saved. Please try again." }
                        finally { finishing = false }
                    }
                })
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureHeader("Your day, together.", "Migraine tracking, movement and everyday habits, in one place.",
            Icons.Filled.Healing, eyebrow = "MigraineGuard")
        Text(stringResource(R.string.app_version_label, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Start setup") }
        Text("Choose your account, check your health connection and personalize your space. Cloud AI and backup are optional.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Personal and educational tracking. Not a medical device or a substitute for professional care.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onNext: (Boolean) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    onVerificationChanged: (CheckState) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var checkedKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { onVerificationChanged(CheckState.WAITING); key = preferencesRepository.getGeminiApiKey() }
    val verified = checkedKey != null && checkedKey == key.trim()

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
            body = "Your key is encrypted on this phone and included in your private Drive backup when connected.",
        ) {
            OutlinedTextField(
                value = key,
                onValueChange = { key = it; checkedKey = null; status = null; statusIsError = false; onVerificationChanged(CheckState.WAITING) },
                label = { Text("Paste Gemini API key") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                        checkedKey = null
                        onVerificationChanged(CheckState.CHECKING)
                        try {
                        preferencesRepository.setGeminiApiKey(trimmed)
                        when (val r = aiClient.testGemini { preferencesRepository.getGeminiApiKey() }) {
                            is Result.Success -> {
                                statusIsError = false
                                status = "Connected — Gemini OK"
                                busy = false
                                checkedKey = trimmed
                                onVerificationChanged(CheckState.VERIFIED)
                            }
                            is Result.Error -> {
                                statusIsError = true
                                onVerificationChanged(CheckState.ATTENTION)
                                status = "Saved, but test failed: ${r.message}. Fix later in Settings, or continue with OfflineHealthBrain."
                                busy = false
                            }
                        }
                        } catch (e: kotlinx.coroutines.CancellationException) { throw e
                        } catch (_: Exception) {
                            onVerificationChanged(CheckState.ATTENTION)
                            statusIsError = true; status = "Could not save or verify the key. Retry, or continue offline."
                        } finally { busy = false }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save & Test")
            }
        }
        VerificationRow("Gemini connection", if (verified) "Verified response received. Your key is saved." else if (busy) "Testing your key with Gemini…" else "Save & Test checks an actual response before showing a checkmark.",
            when { busy -> CheckState.CHECKING; verified -> CheckState.VERIFIED; statusIsError -> CheckState.ATTENTION; else -> CheckState.WAITING })
        if (verified) Button(onClick = { onNext(true) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Continue with verified Gemini") }
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
            OutlinedButton(onClick = { onNext(false) }, modifier = Modifier.fillMaxWidth()) {
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
        embedded = true,
        modifier = modifier,
    )
}

@Composable
private fun DoneStep(checks: List<SetupCheck>, busy: Boolean, error: String?, onFinish: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FeatureHeader("Your space is ready.", "Here is what was saved or checked. Optional connections can be finished later.",
            Icons.Filled.CheckCircle, eyebrow = "Setup review")
        SectionCard("Your setup checklist") {
            checks.forEach { VerificationRow(it.title, if (it.state == CheckState.SKIPPED) "Skipped for now. You can connect in Settings." else it.detail, it.state) }
        }
        Text("A permission check does not guarantee watch records have synced. Fitness shows the actual readings. AI is educational and does not replace professional care.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = onFinish, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
            Text(if (busy) "Saving setup…" else "Enter MigraineGuard")
        }
    }
}
