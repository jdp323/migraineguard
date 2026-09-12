package com.migraineguard.app

import android.app.Application
import com.migraineguard.app.di.AppContainer

/** Application entry — holds the manual DI container. */
class MigraineGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
