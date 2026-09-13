package com.migraineguard.app.feature.migraine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var dragAccum by remember { mutableFloatStateOf(0f) }
    var parallaxX by remember { mutableFloatStateOf(0f) }
    var showLabels by remember { mutableStateOf(true) }
    val outlineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)

    fun rotate(delta: Int) {
        val idx = ViewOrder.indexOf(currentView)
        currentView = ViewOrder[(idx + delta + ViewOrder.size) % ViewOrder.size]
        parallaxX = 0f
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
                        subtitle = "Tap + to log your first migraine with the rotatable head map.",
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
                                MiniHeadPreview(
                                    markers = ep.markers.filter { it.viewId == view.name },
                                    view = view,
                                    outline = outlineColor,
                                    fill = fillColor,
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
                        "Rotate the head (buttons or swipe) and tap to place glowing pain orbs. Drag gently for parallax.",
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
                                onClick = { currentView = v; parallaxX = 0f },
                                label = { Text(if (count > 0) "${v.label} ($count)" else v.label) },
                            )
                        }
                        FilterChip(
                            selected = showLabels,
                            onClick = { showLabels = !showLabels },
                            label = { Text(if (showLabels) "Labels on" else "Labels off") },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.78f)
                            .pointerInput(intensity, currentView) {
                                detectTapGestures { offset ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val nx = (offset.x / size.width).coerceIn(0f, 1f)
                                    val ny = (offset.y / size.height).coerceIn(0f, 1f)
                                    markers.add(
                                        PainMarker(
                                            id = UUID.randomUUID().toString(),
                                            x = nx,
                                            y = ny,
                                            intensity = intensity.roundToInt().coerceIn(1, 10),
                                            viewId = currentView.name,
                                        ),
                                    )
                                }
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        dragAccum = 0f
                                        parallaxX *= 0.35f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccum += dragAmount
                                        parallaxX = (parallaxX + dragAmount * 0.08f).coerceIn(-18f, 18f)
                                        if (dragAccum > 80f) {
                                            rotate(-1)
                                            dragAccum = 0f
                                        } else if (dragAccum < -80f) {
                                            rotate(1)
                                            dragAccum = 0f
                                        }
                                    },
                                )
                            },
                    ) {
                        drawHeadView3d(
                            view = currentView,
                            fill = fillColor,
                            outline = outlineColor,
                            markers = markers.filter { it.viewId == currentView.name },
                            parallaxX = parallaxX,
                            showLabels = showLabels,
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
private fun MiniHeadPreview(
    markers: List<PainMarker>,
    view: HeadView,
    outline: Color,
    fill: Color,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
    ) {
        drawHeadView3d(view, fill, outline, markers, parallaxX = 0f, showLabels = false)
    }
}

private fun intensityColor(intensity: Int): Color {
    val t = ((intensity - 1).coerceIn(0, 9)) / 9f
    // cool (cyan) → hot (red/orange)
    val r = (0.15f + t * 0.85f)
    val g = (0.75f - t * 0.65f).coerceAtLeast(0.12f)
    val b = (0.95f - t * 0.85f).coerceAtLeast(0.08f)
    return Color(r, g, b)
}

private fun DrawScope.drawHeadView3d(
    view: HeadView,
    fill: Color,
    outline: Color,
    markers: List<PainMarker>,
    parallaxX: Float,
    showLabels: Boolean,
) {
    val w = size.width
    val h = size.height
    withTransform({
        translate(left = parallaxX, top = parallaxX * 0.15f)
    }) {
        // Soft drop shadow
        val shadowPath = headSilhouettePath(view, w, h)
        withTransform({ translate(6f, 10f) }) {
            drawPath(shadowPath, Color.Black.copy(alpha = 0.18f))
        }

        val path = headSilhouettePath(view, w, h)
        val gradient = Brush.radialGradient(
            colors = listOf(
                fill.copy(alpha = 0.95f),
                fill.copy(alpha = 0.55f),
                outline.copy(alpha = 0.25f),
            ),
            center = Offset(w * (0.42f + parallaxX / w * 0.05f), h * 0.32f),
            radius = w * 0.72f,
        )
        drawPath(path = path, brush = gradient)

        // Depth rings
        val ring = outline.copy(alpha = 0.12f)
        drawOval(
            color = ring,
            topLeft = Offset(w * 0.22f, h * 0.14f),
            size = Size(w * 0.56f, h * 0.52f),
            style = Stroke(width = 3f),
        )
        drawOval(
            color = ring.copy(alpha = 0.08f),
            topLeft = Offset(w * 0.28f, h * 0.20f),
            size = Size(w * 0.44f, h * 0.40f),
            style = Stroke(width = 2f),
        )

        // Specular highlight
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.38f, h * 0.26f),
                radius = w * 0.22f,
            ),
            topLeft = Offset(w * 0.26f, h * 0.16f),
            size = Size(w * 0.28f, h * 0.22f),
        )

        drawPath(path = path, color = outline.copy(alpha = 0.85f), style = Stroke(width = 5.5f))
        drawFacialHints3d(view, w, h, outline)

        if (showLabels) {
            drawRegionLabels(view, w, h, outline)
        }

        markers.forEach { marker ->
            val cx = marker.x * w
            val cy = marker.y * h
            val radius = 16f + marker.intensity * 3.6f
            val c = intensityColor(marker.intensity)
            // Glow
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
}

