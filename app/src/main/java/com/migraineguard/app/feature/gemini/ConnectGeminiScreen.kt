package com.migraineguard.app.feature.gemini

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.launch

/**
 * Collects the user's own Gemini API key (Google AI Studio free quota).
 * Never uses developer or Firebase api_key values.
 */
@Composable
fun ConnectGeminiScreen(
    preferencesRepository: UserPreferencesRepository,
    aiClient: FallbackAiClient,
    onConnected: () -> Unit,
    onSkipOffline: () -> Unit,
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
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.SmartToy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Connect your Gemini key",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Text(
                "Uses YOUR free Google AI Studio quota, not the app developer’s. " +
                    "Google Sign-In does not grant Gemini tokens. " +
                    "The Firebase api_key in google-services.json is unrelated and is never used for AI.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Start,
            )
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Open Google AI Studio (get free key)")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text("Paste Gemini API key") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !busy,
        )
        Spacer(Modifier.height(12.dp))
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
                            onConnected()
                        }
                        is Result.Error -> {
                            statusIsError = true
                            status = "Saved, but test failed: ${r.message}. " +
                                "You can still use the on-device assistant, or fix the key in Settings."
                            busy = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !busy,
        ) {
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Save & Test")
            }
        }
        status?.let {
            Text(
                it,
                color = if (statusIsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Without a saved key, Coach and Analyst use on-device reports from your logs. " +
                "You can add a key anytime in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = onSkipOffline,
            enabled = !busy,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text("Skip — use offline brain only")
        }
    }
}
