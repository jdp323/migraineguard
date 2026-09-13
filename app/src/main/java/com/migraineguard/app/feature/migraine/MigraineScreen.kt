package com.migraineguard.app.feature.migraine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.R
import com.migraineguard.app.core.domain.HeadView
import com.migraineguard.app.core.domain.MigraineEpisode
import com.migraineguard.app.core.domain.MigraineSymptom
import com.migraineguard.app.core.domain.MigraineTrigger
import com.migraineguard.app.core.domain.PainMarker
import com.migraineguard.app.core.ui.EmptyState
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt

private enum class MigrainePane { Log, History, Detail }

private val ViewOrder = listOf(HeadView.FRONT, HeadView.RIGHT, HeadView.BACK, HeadView.LEFT, HeadView.TOP)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MigraineScreen(
    repository: MigraineRepository,
    modifier: Modifier = Modifier,
) {
    var pane by remember { mutableStateOf(MigrainePane.Log) }
    var detailId by remember { mutableStateOf<Long?>(null) }
    val episodes by repository.observeEpisodes().collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val markers = remember { mutableStateListOf<PainMarker>() }
    var intensity by remember { mutableFloatStateOf(5f) }
    var notes by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<MigraineSymptom>() }
    val selectedTriggers = remember { mutableStateListOf<MigraineTrigger>() }
    var currentView by remember { mutableStateOf(HeadView.FRONT) }
    var showLabels by remember { mutableStateOf(true) }

    fun rotate(delta: Int) {
        val idx = ViewOrder.indexOf(currentView)
        currentView = ViewOrder[(idx + delta + ViewOrder.size) % ViewOrder.size]
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (pane) {
                            MigrainePane.Log -> "Log migraine"
                            MigrainePane.History -> "Episode history"
                            MigrainePane.Detail -> "Episode detail"
                        },
                    )
                },
                navigationIcon = {
                    if (pane != MigrainePane.Log) {
                        IconButton(onClick = {
                            pane = if (pane == MigrainePane.Detail) MigrainePane.History else MigrainePane.Log
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (pane == MigrainePane.Log) {
                        IconButton(onClick = { pane = MigrainePane.History }) {
                            Icon(Icons.Filled.History, contentDescription = "History")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (pane == MigrainePane.History) {
                FloatingActionButton(onClick = { pane = MigrainePane.Log }) {
                    Icon(Icons.Filled.Add, contentDescription = "Log migraine")
                }
            }
        },
    ) { padding ->
        when (pane) {
            MigrainePane.History -> {
                if (episodes.isEmpty()) {
                    EmptyState(
                        title = "No episodes yet",
                        subtitle = "Tap + to log your first migraine on the 3D head map.",
                        icon = Icons.Filled.Healing,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(episodes, key = { it.id }) { ep ->
                            val sides = ep.markers.map { it.viewId }.distinct()
                                .joinToString { runCatching { HeadView.valueOf(it).label }.getOrDefault(it) }
                            SectionCard(title = formatTs(ep.timestampMillis)) {
                                Text("Intensity ${ep.overallIntensity}/10 · ${ep.markers.size} points")
                                if (sides.isNotBlank()) Text("Views: $sides", style = MaterialTheme.typography.bodySmall)
                                if (ep.symptoms.isNotEmpty()) {
                                    Text(ep.symptoms.joinToString { it.label })
                                }
                                if (ep.triggers.isNotEmpty()) {
                                    Text("Triggers: " + ep.triggers.joinToString { it.label }, style = MaterialTheme.typography.bodySmall)
                                }
                                if (ep.notes.isNotBlank()) {
                                    Text(ep.notes, style = MaterialTheme.typography.bodyMedium)
                                }
                                TextButton(
                                    onClick = {
                                        detailId = ep.id
                                        pane = MigrainePane.Detail
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                ) { Text("Open") }
                            }
                        }
                    }
                }
            }
            MigrainePane.Detail -> {
                val ep = episodes.find { it.id == detailId }
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (ep == null) {
                        Text("Episode not found")
                    } else {
                        val sides = ep.markers.map { it.viewId }.distinct()
                            .joinToString { runCatching { HeadView.valueOf(it).label }.getOrDefault(it) }
                        SectionCard(title = formatTs(ep.timestampMillis)) {
                            Text("Overall intensity: ${ep.overallIntensity}/10")
                            Text("Pain points: ${ep.markers.size}")
                            Text("Sides / views: ${sides.ifBlank { "Front" }}")
                            Text("Symptoms: ${ep.symptoms.joinToString { it.label }.ifBlank { "None" }}")
                            Text("Triggers: ${ep.triggers.joinToString { it.label }.ifBlank { "None" }}")
                            if (ep.notes.isNotBlank()) Text("Notes: ${ep.notes}")
                        }
                        ViewOrder.filter { v -> ep.markers.any { it.viewId == v.name } }.ifEmpty { listOf(HeadView.FRONT) }
                            .forEach { view ->
                                Text(view.label, style = MaterialTheme.typography.titleSmall)
                                HeadPainMap(
                                    view = view,
                                    markers = ep.markers.filter { it.viewId == view.name },
                                    interactive = false,
                                )
                            }
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.delete(ep.id)
                                    pane = MigrainePane.History
                                    snackbar.showSnackbar("Episode deleted")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text("Delete episode")
                        }
                    }
                }
            }
            MigrainePane.Log -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        "Rotate the 3D head (chevrons or swipe) and tap to place glowing pain markers. Right view mirrors the left image.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { rotate(-1) }) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Rotate left")
                        }
                        Text(currentView.label, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { rotate(1) }) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Rotate right")
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ViewOrder.forEach { v ->
                            val count = markers.count { it.viewId == v.name }
                            FilterChip(
                                selected = currentView == v,
                                onClick = { currentView = v },
                                label = { Text(if (count > 0) "${v.label} ($count)" else v.label) },
                            )
                        }
                        FilterChip(
                            selected = showLabels,
                            onClick = { showLabels = !showLabels },
                            label = { Text(if (showLabels) "Labels on" else "Labels off") },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    HeadPainMap(
                        view = currentView,
                        markers = markers.filter { it.viewId == currentView.name },
                        interactive = true,
                        intensityForTap = intensity,
                        onAddMarker = { markers.add(it) },
                        onSwipeRotate = { rotate(it) },
                    )
                    if (showLabels) {
                        Text(
                            regionHint(currentView),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Intensity: ${intensity.roundToInt()}/10", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = intensity,
                        onValueChange = {
                            intensity = it
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 1f..10f,
                        steps = 8,
                    )
                    Text("Symptoms", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MigraineSymptom.entries.forEach { symptom ->
                            val selected = symptom in selectedSymptoms
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (selected) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                                },
                                label = { Text(symptom.label) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Triggers", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MigraineTrigger.entries.forEach { trigger ->
                            val selected = trigger in selectedTriggers
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedTriggers.remove(trigger) else selectedTriggers.add(trigger)
                                },
                                label = { Text(trigger.label) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                val idx = markers.indexOfLast { it.viewId == currentView.name }
                                if (idx >= 0) markers.removeAt(idx)
                            },
                            enabled = markers.any { it.viewId == currentView.name },
                            modifier = Modifier.weight(1f),
                        ) { Text("Undo point") }
                        OutlinedButton(
                            onClick = { markers.removeAll { it.viewId == currentView.name } },
                            enabled = markers.any { it.viewId == currentView.name },
                            modifier = Modifier.weight(1f),
                        ) { Text("Clear view (${markers.count { it.viewId == currentView.name }})") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val overall = if (markers.isEmpty()) {
                                    intensity.roundToInt()
                                } else {
                                    markers.map { it.intensity }.average().roundToInt()
                                }
                                repository.save(
                                    MigraineEpisode(
                                        timestampMillis = System.currentTimeMillis(),
                                        overallIntensity = overall.coerceIn(1, 10),
                                        notes = notes.trim(),
                                        symptoms = selectedSymptoms.toList(),
                                        markers = markers.toList(),
                                        triggers = selectedTriggers.toList(),
                                    ),
                                )
                                markers.clear()
                                selectedSymptoms.clear()
                                selectedTriggers.clear()
                                notes = ""
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                snackbar.showSnackbar("Episode saved")
                                pane = MigrainePane.History
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = markers.isNotEmpty() || notes.isNotBlank() ||
                            selectedSymptoms.isNotEmpty() || selectedTriggers.isNotEmpty(),
                    ) { Text("Save episode · ${markers.size} points") }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HeadPainMap(
    view: HeadView,
    markers: List<PainMarker>,
    interactive: Boolean,
    intensityForTap: Float = 5f,
    onAddMarker: ((PainMarker) -> Unit)? = null,
    onSwipeRotate: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val resId = when (view) {
        HeadView.FRONT -> R.drawable.head_front
        HeadView.LEFT -> R.drawable.head_left
        HeadView.RIGHT -> R.drawable.head_left
        HeadView.BACK -> R.drawable.head_back
        HeadView.TOP -> R.drawable.head_top
    }
    val mirror = view == HeadView.RIGHT
    val dragAccum = remember { floatArrayOf(0f) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(resId),
                contentDescription = "${view.label} head",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleX = if (mirror) -1f else 1f, scaleY = 1f),
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (interactive && onAddMarker != null) {
                            Modifier.pointerInput(intensityForTap, view) {
                                detectTapGestures { offset ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val nx = (offset.x / size.width).coerceIn(0f, 1f)
                                    val ny = (offset.y / size.height).coerceIn(0f, 1f)
                                    onAddMarker(
                                        PainMarker(
                                            id = UUID.randomUUID().toString(),
                                            x = nx,
                                            y = ny,
                                            intensity = intensityForTap.roundToInt().coerceIn(1, 10),
                                            viewId = view.name,
                                        ),
                                    )
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive && onSwipeRotate != null) {
                            Modifier.pointerInput(view) {
                                detectHorizontalDragGestures(
                                    onDragEnd = { dragAccum[0] = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccum[0] += dragAmount
                                        if (dragAccum[0] > 80f) {
                                            onSwipeRotate(-1)
                                            dragAccum[0] = 0f
                                        } else if (dragAccum[0] < -80f) {
                                            onSwipeRotate(1)
                                            dragAccum[0] = 0f
                                        }
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                drawPainMarkers(markers)
            }
        }
    }
}

private fun regionHint(view: HeadView): String = when (view) {
    HeadView.FRONT -> "Front · temples, forehead, jaw"
    HeadView.BACK -> "Back · occiput, nape, rear temples"
    HeadView.LEFT -> "Left · temple, jaw, occiput"
    HeadView.RIGHT -> "Right · temple, jaw, occiput (mirrored left image)"
    HeadView.TOP -> "Top · crown, forehead, temples"
}

private fun intensityColor(intensity: Int): Color {
    val t = ((intensity - 1).coerceIn(0, 9)) / 9f
    val r = (0.15f + t * 0.85f)
    val g = (0.75f - t * 0.65f).coerceAtLeast(0.12f)
    val b = (0.95f - t * 0.85f).coerceAtLeast(0.08f)
    return Color(r, g, b)
}

private fun DrawScope.drawPainMarkers(markers: List<PainMarker>) {
    val w = size.width
    val h = size.height
    markers.forEach { marker ->
        val cx = marker.x * w
        val cy = marker.y * h
        val radius = 16f + marker.intensity * 3.6f
        val c = intensityColor(marker.intensity)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c.copy(alpha = 0.55f), c.copy(alpha = 0.0f)),
                center = Offset(cx, cy),
                radius = radius * 2.2f,
            ),
            radius = radius * 2.2f,
            center = Offset(cx, cy),
        )
        drawCircle(color = c.copy(alpha = 0.85f), radius = radius, center = Offset(cx, cy))
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = radius * 0.28f,
            center = Offset(cx - radius * 0.22f, cy - radius * 0.22f),
        )
    }
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d · HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}
