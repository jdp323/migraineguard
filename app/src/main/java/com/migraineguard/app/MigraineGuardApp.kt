package com.migraineguard.app

import android.app.Application
import com.migraineguard.app.di.AppContainer

/**
 * Application entry point. Holds a simple manual DI container (no Hilt yet)
 * so features can share fake repositories during the starter phase.
 */
class MigraineGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
