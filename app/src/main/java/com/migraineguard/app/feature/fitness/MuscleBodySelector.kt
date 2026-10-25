package com.migraineguard.app.feature.fitness

import android.graphics.Region
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.migraineguard.app.R
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

private data class PreparedAnatomyPart(val slug: String, val muscle: Muscle?, val path: Path, val region: Region)
private data class PreparedAnatomy(val asset: AnatomyAsset, val outline: Path, val parts: List<PreparedAnatomyPart>) {
    fun hit(x: Float, y: Float): Muscle? =
        parts.asReversed().firstOrNull { it.region.contains(floor(x).toInt(), floor(y).toInt()) }?.muscle
}

private fun prepareAnatomy(asset: AnatomyAsset): PreparedAnatomy {
    asset.validate()
    val parts = asset.parts.map { part ->
        val path = Path().apply { part.paths.forEach { addPath(PathParser().parsePathString(it).toPath()) } }
        val bounds = path.getBounds()
        val clip = Region(floor(bounds.left).toInt() - 1, floor(bounds.top).toInt() - 1,
            ceil(bounds.right).toInt() + 1, ceil(bounds.bottom).toInt() + 1)
        PreparedAnatomyPart(part.slug, AnatomyRegionMapping.muscleFor(part.slug), path,
            Region().apply { setPath(path.asAndroidPath(), clip) })
    }
    return PreparedAnatomy(asset, PathParser().parsePathString(asset.outline).toPath(), parts)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleBodySelector(selected: Set<Muscle>, onToggle: (Muscle) -> Unit) {
    var back by rememberSaveable { mutableStateOf(false) }
    var illustrationName by rememberSaveable { mutableStateOf(AnatomyIllustration.MALE.name) }
    val illustration = AnatomyIllustration.valueOf(illustrationName)
    val context = LocalContext.current
    val drawings = remember(illustration) {
        val resources = if (illustration == AnatomyIllustration.MALE)
            listOf(R.raw.anatomy_male_front, R.raw.anatomy_male_back)
        else listOf(R.raw.anatomy_female_front, R.raw.anatomy_female_back)
        resources.map { resource ->
            context.resources.openRawResource(resource).bufferedReader().use { reader ->
                prepareAnatomy(Json.decodeFromString<AnatomyAsset>(reader.readText()))
            }
        }
    }
    val progress by animateFloatAsState(if (back) 1f else 0f, tween(260), label = "anatomy front back")
    val selection by rememberUpdatedState(selected)
    val toggle by rememberUpdatedState(onToggle)
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < .5f
    val seam = if (dark) Color(0xFF26354B) else Color(0xFFF4F7FC)
    val support = if (dark) Color(0xFF566981) else Color(0xFFD7DFEA)
    val muscleFill = Brush.verticalGradient(
        if (dark) listOf(Color(0xFFB0BDD0), Color(0xFF7185A0))
        else listOf(Color(0xFFB7C4D7), Color(0xFF95A9C3)), 100f, 1400f)
    val selectedFill = Brush.verticalGradient(listOf(Color(0xFFFFA18A), Color(0xFFE76758)), 100f, 1400f)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Choose your focus", style = MaterialTheme.typography.titleLarge)
            Text("Tap muscles · swipe for the other side", style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant)
        }
        Text(selected.size.toString() + " selected", style = MaterialTheme.typography.labelMedium,
            color = colors.primary)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("Illustration", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        AnatomyIllustration.entries.forEach { value ->
            FilterChip(illustration == value, { illustrationName = value.name },
                label = { Text(value.label) }, modifier = Modifier.testTag("body-illustration-" + value.name))
        }
    }
    Canvas(Modifier.fillMaxWidth().height(350.dp).testTag("muscle-body").semantics {
        contentDescription = "Interactive anatomical muscle map"
        stateDescription = (if (back) "Back" else "Front") + " · " + illustration.label + " · " +
            selected.joinToString { it.label }
        customActions = listOf(
            CustomAccessibilityAction("Show front") { back = false; true },
            CustomAccessibilityAction("Show back") { back = true; true })
    }.pointerInput(Unit) {
        var distance = 0f
        detectHorizontalDragGestures(
            onDragStart = { distance = 0f },
            onDragEnd = { if (abs(distance) > 32.dp.toPx()) back = !back },
            onDragCancel = { distance = 0f }
        ) { change, amount -> change.consume(); distance += amount }
    }.pointerInput(illustration, back) {
        detectTapGestures { point ->
            if (abs(progress - if (back) 1f else 0f) < .03f) {
                val drawing = drawings[if (back) 1 else 0]
                val transform = AnatomyTransform.fit(drawing.asset.viewBox, size.width.toFloat(), size.height.toFloat())
                drawing.hit(transform.bodyX(point.x), transform.bodyY(point.y))?.let(toggle)
            }
        }
    }) {
        if (size.width <= 0 || size.height <= 0) return@Canvas
        drawOval(colors.primary.copy(alpha = .045f), Offset(size.width * .20f, size.height * .08f),
            Size(size.width * .60f, size.height * .84f))
        drawings.forEachIndexed { index, drawing ->
            val alpha = if (index == 0) 1f - progress else progress
            if (alpha > .001f) {
                val transform = AnatomyTransform.fit(drawing.asset.viewBox, size.width, size.height)
                val slide = if (index == 0) -progress else 1f - progress
                withTransform({
                    translate(transform.left + slide * size.width * .12f, transform.top)
                    scale(transform.scale, transform.scale, Offset.Zero)
                    translate(-transform.originX, -transform.originY)
                }) {
                    drawPath(drawing.outline, support.copy(alpha = alpha))
                    drawPath(drawing.outline, colors.onSurfaceVariant.copy(alpha = .4f * alpha), style = Stroke(2f))
                    drawing.parts.forEach { part ->
                        val picked = part.muscle != null && part.muscle in selection
                        if (part.muscle != null)
                            drawPath(part.path, if (picked) selectedFill else muscleFill, alpha = alpha)
                        else drawPath(part.path,
                            (if (part.slug == "hair") colors.onSurfaceVariant.copy(alpha = .6f) else support).copy(alpha = alpha))
                        drawPath(part.path, seam.copy(alpha = alpha), style = Stroke(if (picked) 2.2f else 1.8f))
                    }
                }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        FilterChip(!back, { back = false }, label = { Text("Front") }, modifier = Modifier.testTag("body-front"))
        FilterChip(back, { back = true }, label = { Text("Back") }, modifier = Modifier.testTag("body-back"))
        TextButton(onClick = { back = !back }) { Text("Flip view") }
    }
    Text("Muscle groups", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Muscle.entries.forEach { muscle ->
            FilterChip(muscle in selected, { onToggle(muscle) }, label = { Text(muscle.label) },
                modifier = Modifier.testTag("muscle-" + muscle.name))
        }
    }
    Text("Illustration choice changes the drawing only. Highlighted areas are training regions, not a pain or injury assessment.",
        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
}
