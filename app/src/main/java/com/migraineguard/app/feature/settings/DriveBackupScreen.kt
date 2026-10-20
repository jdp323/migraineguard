package com.migraineguard.app.feature.settings

import android.accounts.Account
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.core.ui.CheckState
import com.migraineguard.app.core.ui.VerificationRow
import com.migraineguard.app.core.ui.FeatureHeader
import com.migraineguard.app.feature.fitness.findActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.auth.api.identity.AuthorizationClient

private fun driveRequest(email: String) = AuthorizationRequest.builder().setAccount(Account(email, "com.google"))
    .setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/drive.appdata"),
        Scope("https://www.googleapis.com/auth/userinfo.email"))).build()

/** Refresh already granted access without launching consent in the background. */
private suspend fun silentDriveToken(client: AuthorizationClient, email: String): String =
    suspendCancellableCoroutine { continuation ->
        client.authorize(driveRequest(email)).addOnSuccessListener { result ->
            if (continuation.isActive) {
                val token = result.accessToken
                if (!result.hasResolution() && token != null) continuation.resume(token)
                else continuation.resumeWithException(IllegalStateException("Reconnect Drive to renew backup permission."))
            }
        }.addOnFailureListener {
            if (continuation.isActive) continuation.resumeWithException(
                IllegalStateException("Drive authorization could not refresh. Reconnect Drive when online."))
        }
    }

/** Native Google authorization: app-data only, explicitly bound to the signed-in email. */
@Composable
fun DriveBackupScreen(
    repository: DriveSyncRepository,
    email: String,
    onContinue: () -> Unit,
    autoCheck: Boolean = true,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val client = remember(context) { Identity.getAuthorizationClient(context) }
    val state by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var active by remember { mutableStateOf(true) }
    DisposableEffect(Unit) { onDispose { active = false } }
    var pending by remember { mutableStateOf<android.app.PendingIntent?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var userInitiated by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(false) }

    fun accept(result: AuthorizationResult) {
        scope.launch {
            busy = true; error = null
            try {
                val token = result.accessToken ?: kotlin.error("Google did not return Drive authorization.")
                repository.connect(token, email, refresh = { silentDriveToken(client, email) })
                if (userInitiated) verified = true else onContinue()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { error = e.message ?: "Drive is unavailable. Local records are preserved."
            } finally { busy = false }
        }
    }
    val resolution = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        busy = false
        if (result.resultCode == Activity.RESULT_OK) {
            try { accept(client.getAuthorizationResultFromIntent(result.data)) }
            catch (e: Exception) { error = e.message ?: "Drive permission could not be checked." }
        } else error = "Drive access was not granted. You can continue with your saved local profile."
    }
    fun authorize() {
        if (activity == null) { error = "Open the app to connect Drive."; return }
        busy = true; error = null
        val request = driveRequest(email)
        client.authorize(request).addOnSuccessListener { result ->
            if (!active) return@addOnSuccessListener
            busy = false
            if (result.hasResolution()) pending = result.pendingIntent else accept(result)
        }.addOnFailureListener {
            if (!active) return@addOnFailureListener
            busy = false; error = "Drive could not connect. Check internet and Google Play services, then retry."
        }
    }
    LaunchedEffect(email) { if (autoCheck) authorize() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(36.dp))
        FeatureHeader("Your space, restored.", "Bring your saved profile and journal with you.", Icons.Default.CloudDone, eyebrow = "Private backup")
        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard("One account. All your progress.") {
            Text("Restore your profile, birthday, body measurements, goals, API keys and journal from your private Google Drive app data.")
            Spacer(Modifier.height(12.dp))
            Text("Google protects this storage in transit and at rest. Keys are re-encrypted on this phone with Android Keystore. This is not a separate password-protected, end-to-end encrypted backup.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (busy || state.busy) {
            CircularProgressIndicator()
            Text(state.message)
        }
        VerificationRow("Google Drive connection", if (verified) "Account identity and private app data were checked successfully." else state.message,
            when { busy || state.busy -> CheckState.CHECKING; verified -> CheckState.VERIFIED; error != null -> CheckState.ATTENTION; else -> CheckState.WAITING })
        if (verified) Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Continue with verified Drive") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            userInitiated = true; verified = false
            val intent = pending
            if (intent != null) { pending = null; busy = true; resolution.launch(IntentSenderRequest.Builder(intent.intentSender).build()) }
            else authorize()
        }, enabled = !busy && !state.busy && !verified, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
            Text(if (pending != null) "Allow private Drive backup" else "Check Drive again")
        }
        if (!verified) TextButton(onClick = onContinue, enabled = !busy && !state.busy) { Text("Continue with local data") }
    }
}

@Composable
fun DriveBackupCard(repository: DriveSyncRepository, onConnect: () -> Unit) {
    val state by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    SectionCard("Google Drive backup", icon = Icons.Default.CloudDone) {
        VerificationRow("Drive account", state.message,
            when { state.busy -> CheckState.CHECKING; state.connected -> CheckState.VERIFIED; else -> CheckState.WAITING })
        state.lastSavedMillis?.let {
            Text("Last verified: " + java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm")
                .withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(it)),
                style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(10.dp))
        if (state.connected) {
            Button(onClick = { scope.launch { runCatching { repository.backupNow() } } }, enabled = !state.busy) {
                Text(if (state.busy) "Saving…" else "Back up now")
            }
            TextButton(onClick = onConnect, enabled = !state.busy) { Text("Reconnect Google Drive") }
            TextButton(onClick = { scope.launch { repository.pause() } }, enabled = !state.busy) { Text("Pause automatic backup") }
        } else Button(onClick = onConnect) { Text("Connect / restore Drive") }
    }
}
