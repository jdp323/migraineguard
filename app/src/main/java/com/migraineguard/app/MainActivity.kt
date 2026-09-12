package com.migraineguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.navigation.MigraineGuardNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MigraineGuardApp
        setContent {
            val prefs by app.container.preferencesRepository.preferences
                .collectAsStateWithLifecycle(UserPreferences())
            MigraineGuardTheme(
                themeMode = prefs.themeMode,
                dynamicColor = prefs.dynamicColor,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MigraineGuardNavHost(container = app.container)
                }
            }
        }
    }
}
