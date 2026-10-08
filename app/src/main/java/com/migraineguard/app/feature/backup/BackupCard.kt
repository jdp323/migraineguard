package com.migraineguard.app.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupCard(repository: BackupRepository) {
    var password by remember { mutableStateOf("") }
    var includeProfile by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) scope.launch {
            busy = true; message = null
            try {
                repository.export(uri, password.toCharArray())
                password = ""
                message = "Encrypted backup saved. Keep its password; it cannot be recovered."
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { message = "Could not save the backup. Check the selected location and try again." }
            finally { busy = false }
        }
    }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true; message = null
            try {
                val count = repository.restore(uri, password.toCharArray(), includeProfile)
                password = ""
                message = "Restored $count new or updated records. Existing records were kept."
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { message = "Restore failed. Check the password and that this is a valid MigraineGuard backup. Existing records were kept." }
            finally { busy = false }
        }
    }
    SectionCard(title = "Encrypted backup") {
        Text("Save or restore migraine, food, water, workout, weight, medication and AI history. API keys, photo files and Health Connect data are excluded.")
        Text("You choose the file location. A cloud folder stores only the password-encrypted file.")
        OutlinedTextField(value = password, onValueChange = { password = it }, enabled = !busy,
            label = { Text("Backup password (at least 8 characters)") },
            visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Row {
            Checkbox(checked = includeProfile, onCheckedChange = { includeProfile = it }, enabled = !busy)
            Text("Also restore profile and goals")
        }
        Button(onClick = { exportFile.launch("MigraineGuard-${LocalDate.now()}.mgbak") },
            enabled = !busy && password.length >= 8, modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "Working…" else "Export encrypted backup")
        }
        OutlinedButton(onClick = { importFile.launch(arrayOf("*/*")) },
            enabled = !busy && password.length >= 8, modifier = Modifier.fillMaxWidth()) {
            Text("Import backup and keep existing records")
        }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}
