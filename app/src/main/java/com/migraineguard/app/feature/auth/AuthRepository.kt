package com.migraineguard.app.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class AuthState(
    val isSignedIn: Boolean = false,
    val user: UserProfile? = null,
    val isDemo: Boolean = false,
)

interface AuthRepository {
    val authState: StateFlow<AuthState>
    fun hasConfiguredClientId(): Boolean
    fun signInDemo()
    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile>
    fun signOut()
}

class DefaultAuthRepository : AuthRepository {
    private val _state = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _state.asStateFlow()

    override fun hasConfiguredClientId(): Boolean {
        val id = BuildConfig.GOOGLE_WEB_CLIENT_ID
        return id.isNotBlank() && !id.startsWith("YOUR_") && id.contains(".apps.googleusercontent.com")
    }

    override fun signInDemo() {
        _state.value = AuthState(
            isSignedIn = true,
            isDemo = true,
            user = UserProfile(
                id = "demo-user",
                displayName = "Demo User",
                email = "demo@migraineguard.app",
            ),
        )
    }

    override suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> {
        if (!hasConfiguredClientId()) {
            // Soft demo path so UI still works without client ID
            signInDemo()
            return Result.success(
                _state.value.user!!,
            )
        }
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(
                context = activityContext,
                request = request,
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                val profile = UserProfile(
                    id = google.id.ifBlank { UUID.randomUUID().toString() },
                    displayName = google.displayName ?: "Google User",
                    email = google.id,
                    photoUrl = google.profilePictureUri?.toString(),
                )
                _state.value = AuthState(isSignedIn = true, isDemo = false, user = profile)
                Result.success(profile)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        _state.value = AuthState()
    }
}
