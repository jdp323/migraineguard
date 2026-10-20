package com.migraineguard.app.feature.setup

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.migraineguard.app.core.ui.*
import com.migraineguard.app.feature.fitness.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun HealthConnectStep(healthDataRepository: HealthDataRepository, snackbar: SnackbarHostState,
    onNext: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier,
    onVerificationChanged: (CheckState) -> Unit = {}) {
    val context = LocalContext.current
    val launchContext = context.findActivity() ?: context
    val scope = rememberCoroutineScope()
    val reportStatus by rememberUpdatedState(onVerificationChanged)
    var evidence by remember { mutableStateOf(HealthSetupEvidence()) }
    var checking by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var checkJob by remember { mutableStateOf<Job?>(null) }
    var generation by remember { mutableIntStateOf(0) }

    fun checkHealth() {
        checkJob?.cancel()
        val thisCheck = ++generation
        checking = true; checked = false; error = null
        evidence = HealthSetupEvidence()
        reportStatus(CheckState.CHECKING)
        checkJob = scope.launch {
            try {
                val available = healthDataRepository.availabilityStatus() == HealthConnectStatus.AVAILABLE
                val permissions = healthDataRepository.hasAllPermissions()
                val fitness = if (available) healthDataRepository.latestFitness() else null
                val sleep = if (available) healthDataRepository.latestSleep() else null
                evidence = HealthSetupEvidence(available, permissions, fitness, sleep)
                checked = true
                reportStatus(if (permissions && available) CheckState.VERIFIED else CheckState.WAITING)
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                error = "The connection could not be checked. Retry when Health Connect is available."
                reportStatus(CheckState.ATTENTION)
            } finally { if (generation == thisCheck) checking = false }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { checkHealth() }

    fun openFirst(intents: List<Intent>) {
        val opened = intents.any { intent ->
            try { launchContext.startHealthActivity(intent); true } catch (_: Exception) { false }
        }
        if (!opened) scope.launch { snackbar.showSnackbar("Could not open health settings. Open Health Connect from Android Settings.") }
    }
    val fallbackLauncher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { checkHealth() }
    val providerLauncher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract(
        providerPackageName = HealthConnectPackages.GOOGLE)) { checkHealth() }
    fun connect() {
        scope.launch {
            try {
                healthDataRepository.allowDeviceHealth()
                if (healthDataRepository.availabilityStatus() != HealthConnectStatus.AVAILABLE) {
                    openFirst(healthDataRepository.openHealthConnectInstallOrSettingsIntents())
                } else {
                    try { providerLauncher.launch(healthDataRepository.requiredPermissions()) }
                    catch (_: ActivityNotFoundException) { fallbackLauncher.launch(healthDataRepository.requiredPermissions()) }
                }
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                openFirst(healthDataRepository.manageHealthPermissionsIntents(launchContext.packageName))
            }
        }
    }
    fun state(verified: Boolean) = when {
        checking -> CheckState.CHECKING
        verified -> CheckState.VERIFIED
        error != null -> CheckState.ATTENTION
        else -> CheckState.WAITING
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FeatureHeader("Your health, connected.", "Check this phone's connection before you continue.",
            Icons.Filled.Watch, eyebrow = "Health setup")
        Text("Health Connect belongs to this phone, independently of Google login. Connect only if these are your records. Every health permission is optional.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard("Connection checklist") {
            VerificationRow("Health Connect available", if (evidence.available) "Android's health service is ready." else "Install or update Health Connect on this phone.", state(evidence.available))
            VerificationRow("Requested permissions", if (evidence.allPermissions) "All requested read permissions were checked." else "Some reads are not allowed, or this account has not connected yet.", state(evidence.allPermissions))
            VerificationRow("Samsung records received", if (evidence.samsungVerified) "A returned record or aggregate identifies Samsung Health." else "Not verified yet. Opening Samsung Health alone does not confirm sharing.", state(evidence.samsungVerified))
            Button(onClick = { connect() }, enabled = !checking, modifier = Modifier.fillMaxWidth()) { Text("Connect / grant permissions") }
            OutlinedButton(onClick = { openFirst(healthDataRepository.openHealthConnectInstallOrSettingsIntents()) }, modifier = Modifier.fillMaxWidth()) { Text("Install / open Health Connect") }
            healthDataRepository.openSamsungHealthSettingsIntent()?.let { intent ->
                TextButton(onClick = { openFirst(listOf(intent)) }, modifier = Modifier.fillMaxWidth()) { Text("Open Samsung Health") }
            }
        }
        SectionCard("Actual readings") {
            VerificationRow("Steps received", if (evidence.stepsVerified) "Today: " + evidence.fitness?.observedSteps + if (evidence.fitness?.stepSync?.isEstimated == true) " · raw-record estimate" else " steps" else "Waiting for shared steps. Permission alone is not a reading.", state(evidence.stepsVerified))
            VerificationRow("Heart rate received", if (evidence.heartVerified) "Latest recorded sample: " + evidence.fitness?.heartRateBpm + " BPM; not a live stream." else "No dated heart-rate sample received.", state(evidence.heartVerified))
            VerificationRow("Sleep received", if (evidence.sleepVerified) "Completed session: %.1f hours".format(evidence.sleep?.hours) + if (evidence.sleep?.isRecent == false) " · older session" else "" else "No completed sleep session received.", state(evidence.sleepVerified))
            Text("Missing readings can arrive after Samsung Health syncs. You can continue and check Fitness later.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { checkHealth() }, enabled = !checking, modifier = Modifier.fillMaxWidth()) {
                Text(if (checking) "Checking connection…" else "Check connection & readings")
            }
            if (checked && !checking) Text("Connection check completed", style = MaterialTheme.typography.labelSmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        Button(onClick = onNext, enabled = !checking, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(if (evidence.allPermissions) "Continue" else "Continue without all permissions")
        }
        TextButton(onClick = onSkip, enabled = !checking, modifier = Modifier.fillMaxWidth()) { Text("Skip Health Connect for now") }
    }
}
