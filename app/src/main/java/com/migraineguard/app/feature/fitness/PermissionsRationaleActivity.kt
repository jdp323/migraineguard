package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.ui.AppTopBar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.migraineguard.app.R
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.ui.SectionCard

/**
 * Shown by Health Connect when the user taps MigraineGuard under Connected apps
 * or when the system needs a permissions rationale.
 */
class PermissionsRationaleActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MigraineGuardTheme(themeMode = ThemeMode.SYSTEM, dynamicColor = false) {
                Scaffold(
                    topBar = {
                        AppTopBar(title = { Text(stringResource(R.string.health_connect_rationale_title)) })
                    },
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SectionCard(
                            title = "Your health data, your choices",
                            icon = Icons.Filled.MonitorHeart,
                        ) {
                            Text(
                                stringResource(R.string.health_connect_rationale),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        Text(
                            "Read permissions: Steps, Sleep, Heart rate, Exercise, Distance and total Calories. Movement reminders can separately request optional background reads. No exercise-write permission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Got it")
                        }
                    }
                }
            }
        }
    }
}
