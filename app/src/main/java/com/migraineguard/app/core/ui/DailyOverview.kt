package com.migraineguard.app.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat

@Composable
fun DailyOverview(steps: Int?, stepGoal: Int, water: Int, waterGoal: Int, sleep: Float?,
    sleepGoal: Float, refreshed: String?, onFitness: () -> Unit, onWater: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF544BA1), Color(0xFF243D62))))) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = .05f), size.width * .5f, Offset(size.width, 0f))
            drawCircle(Color(0xFFA3E8D5).copy(alpha = .10f), size.width * .40f,
                Offset(size.width * .95f, 0f), style = Stroke(1.dp.toPx()))
        }
        Column(Modifier.padding(22.dp)) {
            Text("TODAY'S OVERVIEW", style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD9D4FF), letterSpacing = 1.6.sp)
            Spacer(Modifier.height(8.dp))
            Text("Find your rhythm.", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Movement, hydration & recovery", style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = .8f))
            Spacer(Modifier.height(26.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RhythmMetric("Steps", steps?.let { NumberFormat.getIntegerInstance().format(it) } ?: "—",
                    NumberFormat.getIntegerInstance().format(stepGoal) + " goal", (steps ?: 0).toFloat() / stepGoal.coerceAtLeast(1),
                    Color(0xFFD2C4FF), Modifier.weight(1f), onFitness)
                RhythmMetric("Water", water.toString(), waterGoal.toString() + " cups goal", water.toFloat() / waterGoal.coerceAtLeast(1),
                    Color(0xFF9CE8D6), Modifier.weight(1f), onWater)
                RhythmMetric("Sleep", sleep?.let { "%.1f".format(it) } ?: "—", sleepGoal.toString() + "h goal",
                    (sleep ?: 0f) / sleepGoal.coerceAtLeast(1f), Color(0xFFFFCDB4), Modifier.weight(1f), onFitness)
            }
            Spacer(Modifier.height(22.dp))
            HorizontalDivider(color = Color.White.copy(alpha = .15f))
            Spacer(Modifier.height(12.dp))
            Text(refreshed?.let { "Health Connect · checked " + it } ?: "Tap a habit to see more",
                color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun RhythmMetric(label: String, value: String, goal: String, progress: Float, color: Color,
    modifier: Modifier, onClick: () -> Unit) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(420), label = "habit progress")
    Column(modifier.clip(RoundedCornerShape(18.dp)).clickable(role = Role.Button, onClickLabel = "Open " + label, onClick = onClick)
        .padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxSize(),
                color = color, trackColor = Color.White.copy(alpha = .13f), strokeWidth = 5.dp)
            AnimatedContent(value, label = "habit value") { Text(it, color = Color.White,
                fontSize = if (it.length > 5) 13.sp else 17.sp, style = MaterialTheme.typography.titleMedium) }
        }
        Spacer(Modifier.height(12.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
        Text(goal, color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
    }
}