private fun DrawScope.drawRegionLabels(view: HeadView, w: Float, h: Float, outline: Color) {
    val labelColor = outline.copy(alpha = 0.45f)
    // Lightweight dots + tiny ticks as region hints (text via drawContext is heavy; use markers)
    val spots: List<Pair<Offset, String>> = when (view) {
        HeadView.FRONT -> listOf(
            Offset(w * 0.28f, h * 0.34f) to "temple",
            Offset(w * 0.72f, h * 0.34f) to "temple",
            Offset(w * 0.50f, h * 0.22f) to "forehead",
            Offset(w * 0.50f, h * 0.58f) to "jaw",
        )
        HeadView.BACK -> listOf(
            Offset(w * 0.50f, h * 0.42f) to "occiput",
            Offset(w * 0.28f, h * 0.36f) to "temple",
            Offset(w * 0.72f, h * 0.36f) to "temple",
        )
        HeadView.LEFT -> listOf(
            Offset(w * 0.55f, h * 0.30f) to "temple",
            Offset(w * 0.45f, h * 0.55f) to "jaw",
            Offset(w * 0.35f, h * 0.40f) to "occiput",
        )
        HeadView.RIGHT -> listOf(
            Offset(w * 0.45f, h * 0.30f) to "temple",
            Offset(w * 0.55f, h * 0.55f) to "jaw",
            Offset(w * 0.65f, h * 0.40f) to "occiput",
        )
        HeadView.TOP -> listOf(
            Offset(w * 0.50f, h * 0.28f) to "forehead",
            Offset(w * 0.50f, h * 0.62f) to "occiput",
            Offset(w * 0.28f, h * 0.45f) to "temple",
            Offset(w * 0.72f, h * 0.45f) to "temple",
        )
    }
    spots.forEach { (o, _) ->
        drawCircle(labelColor, 3.5f, o)
        drawCircle(labelColor.copy(alpha = 0.2f), 10f, o)
    }
}

