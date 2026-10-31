package com.migraineguard.app.feature.backup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private fun storageSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.2f MiB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.1f KiB".format(bytes / 1024.0)
    else -> bytes.toString() + " B"
}

@Composable
fun JournalStorageCard(repository: JournalStorageRepository) {
    var usage by remember { mutableStateOf<JournalStorageUsage?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(repository) { try { usage = repository.usage() } catch (e: CancellationException) { throw e } catch (_: Exception) { message = "Could not check local storage." } }
    SectionCard("Compact storage") {
        usage?.let { value ->
            Text("Journal storage on this account: " + storageSize(value.databaseBytes), modifier = Modifier.testTag("journal-storage-size"))
            Text("Body photo data: " + storageSize(value.photoBytes) + " · " + value.measurementLogs + " measurement logs")
        } ?: Text("Checking journal storage…")
        Text("Logs stay in the local database. Body photos use binary JPEG storage; new Drive and encrypted recovery backups compress their JSON data.")
        Text("Optimize reclaims unused journal space after upgrades or deletions and keeps all current records. App files, caches, gallery originals and exported copies are separate.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { scope.launch {
            busy = true; message = ""
            try {
                val before = repository.usage()
                val after = repository.optimize(); usage = after
                message = "Storage optimized. Records preserved. " + storageSize((before.databaseBytes - after.databaseBytes).coerceAtLeast(0)) + " reclaimed."
            } catch (e: CancellationException) { throw e } catch (_: Exception) { message = "Could not optimize right now. Your records are preserved; try again after other saves finish." }
            finally { busy = false }
        } }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("optimize-journal-storage")) { Text(if (busy) "Optimizing…" else "Optimize local storage") }
        if (message.isNotBlank()) Text(message, modifier = Modifier.testTag("journal-storage-message"))
    }
}
