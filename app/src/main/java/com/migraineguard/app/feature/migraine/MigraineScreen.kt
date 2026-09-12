package com.migraineguard.app.feature.migraine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.design.MigraineGuardPainColor
import com.migraineguard.app.core.domain.PainMarker
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Interactive head silhouette: tap to place pain markers; intensity via slider.
 * State is local for this starter — persist later via core/data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigraineScreen(modifier: Modifier = Modifier) {
    val markers = remember { mutableStateListOf<PainMarker>() }
    var intensity by remember { mutableFloatStateOf(5f) }
    val outlineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Migraine") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "Tap the head to place pain markers. Adjust intensity, then tap again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .pointerInput(intensity) {
                        detectTapGestures { offset ->
                            val nx = (offset.x / size.width).coerceIn(0f, 1f)
                            val ny = (offset.y / size.height).coerceIn(0f, 1f)
                            markers.add(
                                PainMarker(
                                    id = UUID.randomUUID().toString(),
                                    x = nx,
                                    y = ny,
                                    intensity = intensity.roundToInt().coerceIn(1, 10),
                                )
                            )
                        }
                    },
            ) {
                val w = size.width
                val h = size.height
                val headPath = headSilhouettePath(w, h)

                drawPath(path = headPath, color = fillColor)
                drawPath(path = headPath, color = outlineColor, style = Stroke(width = 4f))

                // Simple facial guidelines
                drawOval(
                    color = outlineColor.copy(alpha = 0.4f),
                    topLeft = Offset(w * 0.32f, h * 0.38f),
                    size = Size(w * 0.12f, h * 0.06f),
                    style = Stroke(width = 2f),
                )
                drawOval(
                    color = outlineColor.copy(alpha = 0.4f),
                    topLeft = Offset(w * 0.56f, h * 0.38f),
                    size = Size(w * 0.12f, h * 0.06f),
                    style = Stroke(width = 2f),
                )

                markers.forEach { marker ->
                    val radius = 12f + marker.intensity * 3f
                    val alpha = 0.35f + marker.intensity * 0.06f
                    drawCircle(
                        color = MigraineGuardPainColor.copy(alpha = alpha.coerceAtMost(0.95f)),
                        radius = radius,
                        center = Offset(marker.x * w, marker.y * h),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(marker.x * w, marker.y * h),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Intensity: ${intensity.roundToInt()}")
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 1f..10f,
                steps = 8,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { if (markers.isNotEmpty()) markers.removeAt(markers.lastIndex) },
                    enabled = markers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Undo")
                }
                Button(
                    onClick = { markers.clear() },
                    enabled = markers.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Clear (${markers.size})")
                }
            }
        }
    }
}

/** Stylized front-facing head + neck path in canvas coordinates. */
private fun headSilhouettePath(width: Float, height: Float): Path {
    val cx = width * 0.5f
    return Path().apply {
        // Crown
        moveTo(cx, height * 0.08f)
        cubicTo(
            width * 0.78f, height * 0.08f,
            width * 0.88f, height * 0.28f,
            width * 0.86f, height * 0.48f,
        )
        // Jaw right
        cubicTo(
            width * 0.84f, height * 0.62f,
            width * 0.72f, height * 0.72f,
            cx, height * 0.76f,
        )
        // Jaw left
        cubicTo(
            width * 0.28f, height * 0.72f,
            width * 0.16f, height * 0.62f,
            width * 0.14f, height * 0.48f,
        )
        cubicTo(
            width * 0.12f, height * 0.28f,
            width * 0.22f, height * 0.08f,
            cx, height * 0.08f,
        )
        close()
        // Neck
        moveTo(width * 0.40f, height * 0.74f)
        lineTo(width * 0.38f, height * 0.92f)
        lineTo(width * 0.62f, height * 0.92f)
        lineTo(width * 0.60f, height * 0.74f)
    }
}
