package com.migraineguard.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.migraineguard.app.BuildConfig

/**
 * Google Sign-In gate (Credential Manager / Google Identity placeholders).
 *
 * Production wiring sketch:
 * ```
 * val credentialManager = CredentialManager.create(context)
 * val googleIdOption = GetGoogleIdOption.Builder()
 *     .setFilterByAuthorizedAccounts(false)
 *     .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
 *     .build()
 * val request = GetCredentialRequest.Builder()
 *     .addCredentialOption(googleIdOption)
 *     .build()
 * // credentialManager.getCredential(context, request) …
 * ```
 *
 * See README for OAuth client ID setup. Never commit real client secrets.
 */
@Composable
fun SignInScreen(
    onDemoSignIn: () -> Unit,
    onGoogleSignInPlaceholder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "MigraineGuard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Track migraines, habits, and triggers — sign in with Google to continue.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
        )

        Button(
            onClick = onGoogleSignInPlaceholder,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign in with Google")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDemoSignIn,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue as demo user")
        }

        Text(
            text = "Client ID configured: ${
                if (BuildConfig.GOOGLE_WEB_CLIENT_ID.startsWith("YOUR_")) "not set" else "yes (placeholder)"
            }",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}
