package com.migraineguard.app.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlinx.coroutines.CancellationException
import com.migraineguard.app.core.security.installedSigningSha1

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
    private val preferencesRepository: () -> UserPreferencesRepository,
    private val onAccountChanged: (AuthState) -> Unit = {},
) : AuthRepository {
    private val _state = MutableStateFlow(AuthState())
    override val authState: StateFlow<AuthState> = _state.asStateFlow()

    override suspend fun resolveClientId(): String? {
        val prefsId = preferencesRepository().getGoogleWebClientId().trim()
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
        onAccountChanged(_state.value)
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
            val googleIdOption = GetSignInWithGoogleOption.Builder(clientId)
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
                activateGoogleProfile(profile)
                Result.success(profile)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type from Google Sign-In"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(IllegalStateException(
                "Google account selection was cancelled or could not finish. " +
                    "If you did not dismiss it, install the latest APK and retry after Google configuration propagates. " +
                    "You can continue locally in the meantime.", e,
            ))
        } catch (e: NoCredentialException) {
            Result.failure(IllegalStateException(
                "Google could not offer an account. Check that a Google account is on this phone. " +
                    "The app owner must also register this installed APK's certificate with the Android OAuth client. " +
                    "Package: ${BuildConfig.APPLICATION_ID}. SHA-1: ${installedSigningSha1(activityContext)}. " +
                    "Google sign-in and Gemini API access are separate; you can continue locally.",
            ))
        } catch (e: GetCredentialException) {
            Result.failure(IllegalStateException(
                "Google sign-in failed (${e::class.java.simpleName}). " +
                    "Check the Android OAuth client for package ${BuildConfig.APPLICATION_ID} and " +
                    "this installed APK's SHA-1: ${installedSigningSha1(activityContext)}. " +
                    "It must be in the same Google project as the Web client ID.",
            ))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    e.message?.takeIf { it.isNotBlank() } ?: "Google Sign-In failed",
                    e,
                ),
            )
        }
    }

    /** Called only after Credential Manager returns a Google ID credential. */
    internal suspend fun activateGoogleProfile(profile: UserProfile) {
        val next = AuthState(isSignedIn = true, isDemo = false, user = profile)
        onAccountChanged(next) // Swap all storage before exposing the signed-in identity.
        _state.value = next
        preferencesRepository().seedProfileNameIfBlank(profile.displayName)
    }

    override fun signOut() {
        _state.value = AuthState()
        onAccountChanged(_state.value)
    }
}
