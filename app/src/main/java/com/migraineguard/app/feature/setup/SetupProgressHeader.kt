package com.migraineguard.app.feature.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.ui.CheckState

internal data class SetupCheck(val title: String, val detail: String, val state: CheckState)

@Composable
internal fun SetupProgressHeader(checks: List<SetupCheck>, current: Int) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            checks.forEachIndexed { index, check ->
                val verified = check.state == CheckState.VERIFIED
                val active = index == current
                val color = when { verified -> MaterialTheme.colorScheme.secondary
                    active -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant }
                Column(Modifier.weight(1f).semantics {
                    contentDescription = check.title + " setup: " + when (check.state) {
                        CheckState.VERIFIED -> "verified"; CheckState.SKIPPED -> "skipped"
                        CheckState.CHECKING -> "checking"; else -> "not verified"
                    }
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(color.copy(alpha = if (verified || active) .16f else .07f)), Alignment.Center) {
                        when {
                            verified -> Icon(Icons.Filled.Check, null, Modifier.size(17.dp), tint = color)
                            check.state == CheckState.SKIPPED -> Icon(Icons.Filled.Remove, null, Modifier.size(17.dp), tint = color)
                            else -> Text((index + 1).toString(), style = MaterialTheme.typography.labelSmall, color = color)
                        }
                    }
                    Text(check.title, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Verified steps stay checked · optional steps can be skipped", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
