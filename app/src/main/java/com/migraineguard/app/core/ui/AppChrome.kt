package com.migraineguard.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared page chrome keeps all feature screens in the same visual system. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: @Composable () -> Unit, modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {}, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), title) },
        modifier = modifier, navigationIcon = navigationIcon, actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
}

@Composable
fun FeatureHeader(title: String, subtitle: String, icon: ImageVector,
    modifier: Modifier = Modifier, eyebrow: String? = null) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer.copy(alpha = .65f))))) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(colors.primary.copy(alpha=.05f),size.width*.36f,Offset(size.width,size.height*.1f))
        }
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Icon(icon,null,Modifier.size(18.dp),tint=colors.onPrimaryContainer)
                Text(eyebrow?.uppercase() ?: "MIGRAINEGUARD",style=MaterialTheme.typography.labelSmall,
                    letterSpacing=1.2.sp,color=colors.onPrimaryContainer)
            }
            Text(title,style=MaterialTheme.typography.headlineSmall,color=colors.onPrimaryContainer)
            Text(subtitle,style=MaterialTheme.typography.bodySmall,color=colors.onPrimaryContainer.copy(alpha=.85f))
        }
    }
}

@Composable
fun ActionTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit,
    modifier: Modifier = Modifier) {
    val interaction=remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if(pressed).975f else 1f,
        androidx.compose.animation.core.spring(stiffness=650f,dampingRatio=.85f),label="tile press")
    Card(onClick=onClick,interactionSource=interaction,
        modifier=modifier.graphicsLayer { scaleX=scale; scaleY=scale },shape=RoundedCornerShape(24.dp),
        colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
        border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer),Alignment.Center) {
                    Icon(icon,null,tint=MaterialTheme.colorScheme.onSecondaryContainer,modifier=Modifier.size(19.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward,null,Modifier.size(15.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(title,style=MaterialTheme.typography.titleMedium)
            Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

enum class CheckState { WAITING, CHECKING, VERIFIED, ATTENTION, SKIPPED }

/** A check means evidence was received. Opening settings alone never completes a check. */
@Composable
fun VerificationRow(title: String, detail: String, state: CheckState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val tint by animateColorAsState(when (state) {
        CheckState.VERIFIED -> colors.secondary
        CheckState.ATTENTION -> colors.error
        else -> colors.onSurfaceVariant
    }, tween(180), label = "verification color")
    val label = when (state) {
        CheckState.VERIFIED -> "Verified"
        CheckState.CHECKING -> "Checking"
        CheckState.SKIPPED -> "Skipped"
        CheckState.ATTENTION -> "Needs attention"
        CheckState.WAITING -> "Not verified"
    }
    Row(modifier.fillMaxWidth().testTag("verification-" + title).semantics { liveRegion = LiveRegionMode.Polite }
        .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(tint.copy(alpha = .10f)), Alignment.Center) {
            if (state == CheckState.CHECKING) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = tint)
            else Icon(when (state) { CheckState.VERIFIED -> Icons.Filled.Check; CheckState.ATTENTION -> Icons.Filled.ErrorOutline
                else -> Icons.Filled.Remove }, contentDescription = label, modifier = Modifier.size(19.dp), tint = tint)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
