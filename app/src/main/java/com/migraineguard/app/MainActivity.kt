package com.migraineguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.di.AppContainer
import com.migraineguard.app.navigation.MigraineGuardNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MigraineGuardApp
        setContent {
            MigraineGuardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MigraineGuardRoot(container = app.container)
                }
            }
        }
    }
}

@Composable
private fun MigraineGuardRoot(container: AppContainer) {
    // Keep LocalContext available for future permission / Credential Manager flows
    LocalContext.current
    MigraineGuardNavHost(container = container)
}
