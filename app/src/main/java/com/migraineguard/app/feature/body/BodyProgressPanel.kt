package com.migraineguard.app.feature.body

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.migraineguard.app.core.data.db.BodyProgressEntity
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Base64

@Composable
fun BodyProgressPanel(repository: BodyProgressRepository, preferences: UserPreferencesRepository, metric: Boolean) {
    val entries by repository.observe().collectAsStateWithLifecycle(emptyList())
    val prefs by preferences.preferences.collectAsStateWithLifecycle(UserPreferences())
    val scope = rememberCoroutineScope()
    var adding by rememberSaveable { mutableStateOf(false) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var pose by rememberSaveable { mutableStateOf("Front") }
    var notes by rememberSaveable { mutableStateOf("") }
    var kg by rememberSaveable { mutableStateOf("") }
    var waist by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var selectedPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var removal by remember { mutableStateOf<BodyProgressEntity?>(null) }
    var baselineId by rememberSaveable { mutableStateOf<String?>(null) }
    var latestId by rememberSaveable { mutableStateOf<String?>(null) }
    var shown by rememberSaveable { mutableStateOf(6) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) selectedPhoto = uri.toString() }
    SectionCard("Body progress", icon = Icons.Default.PhotoLibrary) {
        Text("A private timeline of your photos, notes and measurements.")
        Text("Stored in this account on this phone. Photos are never sent to AI. Images cannot reliably measure body fat or health; compare your own observations under similar lighting and posture.", style = MaterialTheme.typography.bodySmall)
        Text(if(entries.isEmpty()) "Start with a photo or measurement." else "${entries.size} check-ins · ${entries.last().dateIso} → ${entries.first().dateIso}", style = MaterialTheme.typography.titleSmall)
        Button(onClick = { adding = !adding }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("body-add")) { Text(if(adding) "Close new check-in" else "Add progress photo / check-in") }
        if (adding) {
            OutlinedTextField(date, { date = it.take(10) }, label = { Text("Date YYYY-MM-DD") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("body-date"))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Front", "Side", "Back", "Other").forEach { value -> FilterChip(pose == value, { pose = value }, label = { Text(value) }) } }
            OutlinedButton(onClick = { picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(if(selectedPhoto == null) "Choose a body photo" else "Change selected photo") }
            selectedPhoto?.let { uri ->
                AsyncImage(Uri.parse(uri), "Selected progress photo", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Fit)
                TextButton(onClick = { selectedPhoto = null }) { Text("Remove selected photo") }
            }
            OutlinedTextField(kg, { kg = it.take(8) }, label = { Text(if(metric) "Weight kg (optional)" else "Weight lb (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(waist, { waist = it.take(8) }, label = { Text(if(metric) "Waist cm (optional)" else "Waist inches (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fat, { fat = it.take(6) }, label = { Text("Body fat % from your scale (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it.take(2000) }, label = { Text("Your observations") }, modifier = Modifier.fillMaxWidth().testTag("body-notes"))
            Button(onClick = { scope.launch {
                busy = true; message = ""
                try {
                    require(!LocalDate.parse(date).isAfter(LocalDate.now())) { "Choose today or an earlier date." }
                    fun number(value: String): Float? = if(value.isBlank()) null else value.toFloatOrNull()?.takeIf { it.isFinite() } ?: error("Use numbers for measurements.")
                    val weight = number(kg)?.let { if(metric) it else it / 2.20462f }
                    val circumference = number(waist)?.let { if(metric) it else it * 2.54f }
                    repository.add(selectedPhoto?.let(Uri::parse), date, pose, notes, weight, circumference, number(fat))
                    selectedPhoto = null; notes = ""; kg = ""; waist = ""; fat = ""; adding = false
                    message = "Check-in saved privately on this phone."
                } catch(e: CancellationException) { throw e } catch(e: Exception) { message = e.message ?: "Could not save. Check the photo, date and measurements." } finally { busy = false }
            } }, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("body-save")) { Text(if(busy) "Saving…" else "Save private check-in") }
        }
        if(message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("body-message"))
    }
    if(entries.size >= 2) SectionCard("Compare over time") {
        val baseline = entries.find { it.id == baselineId } ?: entries.last()
        val latest = entries.find { it.id == latestId } ?: entries.first()
        ProgressChoice("Earlier", entries, baseline) { baselineId = it.id }
        ProgressChoice("Later", entries, latest) { latestId = it.id }
        val days = ChronoUnit.DAYS.between(LocalDate.parse(baseline.dateIso), LocalDate.parse(latest.dateIso))
        Text("$days days between selected check-ins. Compare the same pose for a more consistent view.")
        if(baseline.pose != latest.pose) Text("Different poses selected: ${baseline.pose} / ${latest.pose}.", color = MaterialTheme.colorScheme.secondary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProgressPhoto(baseline, Modifier.weight(1f)); ProgressPhoto(latest, Modifier.weight(1f)) }
        val weightDelta = if(baseline.weightKg != null && latest.weightKg != null) latest.weightKg - baseline.weightKg else null
        val waistDelta = if(baseline.waistCm != null && latest.waistCm != null) latest.waistCm - baseline.waistCm else null
        val fatDelta = if(baseline.bodyFatPercent != null && latest.bodyFatPercent != null) latest.bodyFatPercent - baseline.bodyFatPercent else null
        weightDelta?.let { Text("Recorded weight change: %+.1f %s".format(if(metric) it else it * 2.20462f, if(metric) "kg" else "lb")) }
        waistDelta?.let { Text("Recorded waist change: %+.1f %s".format(if(metric) it else it / 2.54f, if(metric) "cm" else "in")) }
        fatDelta?.let { Text("Scale-reported body fat change: %+.1f percentage points".format(it)) }
        if(weightDelta == null && waistDelta == null && fatDelta == null) Text("Add measurements to both check-ins to see numerical changes. Photo appearance varies with lighting, clothing and posture.")
    }
    if(entries.isNotEmpty()) SectionCard("Progress timeline") {
        entries.take(shown).forEach { entry ->
            Text("${entry.dateIso} · ${entry.pose}", style = MaterialTheme.typography.titleSmall)
            ProgressPhoto(entry, Modifier.fillMaxWidth())
            entry.weightKg?.let { Text(if(metric) "%.1f kg".format(it) else "%.1f lb".format(it * 2.20462f)) }
            entry.waistCm?.let { Text(if(metric) "Waist %.1f cm".format(it) else "Waist %.1f in".format(it / 2.54f)) }
            entry.bodyFatPercent?.let { Text("Scale-reported body fat: %.1f%%".format(it)) }
            if(entry.notes.isNotBlank()) Text(entry.notes)
            TextButton(onClick = { removal = entry }) { Text("Delete this check-in") }
            HorizontalDivider()
        }
        if(entries.size > shown) TextButton(onClick = { shown += 6 }) { Text("Show older check-ins") }
    }
    SectionCard("Photo backup choice") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Include body photos in my Google Drive backup", Modifier.weight(1f))
            Switch(prefs.bodyPhotoDriveBackup, { enabled -> scope.launch { preferences.setBodyPhotoDriveBackup(enabled) } })
        }
        Text("Off by default. Turning this on includes these photos and check-in measurements in your private Drive app data when Drive is connected. No developer server receives them. Snapshots have a 20 MB limit; export before removing local data. Turning this off does not revoke Drive or instantly erase an older snapshot.", style = MaterialTheme.typography.bodySmall)
    }
    removal?.let { entry -> AlertDialog(onDismissRequest = { removal = null }, title = { Text("Delete this check-in?") }, text = { Text("Removes its photo, measurements and notes from this account on this phone. Connected photo-enabled Drive applies the deletion on its next successful save. Existing exported files are separate copies.") }, confirmButton = { TextButton(onClick = { scope.launch { repository.remove(entry); removal = null } }) { Text("Delete check-in") } }, dismissButton = { TextButton(onClick = { removal = null }) { Text("Keep") } }) }
}

@Composable
private fun ProgressChoice(label: String, entries: List<BodyProgressEntity>, selected: BodyProgressEntity, choose: (BodyProgressEntity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: ${selected.dateIso} · ${selected.pose}") }
        DropdownMenu(expanded, { expanded = false }) { entries.forEach { entry -> DropdownMenuItem(text = { Text("${entry.dateIso} · ${entry.pose}") }, onClick = { choose(entry); expanded = false }) } }
    }
}

@Composable
private fun ProgressPhoto(entry: BodyProgressEntity, modifier: Modifier) {
    val bytes = remember(entry.id, entry.modifiedAtMillis) { if(entry.jpegBase64.isEmpty()) null else runCatching { Base64.getDecoder().decode(entry.jpegBase64) }.getOrNull() }
    Column(modifier) {
        if(bytes != null) AsyncImage(bytes, "Progress photo ${entry.dateIso}, ${entry.pose}", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Fit)
        Text(entry.dateIso, style = MaterialTheme.typography.labelSmall)
    }
}