private fun DrawScope.drawFacialHints3d(view: HeadView, w: Float, h: Float, outline: Color) {
    val faint = outline.copy(alpha = 0.40f)
    when (view) {
        HeadView.FRONT -> {
            drawOval(faint, Offset(w * 0.32f, h * 0.38f), Size(w * 0.12f, h * 0.06f), style = Stroke(2.5f))
            drawOval(faint, Offset(w * 0.56f, h * 0.38f), Size(w * 0.12f, h * 0.06f), style = Stroke(2.5f))
            // nose bridge
            drawLine(faint, Offset(w * 0.50f, h * 0.42f), Offset(w * 0.50f, h * 0.52f), strokeWidth = 2.5f)
            drawLine(faint, Offset(w * 0.46f, h * 0.54f), Offset(w * 0.54f, h * 0.54f), strokeWidth = 2f)
            // jaw soft curve
            drawArc(faint, 20f, 140f, false, Offset(w * 0.30f, h * 0.52f), Size(w * 0.40f, h * 0.28f), style = Stroke(2.5f))
            // ears with depth
            drawOval(Color.Black.copy(alpha = 0.08f), Offset(w * 0.05f, h * 0.40f), Size(w * 0.09f, h * 0.13f))
            drawOval(faint, Offset(w * 0.05f, h * 0.40f), Size(w * 0.09f, h * 0.13f), style = Stroke(2.5f))
            drawOval(Color.Black.copy(alpha = 0.08f), Offset(w * 0.86f, h * 0.40f), Size(w * 0.09f, h * 0.13f))
            drawOval(faint, Offset(w * 0.86f, h * 0.40f), Size(w * 0.09f, h * 0.13f), style = Stroke(2.5f))
        }
        HeadView.LEFT, HeadView.RIGHT -> {
            val eyeX = if (view == HeadView.LEFT) w * 0.58f else w * 0.30f
            drawOval(faint, Offset(eyeX, h * 0.38f), Size(w * 0.14f, h * 0.06f), style = Stroke(2.5f))
            val earX = if (view == HeadView.LEFT) w * 0.10f else w * 0.78f
            drawOval(Color.Black.copy(alpha = 0.10f), Offset(earX, h * 0.38f), Size(w * 0.11f, h * 0.16f))
            drawOval(faint, Offset(earX, h * 0.38f), Size(w * 0.11f, h * 0.16f), style = Stroke(2.5f))
            val noseX = if (view == HeadView.LEFT) w * 0.72f else w * 0.22f
            val dx = if (view == HeadView.LEFT) 0.07f else -0.07f
            drawLine(faint, Offset(noseX, h * 0.42f), Offset(noseX + dx * w, h * 0.52f), strokeWidth = 3f)
            // jaw line
            drawLine(faint, Offset(w * 0.35f, h * 0.62f), Offset(w * 0.62f, h * 0.70f), strokeWidth = 2.5f)
        }
        HeadView.BACK -> {
            drawOval(faint, Offset(w * 0.07f, h * 0.40f), Size(w * 0.09f, h * 0.13f), style = Stroke(2.5f))
            drawOval(faint, Offset(w * 0.84f, h * 0.40f), Size(w * 0.09f, h * 0.13f), style = Stroke(2.5f))
            drawArc(faint, -200f, 220f, false, Offset(w * 0.20f, h * 0.10f), Size(w * 0.60f, h * 0.30f), style = Stroke(3f))
            // nuchal hint
            drawArc(faint, 40f, 100f, false, Offset(w * 0.35f, h * 0.52f), Size(w * 0.30f, h * 0.22f), style = Stroke(2.5f))
        }
        HeadView.TOP -> {
            drawOval(faint, Offset(w * 0.26f, h * 0.26f), Size(w * 0.48f, h * 0.44f), style = Stroke(3f))
            drawOval(faint, Offset(w * 0.09f, h * 0.42f), Size(w * 0.11f, h * 0.11f), style = Stroke(2f))
            drawOval(faint, Offset(w * 0.80f, h * 0.42f), Size(w * 0.11f, h * 0.11f), style = Stroke(2f))
            // forehead flat hint
            drawLine(faint, Offset(w * 0.32f, h * 0.30f), Offset(w * 0.68f, h * 0.30f), strokeWidth = 2f)
        }
    }
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d · HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}

private fun headSilhouettePath(view: HeadView, width: Float, height: Float): Path {
    val cx = width * 0.5f
    return Path().apply {
        when (view) {
            HeadView.TOP -> {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        left = width * 0.16f,
                        top = height * 0.16f,
                        right = width * 0.84f,
                        bottom = height * 0.80f,
                    ),
                )
            }
            HeadView.LEFT, HeadView.RIGHT -> {
                val flip = view == HeadView.RIGHT
                fun fx(x: Float) = if (flip) width - x else x
                moveTo(fx(cx * 0.95f), height * 0.08f)
                cubicTo(fx(width * 0.78f), height * 0.05f, fx(width * 0.90f), height * 0.22f, fx(width * 0.88f), height * 0.42f)
                cubicTo(fx(width * 0.86f), height * 0.58f, fx(width * 0.72f), height * 0.72f, fx(cx), height * 0.76f)
                cubicTo(fx(width * 0.26f), height * 0.72f, fx(width * 0.14f), height * 0.58f, fx(width * 0.16f), height * 0.42f)
                cubicTo(fx(width * 0.18f), height * 0.22f, fx(width * 0.30f), height * 0.07f, fx(cx * 0.95f), height * 0.08f)
                close()
                moveTo(fx(width * 0.40f), height * 0.72f)
                lineTo(fx(width * 0.34f), height * 0.95f)
                lineTo(fx(width * 0.64f), height * 0.95f)
                lineTo(fx(width * 0.58f), height * 0.72f)
            }
            HeadView.BACK, HeadView.FRONT -> {
                moveTo(cx, height * 0.05f)
                cubicTo(width * 0.82f, height * 0.05f, width * 0.92f, height * 0.24f, width * 0.90f, height * 0.46f)
                cubicTo(width * 0.88f, height * 0.64f, width * 0.74f, height * 0.74f, cx, height * 0.78f)
                cubicTo(width * 0.26f, height * 0.74f, width * 0.12f, height * 0.64f, width * 0.10f, height * 0.46f)
                cubicTo(width * 0.08f, height * 0.24f, width * 0.18f, height * 0.05f, cx, height * 0.05f)
                close()
                moveTo(width * 0.40f, height * 0.76f)
                lineTo(width * 0.36f, height * 0.95f)
                lineTo(width * 0.64f, height * 0.95f)
                lineTo(width * 0.60f, height * 0.76f)
            }
        }
    }
}
