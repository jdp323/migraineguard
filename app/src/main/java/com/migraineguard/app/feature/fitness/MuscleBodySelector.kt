package com.migraineguard.app.feature.fitness

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleBodySelector(selected:Set<Muscle>,onToggle:(Muscle)->Unit) {
    var target by rememberSaveable { mutableFloatStateOf(-12f) }
    var dragging by remember { mutableStateOf(false) }
    val angle by animateFloatAsState(target,if(dragging)snap() else spring(dampingRatio=.92f,stiffness=180f),label="body rotation")
    val velocity=remember { VelocityTracker() }
    val selection by rememberUpdatedState(selected);val toggle by rememberUpdatedState(onToggle)
    val colors=MaterialTheme.colorScheme
    val front=cos(angle*PI/180)>=.4;val back=cos(angle*PI/180)<=-.4
    fun turn(to:Float) { val delta=((to-target+540f)%360f+360f)%360f-180f;target+=delta }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
        Column { Text("Choose your focus",style=MaterialTheme.typography.titleLarge);Text("Swipe to rotate · tap muscles",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant) }
        Text("${selected.size} selected",style=MaterialTheme.typography.labelMedium,color=colors.primary)
    }
    Canvas(Modifier.fillMaxWidth().height(310.dp).testTag("muscle-body").semantics {
        contentDescription="Interactive training muscle map"
        stateDescription=(if(front)"Front" else if(back)"Back" else "Side")+" · "+selected.joinToString { it.label }
        customActions=listOf(CustomAccessibilityAction("Rotate to front") { turn(0f);true },CustomAccessibilityAction("Rotate to back") { turn(180f);true })
    }.pointerInput(Unit) {
        detectHorizontalDragGestures(onDragStart={dragging=true;velocity.resetTracking()},onDragEnd={ target+=velocity.calculateVelocity().x.coerceIn(-1500f,1500f)*.035f;dragging=false },onDragCancel={dragging=false}) { change, amount ->
            change.consume();velocity.addPosition(change.uptimeMillis,change.position);target+=amount*.5f
        }
    }.pointerInput(Unit) { detectTapGestures { offset -> MuscleBodyMesh.hit(angle,size.width.toFloat(),size.height.toFloat(),offset.x,offset.y)?.let(toggle) } }) {
        drawOval(colors.primary.copy(alpha=.10f),Offset(size.width*.21f,size.height*.94f),androidx.compose.ui.geometry.Size(size.width*.58f,size.height*.035f))
        val projected=MuscleBodyMesh.project(angle,size.width,size.height)
        val path=Path()
        projected.forEach { f ->
            path.reset();path.moveTo(f.a.x,f.a.y);path.lineTo(f.b.x,f.b.y);path.lineTo(f.c.x,f.c.y);path.close()
            val picked=f.muscle in selection
            val base=if(picked) colors.secondary else if(f.muscle!=null) lerp(colors.primary,colors.onSurfaceVariant,.48f).copy(alpha=1f) else colors.onSurfaceVariant.copy(alpha=1f)
            val lit=lerp(base,Color.White,(f.light-.4f)*if(picked).38f else .62f)
            drawPath(path,lit)
            if(picked)drawPath(path,colors.secondary.copy(alpha=.18f),style=androidx.compose.ui.graphics.drawscope.Stroke(.7f))
        }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        FilterChip(front,{turn(0f)},label={Text("Front")},modifier=Modifier.testTag("body-front"))
        FilterChip(back,{turn(180f)},label={Text("Back")},modifier=Modifier.testTag("body-back"))
        TextButton(onClick={target+=90f}) { Text("Rotate 90°") }
    }
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(0.dp)) {
        Muscle.entries.forEach { muscle -> FilterChip(muscle in selected,{onToggle(muscle)},label={Text(muscle.label)},modifier=Modifier.testTag("muscle-"+muscle.name)) }
    }
    Text("Highlighted areas are approximate training regions. Selection is not a pain or injury assessment.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
}
