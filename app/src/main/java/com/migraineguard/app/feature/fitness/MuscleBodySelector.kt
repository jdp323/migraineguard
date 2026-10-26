package com.migraineguard.app.feature.fitness

import android.graphics.Region
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Rect
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
    val outline = PathParser().parsePathString(asset.outline).toPath()
    asset.bodyClipTop?.let { top ->
        val clip = Path().apply { addRect(Rect(asset.viewBox[0]-10, top, asset.viewBox[0]+asset.viewBox[2]+10, asset.viewBox[1]+asset.viewBox[3]+10)) }
        outline.op(outline, clip, PathOperation.Intersect)
    }
    return PreparedAnatomy(asset, outline, parts)
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
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var panX by rememberSaveable { mutableFloatStateOf(0f) }
    var panY by rememberSaveable { mutableFloatStateOf(0f) }
    var canvasWidth by remember { mutableIntStateOf(0) }
    var canvasHeight by remember { mutableIntStateOf(0) }
    fun setCamera(value: Float, x: Float = panX, y: Float = panY) {
        if(!value.isFinite() || !x.isFinite() || !y.isFinite()) return
        zoom = value.coerceIn(1f, 3f)
        if(canvasWidth > 0 && canvasHeight > 0) {
            val box = drawings[if(back) 1 else 0].asset.viewBox
            val t = AnatomyTransform.fit(box,canvasWidth.toFloat(),canvasHeight.toFloat(),zoom,x,y)
            panX = t.left-(canvasWidth-box[2]*t.scale)/2
            panY = t.top-(canvasHeight-box[3]*t.scale)/2
        }
    }
    var cameraViewKey by rememberSaveable { mutableStateOf(illustration.name+back) }
    LaunchedEffect(illustration, back) { val key=illustration.name+back;if(cameraViewKey!=key){panX=0f;panY=0f;cameraViewKey=key} }
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
            Text("Tap muscles · pinch to zoom", style = MaterialTheme.typography.bodySmall,
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
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick={setCamera(zoom-.5f)},enabled=zoom>1f,modifier=Modifier.testTag("body-zoom-out")) { Icon(Icons.Default.ZoomOut,"Zoom out") }
        Text((zoom*100).toInt().toString()+"%",style=MaterialTheme.typography.labelLarge)
        IconButton(onClick={setCamera(zoom+.5f)},enabled=zoom<3f,modifier=Modifier.testTag("body-zoom-in")) { Icon(Icons.Default.ZoomIn,"Zoom in") }
        TextButton(onClick={setCamera(1f,0f,0f)},modifier=Modifier.testTag("body-zoom-fit")) { Text("Fit body") }
    }
    Text(if(zoom>1.01f) "Drag to explore · double-tap to fit" else "Swipe for front/back · double-tap to enlarge",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
    Canvas(Modifier.fillMaxWidth().height(350.dp).clipToBounds().onSizeChanged { canvasWidth=it.width;canvasHeight=it.height }.testTag("muscle-body").semantics {
        contentDescription = "Interactive anatomical muscle map"
        stateDescription = (if (back) "Back" else "Front") + " · " + illustration.label + " · " +
            selected.joinToString { it.label } + " · Zoom " + (zoom*100).toInt()+"%"
        customActions = listOf(
            CustomAccessibilityAction("Show front") { back = false; true },
            CustomAccessibilityAction("Show back") { back = true; true })
    }.pointerInput(illustration, back) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var distance = Offset.Zero
            var captured = false
            var multiple = false
            do {
                val event = awaitPointerEvent()
                val pan = event.calculatePan()
                val pressed = event.changes.count { it.pressed }
                if (pressed > 1 || multiple) {
                    multiple = true
                    val factor = event.calculateZoom()
                    val nextZoom = (zoom * factor).coerceIn(1f, 3f)
                    val ratio = nextZoom / zoom
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (centroid.x.isFinite() && centroid.y.isFinite()) {
                        setCamera(nextZoom,
                            panX * ratio + (centroid.x - size.width/2f)*(1-ratio) + pan.x,
                            panY * ratio + (centroid.y - size.height/2f)*(1-ratio) + pan.y)
                    }
                    event.changes.forEach { if (it.position != it.previousPosition) it.consume() }
                } else if (zoom > 1.01f) {
                    if (pan.getDistance() > 0) {
                        distance += pan
                        if (distance.getDistance() > viewConfiguration.touchSlop || captured) {
                            captured = true
                            setCamera(zoom, panX + pan.x, panY + pan.y)
                            event.changes.forEach { it.consume() }
                        }
                    }
                } else {
                    distance += pan
                    if (!captured && abs(distance.y)>viewConfiguration.touchSlop && abs(distance.y)>abs(distance.x)) break
                    if (abs(distance.x)>viewConfiguration.touchSlop) captured = true
                    if (captured) event.changes.forEach { it.consume() }
                }
            } while (event.changes.any { it.pressed })
            if (!multiple && captured && zoom <= 1.01f && abs(distance.x) > 32.dp.toPx()) back = !back
        }
    }.pointerInput(illustration, back) {
        detectTapGestures(onDoubleTap={setCamera(if(zoom>1.01f) 1f else 2f,0f,0f)},onTap={ point ->
            if (abs(progress - if (back) 1f else 0f) < .03f) {
                val drawing = drawings[if (back) 1 else 0]
                val transform = AnatomyTransform.fit(drawing.asset.viewBox, size.width.toFloat(), size.height.toFloat(),zoom,panX,panY)
                drawing.hit(transform.bodyX(point.x), transform.bodyY(point.y))?.let(toggle)
            }
        })
    }) {
        if (size.width <= 0 || size.height <= 0) return@Canvas
        drawOval(colors.primary.copy(alpha = .045f), Offset(size.width * .20f, size.height * .08f),
            Size(size.width * .60f, size.height * .84f))
        drawings.forEachIndexed { index, drawing ->
            val alpha = if (index == 0) 1f - progress else progress
            if (alpha > .001f) {
                val transform = AnatomyTransform.fit(drawing.asset.viewBox, size.width, size.height,zoom,panX,panY)
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
