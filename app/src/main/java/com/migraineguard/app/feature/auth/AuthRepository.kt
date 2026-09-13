package com.migraineguard.app.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
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
    suspend fun resolveClientId(): String?
    suspend fun hasConfiguredClientId(): Boolean
    fun signInDemo()
    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile>
    fun signOut()
}

class DefaultAuthRepository(
    private val preferencesRepository: UserPreferencesRepository,
) : AuthRepository {
    private val _state = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _state.asStateFlow()

    override suspend fun resolveClientId(): String? {
        val prefsId = preferencesRepository.getGoogleWebClientId().trim()
        val buildId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        val id = when {
            isValidClientId(prefsId) -> prefsId
            isValidClientId(buildId) -> buildId
            else -> null
        }
        return id
    }

    override suspend fun hasConfiguredClientId(): Boolean = resolveClientId() != null

    private fun isValidClientId(id: String): Boolean =
        id.isNotBlank() && !id.startsWith("YOUR_") && id.contains(".apps.googleusercontent.com")

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
        val clientId = resolveClientId()
            ?: return Result.failure(
                IllegalStateException(
                    "Google Web Client ID missing. Reinstall the app or paste one in Settings → Google.",
                ),
            )
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
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
                preferencesRepository.seedProfileNameIfBlank(profile.displayName)
                _state.value = AuthState(isSignedIn = true, isDemo = false, user = profile)
                Result.success(profile)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type from Google Sign-In"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Sign-In cancelled"))
        } catch (e: NoCredentialException) {
            Result.failure(
                IllegalStateException(
                    "No Google account available, or this APK’s signing certificate (SHA-1) " +
                        "is not registered for package ${BuildConfig.APPLICATION_ID}. " +
                        "Add SHA-1 FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14 " +
                        "to the Android OAuth client in Firebase project migraineguard-66229 " +
                        "(or your Google Cloud Console), then try again.",
                ),
            )
        } catch (e: GetCredentialException) {
            val msg = e.message.orEmpty()
            val lower = msg.lowercase()
            val hint = when {
                "sha" in lower || "fingerprint" in lower || "certificate" in lower ||
                    "developer_error" in lower || "10:" in lower || "12500" in msg ->
                    "Likely SHA-1 mismatch: this APK’s signing cert is not on the Android OAuth client " +
                        "for ${BuildConfig.APPLICATION_ID}. Register SHA-1 " +
                        "FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14 " +
                        "in Firebase / Google Cloud (project migraineguard-66229). Details: $msg"
                else -> "Google Sign-In failed: ${msg.ifBlank { e::class.java.simpleName }}. " +
                    "If this persists, check that the APK SHA-1 matches the Android OAuth client."
            }
            Result.failure(IllegalStateException(hint))
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    e.message?.takeIf { it.isNotBlank() } ?: "Google Sign-In failed",
                    e,
                ),
            )
        }
    }

    override fun signOut() {
        _state.value = AuthState()
    }
}
