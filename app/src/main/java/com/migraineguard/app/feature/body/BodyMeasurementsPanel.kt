package com.migraineguard.app.feature.body

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.db.BodyMeasurementEntity
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

private val measurementDraftSaver = listSaver<Map<String, String>, String>(
    save = { values -> values.flatMap { listOf(it.key, it.value) } },
    restore = { values -> values.chunked(2).associate { it[0] to it[1] } },
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyMeasurementsPanel(repository: BodyMeasurementRepository, metric: Boolean) {
    val entries by repository.observe().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var editing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var group by rememberSaveable { mutableStateOf("Core") }
    var notes by rememberSaveable { mutableStateOf("") }
    var draft by rememberSaveable(stateSaver = measurementDraftSaver) { mutableStateOf(emptyMap<String, String>()) }
    var draftMetric by rememberSaveable { mutableStateOf(metric) }
    var discard by remember { mutableStateOf(false) }
    var customName by rememberSaveable { mutableStateOf("") }
    var customValue by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf(false) }
    var trends by rememberSaveable { mutableStateOf(false) }
    var shown by rememberSaveable { mutableStateOf(6) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var removal by remember { mutableStateOf<BodyMeasurementEntity?>(null) }
    val unit = if (metric) "cm" else "in"
    val formUnit = if (draftMetric) "cm" else "in"
    val hasDraft = editingId != null || draft.values.any { it.isNotBlank() } || notes.isNotBlank() || customName.isNotBlank() || customValue.isNotBlank()
    fun newLog() {
        draftMetric = metric; editingId = null; date = LocalDate.now().toString(); notes = ""; draft = emptyMap()
        customName = ""; customValue = ""; group = "Core"; editing = true; message = ""
    }
    SectionCard("Body measurements") {
        Text("Arms, biceps, chest, abdomen, waist, hips, thighs and more — logged by date.")
        Text(if (entries.isEmpty()) "Start with any area you want to track." else entries.size.toString() + " logs · latest " + entries.first().dateIso)
        Button(onClick = { if (editing) editing = false else if (hasDraft) editing = true else newLog() }, enabled = !busy,
            modifier = Modifier.fillMaxWidth().testTag("measurements-add")) { Text(if (editing) "Hide measurement form" else if (hasDraft) "Continue measurement log" else "Log measurements") }
        if (entries.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { trends = !trends }, modifier = Modifier.testTag("measurements-trends")) { Text(if (trends) "Hide trends" else "Trends") }
            OutlinedButton(onClick = { history = !history }, modifier = Modifier.testTag("measurements-history")) { Text(if (history) "Hide history" else "History") }
        }
        if (editing) {
            Text(if (editingId == null) "New dated log" else "Edit dated log", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(date, { date = it.take(10) }, label = { Text("Measurement date YYYY-MM-DD") },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("measurement-date"))
            Text("Use a soft tape in " + formUnit + ". Leave unused areas blank. Keep the same landmark, posture and tape tension.", style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BodyMeasurements.groups.forEach { name -> FilterChip(group == name, { group = name }, enabled = !busy, label = { Text(name) },
                    modifier = Modifier.testTag("measurement-group-" + name)) }
            }
            MeasurementArea.entries.filter { it.group == group }.forEach { area ->
                OutlinedTextField(draft[area.id].orEmpty(), { draft = draft + (area.id to it.take(16)) },
                    label = { Text(area.label + " (" + formUnit + ")") }, supportingText = { Text(area.tip) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, enabled = !busy,
                    modifier = Modifier.fillMaxWidth().testTag("measurement-value-" + area.id))
            }
            if (group == "Custom") {
                BodyMeasurements.orderedKeys(draft.keys).filter { it.startsWith("custom:") }.forEach { key ->
                    OutlinedTextField(draft[key].orEmpty(), { draft = draft + (key to it.take(16)) },
                        label = { Text(BodyMeasurements.label(key) + " (" + formUnit + ")") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("measurement-value-" + key))
                    TextButton(onClick = { draft = draft - key }, enabled = !busy) { Text("Remove " + BodyMeasurements.label(key)) }
                }
                OutlinedTextField(customName, { customName = it.take(40) }, label = { Text("Custom area name") }, singleLine = true,
                    enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("measurement-custom-name"))
                OutlinedTextField(customValue, { customValue = it.take(16) }, label = { Text("Custom measurement (" + formUnit + ")") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), enabled = !busy, modifier = Modifier.fillMaxWidth().testTag("measurement-custom-value"))
                OutlinedButton(onClick = {
                    try {
                        val key = BodyMeasurements.customId(customName)
                        require(key !in draft) { "This custom area already exists. Edit its field above." }
                        require(draft.keys.count { it.startsWith("custom:") } < 8) { "Use up to eight custom areas per log." }
                        BodyMeasurements.parse(customValue, draftMetric)
                        draft = draft + (key to customValue); customName = ""; customValue = ""; message = "Custom area added."
                    } catch (e: Exception) { message = e.message ?: "Check the custom area." }
                }, enabled = !busy, modifier = Modifier.testTag("measurement-add-custom")) { Text("Add custom area") }
            }
            Text(draft.values.count { it.isNotBlank() }.toString() + " measurements entered across all groups.", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(notes, { notes = it.take(2000) }, label = { Text("Measurement notes (optional)") },
                modifier = Modifier.fillMaxWidth().testTag("measurement-notes"), enabled = !busy)
            Button(onClick = {
                focus.clearFocus(); keyboard?.hide()
                scope.launch {
                    busy = true; message = ""
                    try {
                        val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: error("Use a date in YYYY-MM-DD format.")
                        require(!day.isAfter(LocalDate.now())) { "Choose today or an earlier date." }
                        var values = draft.filterValues { it.isNotBlank() }
                        if (customName.isNotBlank() || customValue.isNotBlank()) {
                            val key = BodyMeasurements.customId(customName)
                            require(key !in values) { "This custom area already exists. Edit its field above." }
                            values = values + (key to customValue)
                        }
                        require(values.isNotEmpty()) { "Enter at least one measurement." }
                        val canonical = values.mapValues { (key, text) ->
                            try { BodyMeasurements.parse(text, draftMetric) } catch (_: Exception) { error("Check " + BodyMeasurements.label(key) + ": use a value greater than 0 and no more than 500 cm (196.85 in).") }
                        }
                        repository.save(editingId, day.toString(), canonical, notes)
                        editing = false; editingId = null; draft = emptyMap(); customName = ""; customValue = ""; notes = ""
                        message = "Measurements saved for " + day + "."; history = true
                    } catch (e: CancellationException) { throw e } catch (e: Exception) { message = e.message ?: "Could not save measurements." }
                    finally { busy = false }
                }
            }, enabled = !busy && (draft.values.any { it.isNotBlank() } || customValue.isNotBlank()),
                modifier = Modifier.fillMaxWidth().testTag("measurement-save")) { Text(if (busy) "Saving…" else if (editingId == null) "Save measurements" else "Save changes") }
            TextButton(onClick = { discard = true }, enabled = !busy, modifier = Modifier.testTag("measurement-discard")) { Text("Discard unsaved changes") }
        }
        if (message.isNotBlank()) Text(message, modifier = Modifier.testTag("measurement-message"), style = MaterialTheme.typography.bodySmall)
        Text("Stored in this account. These logs follow your normal account backup setting; body-photo backup has its own choice.", style = MaterialTheme.typography.bodySmall)
    }
    if (trends && entries.isNotEmpty()) MeasurementTrend(entries, metric)
    if (history && entries.isNotEmpty()) SectionCard("Measurement history") {
        entries.take(shown).forEach { entry ->
            Text(entry.dateIso, style = MaterialTheme.typography.titleMedium)
            BodyMeasurements.orderedKeys(entry.measurementsCm.keys).forEach { key ->
                Text(BodyMeasurements.label(key) + ": " + "%.1f".format(BodyMeasurements.displayed(entry.measurementsCm.getValue(key), metric)) + " " + unit)
            }
            if (entry.notes.isNotBlank()) Text(entry.notes)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    draftMetric = metric; editingId = entry.id; date = entry.dateIso; notes = entry.notes
                    draft = entry.measurementsCm.mapValues { BodyMeasurements.draft(it.value, metric) }
                    customName = ""; customValue = ""; editing = true
                    group = MeasurementArea.entries.find { it.id in entry.measurementsCm }?.group ?: "Custom"
                    message = "Editing " + entry.dateIso + "."; focus.clearFocus(); keyboard?.hide()
                }, enabled = !busy, modifier = Modifier.testTag("measurement-edit-" + entry.id)) { Text("Edit measurements") }
                TextButton(onClick = { removal = entry }, enabled = !busy, modifier = Modifier.testTag("measurement-delete-" + entry.id)) { Text("Delete log") }
            }
            HorizontalDivider()
        }
        if (entries.size > shown) TextButton(onClick = { shown += 6 }) { Text("Show older measurement logs") }
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Discard these unsaved changes?") },
        confirmButton = { TextButton(onClick = { newLog(); editing = false; discard = false; message = "Unsaved changes discarded." }, modifier = Modifier.testTag("measurement-confirm-discard")) { Text("Discard draft") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Keep draft") } })
    removal?.let { entry -> AlertDialog(onDismissRequest = { removal = null }, title = { Text("Delete measurement log?") },
        text = { Text("Removes this dated log from this account. Connected Drive applies the deletion on its next successful save; exported copies are separate.") },
        confirmButton = { TextButton(onClick = { scope.launch { repository.remove(entry.id); if (editingId == entry.id) { newLog(); editing = false }; removal = null; message = "Measurement log deleted." } }, modifier = Modifier.testTag("measurement-confirm-delete")) { Text("Delete measurements") } },
        dismissButton = { TextButton(onClick = { removal = null }) { Text("Keep log") } }) }
}

@Composable
private fun MeasurementTrend(entries: List<BodyMeasurementEntity>, metric: Boolean) {
    val keys = BodyMeasurements.orderedKeys(entries.flatMap { it.measurementsCm.keys }.toSet())
    var selected by rememberSaveable { mutableStateOf("waist") }
    val key = selected.takeIf { it in keys } ?: keys.first()
    var expanded by remember { mutableStateOf(false) }
    val points = BodyMeasurements.points(entries, key)
    val unit = if (metric) "cm" else "in"
    val color = MaterialTheme.colorScheme.primary
    SectionCard("Measurement trend") {
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().testTag("measurement-trend-picker")) { Text(BodyMeasurements.label(key) + " trend ▾") }
            DropdownMenu(expanded, { expanded = false }) { keys.forEach { value -> DropdownMenuItem(text = { Text(BodyMeasurements.label(value)) }, onClick = { selected = value; expanded = false }, modifier = Modifier.testTag("measurement-trend-" + value)) } }
        }
        val first = points.first(); val last = points.last()
        Text("Latest: %.1f %s".format(BodyMeasurements.displayed(last.centimetres, metric), unit), modifier = Modifier.testTag("measurement-latest"))
        if (points.size >= 2) {
            Text("Since first log: %+.1f %s".format(BodyMeasurements.displayed(last.centimetres - first.centimetres, metric), unit), modifier = Modifier.testTag("measurement-change"))
            Canvas(Modifier.fillMaxWidth().height(140.dp).semantics { contentDescription = BodyMeasurements.label(key) + " measurement trend by date" }) {
                val min = points.minOf { it.centimetres }; val max = points.maxOf { it.centimetres }.coerceAtLeast(min + 1f)
                val start = points.minOf { it.epochDay }; val days = (points.maxOf { it.epochDay } - start).coerceAtLeast(1)
                val line = Path()
                points.forEachIndexed { index, point ->
                    val x = 12.dp.toPx() + (size.width - 24.dp.toPx()) * (point.epochDay - start).toFloat() / days.toFloat()
                    val y = 8.dp.toPx() + (size.height - 16.dp.toPx()) * (1f - (point.centimetres - min) / (max - min))
                    if (index == 0) line.moveTo(x,y) else line.lineTo(x,y)
                    drawCircle(color, 4.dp.toPx(), Offset(x,y))
                }
                drawPath(line, color, style = Stroke(2.dp.toPx()))
            }
            Text(first.dateIso + " → " + last.dateIso, style = MaterialTheme.typography.bodySmall)
        } else Text("Add another dated log of this area to see change over time.")
    }
}
