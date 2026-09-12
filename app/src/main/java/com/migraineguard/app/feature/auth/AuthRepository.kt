package com.migraineguard.app.feature.auth

import com.migraineguard.app.core.domain.UserProfile
import kotlinx.coroutines.flow.StateFlow

data class AuthState(
    val isSignedIn: Boolean = false,
    val user: UserProfile? = null,
)

/**
 * Auth gate — Google Sign-In ONLY for production.
 *
 * Real flow (to implement):
 * 1. Create OAuth 2.0 Web client ID in Google Cloud Console
 * 2. Put it in local.properties as GOOGLE_WEB_CLIENT_ID=...
 * 3. Use Credential Manager + GoogleIdOption (see SignInScreen comments)
 *
 * [FakeAuthRepository] provides a demo toggle so navigation works without secrets.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    fun signInDemo()
    fun signOut()
}

class FakeAuthRepository : AuthRepository {
    private val _state = kotlinx.coroutines.flow.MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _state

    override fun signInDemo() {
        _state.value = AuthState(
            isSignedIn = true,
            user = UserProfile(
                id = "demo-user",
                displayName = "Demo User",
                email = "demo@migraineguard.app",
            ),
        )
    }

    override fun signOut() {
        _state.value = AuthState()
    }
}
