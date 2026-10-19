package com.migraineguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.navigation.MigraineGuardNavHost

class MainActivity : ComponentActivity() {
    private lateinit var app: MigraineGuardApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app = application as MigraineGuardApp
        setContent {
            val active by app.containers.collectAsStateWithLifecycle()
            val container = active ?: return@setContent
            key(container) {
                val prefs by container.preferencesRepository.preferences
                    .collectAsStateWithLifecycle(UserPreferences())
                MigraineGuardTheme(
                    themeMode = prefs.themeMode,
                    dynamicColor = prefs.dynamicColor,
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MigraineGuardNavHost(container = container)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::app.isInitialized && app.container.authRepository.authState.value.isSignedIn) {
            // Soft auto-refresh Health Connect on foreground
            app.container.healthRefreshCoordinator.refresh(force = false)
        }
    }
}
