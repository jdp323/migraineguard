package com.migraineguard.app

import android.app.Application
import com.migraineguard.app.di.AppContainer
import com.migraineguard.app.feature.reminders.ReminderWorker

/** Application entry — holds the manual DI container. */
class MigraineGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderWorker.ensureChannel(this)
    }
}
