package com.migraineguard.app.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyOverview(steps: Int?, stepGoal: Int, water: Int, waterGoal: Int, sleep: Float?,
    sleepGoal: Float, refreshed: String?, onFitness: () -> Unit, onWater: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(
        listOf(Color(0xFF655AC6), Color(0xFF344F79)))).padding(20.dp)) {
        Text("YOUR DAILY RHYTHM", style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f), letterSpacing = 1.8.sp)
        Spacer(Modifier.height(6.dp))
        Text("A little progress, every day.", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RhythmMetric("Steps", steps?.toString() ?: "—", stepGoal.toString() + " goal",
                (steps ?: 0).toFloat() / stepGoal.coerceAtLeast(1), Color(0xFFC7C0FF), onFitness)
            RhythmMetric("Water", water.toString(), waterGoal.toString() + " cups",
                water.toFloat() / waterGoal.coerceAtLeast(1), Color(0xFF9CE8D6), onWater)
            RhythmMetric("Sleep", sleep?.let { "%.1f".format(it) } ?: "—", sleepGoal.toString() + "h goal",
                (sleep ?: 0f) / sleepGoal.coerceAtLeast(1f), Color(0xFFFFCDB4), onFitness)
        }
        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
        Spacer(Modifier.height(12.dp))
        Text(if (refreshed != null) "Health Connect · checked " + refreshed else "Your habits, together in one place",
            color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RhythmMetric(label: String, value: String, goal: String, progress: Float, color: Color, onClick: () -> Unit) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(450), label = "habit progress")
    Column(Modifier.width(74.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxSize(),
                color = color, trackColor = Color.White.copy(alpha = 0.15f), strokeWidth = 5.dp)
            Text(value, color = Color.White, fontSize = 17.sp, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Text(goal, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
    }
}
