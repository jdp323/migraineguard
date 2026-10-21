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
    private var restoringSession = false
    private lateinit var remembered: com.migraineguard.app.feature.auth.RememberedSession
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        remembered = com.migraineguard.app.feature.auth.RememberedSession(this)
        auth = DefaultAuthRepository({ container.preferencesRepository }, ::switchAccount)
        current.value = AppContainer(this, AccountStorage.SIGNED_OUT, auth)
        val saved = remembered.load()
        if (saved != null) {
            restoringSession = true
            try { auth.restoreRememberedSession(saved) } finally { restoringSession = false }
        } else {
            getSharedPreferences("reminder_owner",MODE_PRIVATE).edit().clear().commit()
            ReminderWorker.schedule(this,false)
        }
        ReminderWorker.ensureChannel(this)
    }

    private fun switchAccount(state: AuthState) {
        val account = when {
            !state.isSignedIn -> AccountStorage.SIGNED_OUT
            state.isDemo -> AccountStorage.LOCAL
            else -> AccountStorage.google(requireNotNull(state.user).id)
        }
        remembered.save(state)
        if (current.value?.account == account) return
        val sameRestoredOwner = restoringSession && getSharedPreferences("reminder_owner",MODE_PRIVATE).getString("active_account",null)==account
        if (!sameRestoredOwner) {
        getSharedPreferences("reminder_owner", MODE_PRIVATE).edit().apply {
            if (state.isSignedIn) putString("active_account", account) else remove("active_account")
            remove("water_token"); remove("water_issued")
        }.commit()
        ReminderWorker.schedule(this, false)
        getSystemService(android.app.NotificationManager::class.java)?.cancelAll()
        }
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
