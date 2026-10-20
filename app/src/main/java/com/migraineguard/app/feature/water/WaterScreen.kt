package com.migraineguard.app.feature.water

import com.migraineguard.app.core.ui.AppTopBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.WaterUnit
import com.migraineguard.app.core.ui.ProgressRing
import com.migraineguard.app.core.ui.FeatureHeader
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterScreen(
    repository: WaterIntakeRepository,
    preferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val cups by repository.cupsToday.collectAsStateWithLifecycle(initialValue = 0)
    val prefs by preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = com.migraineguard.app.core.data.prefs.UserPreferences(),
    )
    val history by repository.observeHistory(14).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val goal = prefs.waterGoalCups.coerceAtLeast(1)
    val progress = (cups.toFloat() / goal).coerceAtMost(1f)
    val display = when (prefs.waterUnit) {
        WaterUnit.CUPS -> "$cups / $goal cups"
        WaterUnit.ML -> "${cups * 250} / ${goal * 250} ml"
    }

    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = { Text("Water") }, navigationIcon = {
            onBack?.let { back -> androidx.compose.material3.IconButton(onClick = back) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, "Back")
            } }
        }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FeatureHeader("One sip at a time.", "Keep your hydration close throughout the day.", Icons.Filled.WaterDrop, eyebrow = "Your hydration")
            Spacer(Modifier.height(24.dp))
            ProgressRing(progress = progress, label = "$cups")
            Spacer(Modifier.height(8.dp))
            Text(display, style = MaterialTheme.typography.titleMedium)
            Text(
                if (cups >= goal) "Goal reached — nice hydration!" else "Keep sipping toward your daily goal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { repository.removeCup() }
                }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Remove cup")
                }
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                FilledIconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { repository.addCup() }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add cup")
                }
            }
            Spacer(Modifier.height(20.dp))
            SectionCard(title = "Recent days", icon = Icons.Filled.WaterDrop) {
                if (history.isEmpty()) {
                    Text("History appears as you log cups.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    history.forEach { day ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(day.dateIso)
                            Text("${day.cups} cups")
                        }
                    }
                }
            }
        }
    }
}
