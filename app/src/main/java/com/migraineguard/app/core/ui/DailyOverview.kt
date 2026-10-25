package com.migraineguard.app.core.ui

import com.migraineguard.app.core.design.glassPanel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat

/** Compact branded summary; unavailable readings stay unknown and never fill a goal bar. */
@Composable
fun DailyOverview(steps: Int?,stepGoal: Int,water: Int,waterGoal: Int,sleep: Float?,
    sleepGoal: Float,refreshed: String?,onFitness: ()->Unit,onWater: ()->Unit) {
    Box(Modifier.fillMaxWidth().glassPanel(elevated=true)
        .background(Brush.linearGradient(listOf(Color(0xFF39468E).copy(alpha=.92f),Color(0xFF146776).copy(alpha=.87f))))) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.linearGradient(listOf(Color.White.copy(alpha=.24f),Color.Transparent,Color.White.copy(alpha=.03f)),Offset.Zero,Offset(size.width*.7f,size.height)))
            drawCircle(Color.White.copy(alpha=.07f),size.width*.55f,Offset(size.width,size.height))
            drawCircle(Color.White.copy(alpha=.04f),size.width*.32f,Offset(size.width*.9f,0f))
        }
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text("Daily essentials",color=Color.White,style=MaterialTheme.typography.titleMedium,modifier=Modifier.weight(1f))
                Icon(Icons.Default.Spa,null,Modifier.size(20.dp),tint=Color(0xFF9CE8D6))
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                EssentialMetric("Steps",steps?.let { NumberFormat.getIntegerInstance().format(it) } ?: "—",null,
                    (steps ?: 0).toFloat()/stepGoal.coerceAtLeast(1),Icons.Default.DirectionsWalk,Color(0xFFD2D4FF),Modifier.weight(1f),onFitness)
                EssentialMetric("Water",water.toString(),"/ $waterGoal",water.toFloat()/waterGoal.coerceAtLeast(1),Icons.Default.WaterDrop,Color(0xFF9CE8D6),Modifier.weight(1f),onWater)
                EssentialMetric("Sleep",sleep?.let { "%.1f".format(it) } ?: "—","h",(sleep ?: 0f)/sleepGoal.coerceAtLeast(1f),Icons.Default.Bedtime,Color(0xFFFFD4B9),Modifier.weight(1f),onFitness)
            }
            FilledTonalButton(onClick=onWater,modifier=Modifier.fillMaxWidth(),
                colors=ButtonDefaults.filledTonalButtonColors(containerColor=Color.White.copy(alpha=.14f),contentColor=Color.White)) {
                Icon(Icons.Default.Add,null,Modifier.size(17.dp)); Spacer(Modifier.width(6.dp)); Text("Add water")
            }
        }
    }
}

@Composable
private fun EssentialMetric(label:String,value:String,suffix:String?,progress:Float,icon:ImageVector,tint:Color,modifier:Modifier,onClick:()->Unit) {
    val p by animateFloatAsState(progress.coerceIn(0f,1f),tween(360),label="essentials progress")
    Column(modifier.clip(RoundedCornerShape(10.dp)).clickable(role=Role.Button,onClickLabel="Open "+label,onClick=onClick).padding(vertical=2.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            Icon(icon,null,Modifier.size(13.dp),tint=tint)
            Text(label,color=Color.White.copy(alpha=.85f),style=MaterialTheme.typography.labelMedium)
        }
        Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(3.dp)) {
            Text(value,color=Color.White,style=MaterialTheme.typography.titleLarge,fontSize=if(value.length>5)18.sp else 22.sp)
            suffix?.let { Text(it,color=Color.White.copy(alpha=.75f),style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(bottom=3.dp)) }
        }
        LinearProgressIndicator(progress={p},modifier=Modifier.fillMaxWidth().height(3.dp),color=tint,trackColor=Color.White.copy(alpha=.13f))
    }
}
