package com.migraineguard.app.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DataExportCard(repository: BackupRepository) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if(uri != null) scope.launch {
            busy = true; message = ""
            try { repository.exportReadable(uri); message = "Export saved. This ZIP is not encrypted; keep it private." }
            catch(e: CancellationException) { throw e } catch(_: Exception) { message = "Export could not finish. Remove any partial file and retry with enough free storage." } finally { busy = false }
        }
    }
    SectionCard("Export my data") {
        Text("Your records belong to you. Save a readable ZIP with JSON, CSV tables and your saved body-progress photos.")
        Text("Includes this account's profile, goals, preferences and saved journals. Excludes API keys, sign-in tokens, the Health Connect database, meal-photo files and equipment-photo files. This ZIP is not encrypted and is for reading/analysis; use encrypted backup below for app recovery.",style=MaterialTheme.typography.bodySmall)
        Button(onClick = { export.launch("MigraineGuard-data-"+LocalDate.now()+".zip") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if(busy) "Exporting…" else "Export readable data & body photos") }
        if(message.isNotBlank()) Text(message)
    }
}
