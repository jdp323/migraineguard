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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.design.MigraineGuardPainColor
import com.migraineguard.app.core.domain.HeadView
import com.migraineguard.app.core.domain.MigraineEpisode
import com.migraineguard.app.core.domain.MigraineSymptom
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
    var currentView by remember { mutableStateOf(HeadView.FRONT) }
    var dragAccum by remember { mutableFloatStateOf(0f) }
    val outlineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)

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
                        "Rotate the head (buttons or swipe) and tap to place pain points on each side. Markers stay per view.",
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
                                    onDragEnd = { dragAccum = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccum += dragAmount
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
                        drawHeadView(
                            view = currentView,
                            fill = fillColor,
                            outline = outlineColor,
                            markers = markers.filter { it.viewId == currentView.name },
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
                                    ),
                                )
                                markers.clear()
                                selectedSymptoms.clear()
                                notes = ""
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                snackbar.showSnackbar("Episode saved")
                                pane = MigrainePane.History
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = markers.isNotEmpty() || notes.isNotBlank() || selectedSymptoms.isNotEmpty(),
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
        drawHeadView(view, fill, outline, markers)
    }
}

private fun DrawScope.drawHeadView(
    view: HeadView,
    fill: Color,
    outline: Color,
    markers: List<PainMarker>,
) {
    val w = size.width
    val h = size.height
    val path = headSilhouettePath(view, w, h)
    drawPath(path = path, color = fill)
    drawPath(path = path, color = outline, style = Stroke(width = 5f))
    drawFacialHints(view, w, h, outline)
    markers.forEach { marker ->
        val radius = 14f + marker.intensity * 3.2f
        val alpha = 0.35f + marker.intensity * 0.06f
        drawCircle(
            color = MigraineGuardPainColor.copy(alpha = alpha.coerceAtMost(0.95f)),
            radius = radius,
            center = Offset(marker.x * w, marker.y * h),
        )
        drawCircle(
            color = Color.White,
            radius = 4.5f,
            center = Offset(marker.x * w, marker.y * h),
        )
    }
}

private fun DrawScope.drawFacialHints(view: HeadView, w: Float, h: Float, outline: Color) {
    val faint = outline.copy(alpha = 0.35f)
    when (view) {
        HeadView.FRONT -> {
            drawOval(faint, Offset(w * 0.32f, h * 0.38f), Size(w * 0.12f, h * 0.06f), style = Stroke(2f))
            drawOval(faint, Offset(w * 0.56f, h * 0.38f), Size(w * 0.12f, h * 0.06f), style = Stroke(2f))
            // ears
            drawOval(faint, Offset(w * 0.06f, h * 0.40f), Size(w * 0.08f, h * 0.12f), style = Stroke(2.5f))
            drawOval(faint, Offset(w * 0.86f, h * 0.40f), Size(w * 0.08f, h * 0.12f), style = Stroke(2.5f))
        }
        HeadView.LEFT, HeadView.RIGHT -> {
            val eyeX = if (view == HeadView.LEFT) w * 0.58f else w * 0.30f
            drawOval(faint, Offset(eyeX, h * 0.38f), Size(w * 0.14f, h * 0.06f), style = Stroke(2f))
            val earX = if (view == HeadView.LEFT) w * 0.12f else w * 0.78f
            drawOval(faint, Offset(earX, h * 0.40f), Size(w * 0.10f, h * 0.14f), style = Stroke(2.5f))
            // nose hint
            val noseX = if (view == HeadView.LEFT) w * 0.72f else w * 0.22f
            drawLine(faint, Offset(noseX, h * 0.42f), Offset(noseX + (if (view == HeadView.LEFT) 0.06f else -0.06f) * w, h * 0.52f), strokeWidth = 2.5f)
        }
        HeadView.BACK -> {
            // ear hints on both sides
            drawOval(faint, Offset(w * 0.08f, h * 0.40f), Size(w * 0.08f, h * 0.12f), style = Stroke(2.5f))
            drawOval(faint, Offset(w * 0.84f, h * 0.40f), Size(w * 0.08f, h * 0.12f), style = Stroke(2.5f))
            // hairline hint
            drawArc(faint, -200f, 220f, false, Offset(w * 0.22f, h * 0.10f), Size(w * 0.56f, h * 0.28f), style = Stroke(2.5f))
        }
        HeadView.TOP -> {
            // crown oval + ear tips
            drawOval(faint, Offset(w * 0.28f, h * 0.28f), Size(w * 0.44f, h * 0.40f), style = Stroke(2.5f))
            drawOval(faint, Offset(w * 0.10f, h * 0.42f), Size(w * 0.10f, h * 0.10f), style = Stroke(2f))
            drawOval(faint, Offset(w * 0.80f, h * 0.42f), Size(w * 0.10f, h * 0.10f), style = Stroke(2f))
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
                // top-down oval head with slight flat at forehead
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        left = width * 0.18f,
                        top = height * 0.18f,
                        right = width * 0.82f,
                        bottom = height * 0.78f,
                    ),
                )
            }
            HeadView.LEFT, HeadView.RIGHT -> {
                val flip = view == HeadView.RIGHT
                fun fx(x: Float) = if (flip) width - x else x
                moveTo(fx(cx * 0.95f), height * 0.08f)
                cubicTo(fx(width * 0.78f), height * 0.06f, fx(width * 0.88f), height * 0.22f, fx(width * 0.86f), height * 0.42f)
                cubicTo(fx(width * 0.84f), height * 0.58f, fx(width * 0.70f), height * 0.70f, fx(cx), height * 0.74f)
                cubicTo(fx(width * 0.28f), height * 0.70f, fx(width * 0.16f), height * 0.58f, fx(width * 0.18f), height * 0.42f)
                cubicTo(fx(width * 0.20f), height * 0.24f, fx(width * 0.32f), height * 0.08f, fx(cx * 0.95f), height * 0.08f)
                close()
                // neck
                moveTo(fx(width * 0.40f), height * 0.72f)
                lineTo(fx(width * 0.36f), height * 0.94f)
                lineTo(fx(width * 0.62f), height * 0.94f)
                lineTo(fx(width * 0.58f), height * 0.72f)
            }
            HeadView.BACK, HeadView.FRONT -> {
                moveTo(cx, height * 0.06f)
                cubicTo(width * 0.80f, height * 0.06f, width * 0.90f, height * 0.26f, width * 0.88f, height * 0.46f)
                cubicTo(width * 0.86f, height * 0.62f, width * 0.74f, height * 0.72f, cx, height * 0.76f)
                cubicTo(width * 0.26f, height * 0.72f, width * 0.14f, height * 0.62f, width * 0.12f, height * 0.46f)
                cubicTo(width * 0.10f, height * 0.26f, width * 0.20f, height * 0.06f, cx, height * 0.06f)
                close()
                moveTo(width * 0.40f, height * 0.74f)
                lineTo(width * 0.37f, height * 0.94f)
                lineTo(width * 0.63f, height * 0.94f)
                lineTo(width * 0.60f, height * 0.74f)
            }
        }
    }
}
