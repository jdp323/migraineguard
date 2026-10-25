package com.migraineguard.app.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

val LocalReduceTransparency = staticCompositionLocalOf { false }

/** Original layered glass treatment. Only the backdrop is diffused; text stays sharp. */
@Composable
fun GlassBackdrop(dark: Boolean, content: @Composable () -> Unit) {
    val base = if(dark) Color(0xFF080E20) else Color(0xFFE8EEF9)
    Box(Modifier.fillMaxSize().background(base)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF635BFF).copy(alpha=if(dark).32f else .19f),Color.Transparent),Offset(size.width*.05f,size.height*.12f),size.width*1.2f))
            drawRect(Brush.radialGradient(listOf(Color(0xFF32DCCA).copy(alpha=if(dark).21f else .17f),Color.Transparent),Offset(size.width*.95f,size.height*.52f),size.width*1.2f))
            drawRect(Brush.radialGradient(listOf(Color(0xFFCC8BFF).copy(alpha=if(dark).16f else .14f),Color.Transparent),Offset(size.width*.1f,size.height*.9f),size.width))
        }
        content()
    }
}

@Composable
fun Modifier.glassPanel(shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), elevated: Boolean = false): Modifier {
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    val reduced = LocalReduceTransparency.current
    val dark = colors.onSurface.luminance() > .5f
    val fill = if(dark) Color(0xFF1D2B46) else Color.White
    return this.shadow(if(elevated) 14.dp else 5.dp, shape, ambientColor=Color(0xFF172246).copy(alpha=.12f),spotColor=Color(0xFF172246).copy(alpha=.16f))
        .clip(shape).background(if(reduced) fill else fill.copy(alpha=if(dark).68f else .64f))
        .drawWithCache {
            val sheen = Brush.linearGradient(listOf(Color.White.copy(alpha=if(dark).11f else .52f),Color.Transparent,colors.secondary.copy(alpha=.035f)),Offset.Zero,Offset(size.width,size.height))
            onDrawBehind { if(!reduced) drawRect(sheen) }
        }.border(1.dp,Brush.linearGradient(listOf(Color.White.copy(alpha=if(dark).28f else .92f),colors.primary.copy(alpha=.08f),Color.White.copy(alpha=if(dark).07f else .35f))),shape)
}
