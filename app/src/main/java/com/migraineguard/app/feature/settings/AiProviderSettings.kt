package com.migraineguard.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.ai.*
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AiProviderSettings(repository: UserPreferencesRepository) {
    val prefs by repository.preferences.collectAsStateWithLifecycle(UserPreferences())
    var providerId by rememberSaveable { mutableStateOf("gemini") }
    var expanded by remember { mutableStateOf(false) }
    var suggestedPaste by remember { mutableStateOf<String?>(null) }
    var key by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var tested by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val service = AiCatalog.service(providerId) ?: AiCatalog.services.first()
    val saved = prefs.connection(providerId)
    LaunchedEffect(providerId, saved) { key = suggestedPaste ?: saved.apiKey; suggestedPaste = null; model = saved.model; tested = false; status = "" }
    SectionCard("AI providers", icon = Icons.Default.SmartToy) {
        Text("Active: "+when(prefs.preferredAiProvider) { "auto" -> "Legacy automatic fallback"; "offline" -> "On-device only"; else -> AiCatalog.service(prefs.preferredAiProvider)?.label ?: "Not selected" })
        Text("Choose a provider, paste your own key, then save. ChatGPT/Claude subscriptions do not include API credits. Provider requests may cost money.", style = MaterialTheme.typography.bodySmall)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().testTag("ai-provider-picker")) { Text(service.label+" ▾") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AiCatalog.services.forEach { provider -> DropdownMenuItem(text = { Text(provider.label) }, onClick = { providerId = provider.id; expanded = false }) }
            }
        }
        OutlinedTextField(value = key, onValueChange = { key = it.trim().take(4096); tested = false; status = "" }, label = { Text("API key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("ai-provider-key"), singleLine = true)
        val hint = AiCatalog.suggest(key)
        if (key.isNotBlank()) Text(if (hint != null) "Key format suggests "+AiCatalog.service(hint)?.label+". Confirm the provider; this is not verification." else "Provider cannot be reliably detected from this key. Confirm your selection.", style = MaterialTheme.typography.bodySmall)
        if (hint != null && hint != providerId) TextButton(onClick = { suggestedPaste = key; providerId = hint }) { Text("Use suggested provider") }
        OutlinedTextField(value = model, onValueChange = { model = it.trim().take(160); tested = false; status = "" }, label = { Text("Model ID") }, modifier = Modifier.fillMaxWidth().testTag("ai-provider-model"), singleLine = true)
        Text("Requests go only to "+service.endpoint.substringBefore("/v1")+" for this selected provider. Photos: "+if(service.images) "supported with explicit consent" else "not enabled in this app", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { scope.launch {
            busy = true; status = "Saving…"; tested = false
            try { repository.saveAiConnection(providerId, key, model); status = "Saved. This provider is now selected." }
            catch (e: CancellationException) { throw e } catch (_: Exception) { status = "Could not save the provider." } finally { busy = false }
        } }, enabled = !busy && key.isNotBlank() && model.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save provider") }
        OutlinedButton(onClick = { scope.launch {
            busy = true; tested = false; status = "Testing selected provider only…"
            try { when (val r = completeService(providerId, AiConnection(key, model), "Reply with exactly: ok")) {
                is Result.Success -> { tested = true; status = "Connection verified ✓. This test does not save unsaved changes." }
                is Result.Error -> status = r.message
            } } catch (e: CancellationException) { throw e } finally { busy = false }
        } }, enabled = !busy && key.isNotBlank() && model.isNotBlank()) { Text("Test selected provider") }
        if (status.isNotBlank()) Text(status, modifier = Modifier.testTag("ai-provider-status"), color = if(tested) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Only a real provider response verifies the connection. Offline fallback never passes this test. Keys stay separate for each account and are included only in optional account Drive backup.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { scope.launch { repository.setPreferredAiProvider("offline"); status = "On-device mode selected. Saved keys retained." } }) { Text("Use on-device AI only") }
        TextButton(onClick = { scope.launch { repository.setPreferredAiProvider("auto"); status = "Legacy fallback selected: Gemini → Groq → OpenRouter → offline." } }) { Text("Use legacy automatic fallback") }
        TextButton(onClick = { scope.launch { repository.saveAiConnection(providerId, "", model); key = ""; tested = false; status = "Selected provider key removed." } }) { Text("Remove selected key") }
        TextButton(onClick = { scope.launch { repository.clearAllAiKeys(); key = ""; tested = false; status = "All AI keys removed." } }) { Text("Remove all my AI keys") }
    }
}
