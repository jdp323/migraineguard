package com.migraineguard.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
    TopAppBar(title = { ProvideTextStyle(MaterialTheme.typography.titleLarge, title) },
        modifier = modifier, navigationIcon = navigationIcon, actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
}

@Composable
fun FeatureHeader(title: String, subtitle: String, icon: ImageVector,
    modifier: Modifier = Modifier, eyebrow: String? = null) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
        .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer.copy(alpha = .55f))))) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(colors.primary.copy(alpha = .06f), radius = size.width * .45f,
                center = Offset(size.width, 0f))
            drawCircle(colors.secondary.copy(alpha = .07f), radius = size.width * .28f,
                center = Offset(size.width * .95f, size.height))
        }
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, Modifier.size(22.dp), tint = colors.onPrimaryContainer)
                eyebrow?.let { Text(it.uppercase(), color = colors.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp) }
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.onPrimaryContainer)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer.copy(alpha = .85f))
        }
    }
}

@Composable
fun ActionTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit,
    modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .6f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.secondaryContainer), Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
