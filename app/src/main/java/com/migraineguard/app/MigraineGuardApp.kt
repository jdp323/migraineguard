package com.migraineguard.app

import android.app.Application
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.di.AppContainer
import com.migraineguard.app.feature.auth.AuthState
import com.migraineguard.app.feature.auth.DefaultAuthRepository
import com.migraineguard.app.feature.reminders.ReminderWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A fresh composition/repository graph for each login, including the signed-out screen. */
class MigraineGuardApp : Application() {
    private val current = MutableStateFlow<AppContainer?>(null)
    val containers = current.asStateFlow()
    val container: AppContainer get() = requireNotNull(current.value)
    private lateinit var auth: DefaultAuthRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        auth = DefaultAuthRepository({ container.preferencesRepository }, ::switchAccount)
        current.value = AppContainer(this, AccountStorage.SIGNED_OUT, auth)
        ReminderWorker.ensureChannel(this)
    }

    private fun switchAccount(state: AuthState) {
        val account = when {
            !state.isSignedIn -> AccountStorage.SIGNED_OUT
            state.isDemo -> AccountStorage.LOCAL
            else -> AccountStorage.google(requireNotNull(state.user).id)
        }
        if (current.value?.account == account) return
        ReminderWorker.schedule(this, false)
        getSystemService(android.app.NotificationManager::class.java)?.cancelAll()
        val previous = current.value
        current.value = AppContainer(this, account, auth)
        previous?.dispose()
        if (!state.isSignedIn) scope.launch {
            try { CredentialManager.create(this@MigraineGuardApp)
                .clearCredentialState(ClearCredentialStateRequest())
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { /* Local isolation does not depend on the provider. */ }
        }
    }
}
