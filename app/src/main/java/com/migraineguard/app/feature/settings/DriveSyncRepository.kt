package com.migraineguard.app.feature.settings

import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Google Drive backup / sync.
 * Persists the toggle in DataStore; actual Drive REST upload is intentionally a clear TODO
 * so the app builds without Drive credentials.
 */
interface DriveSyncRepository {
    suspend fun isSyncEnabled(): Boolean
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun lastSyncLabel(): String
    suspend fun statusMessage(): String
    suspend fun requestSyncNow(): String
}

class DriveSyncRepositoryImpl(
    private val prefs: UserPreferencesRepository,
) : DriveSyncRepository {
    @Volatile private var lastAttemptIso: String? = null

    override suspend fun isSyncEnabled(): Boolean = prefs.preferences.first().driveSyncEnabled

    override suspend fun setSyncEnabled(enabled: Boolean) {
        prefs.setDriveSyncEnabled(enabled)
        if (enabled) {
            lastAttemptIso = Instant.now().toString()
        }
    }

    override suspend fun lastSyncLabel(): String {
        val enabled = isSyncEnabled()
        return when {
            !enabled -> "Sync off"
            lastAttemptIso == null -> "No cloud backup available"
            else -> {
                val fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())
                "Requested ${fmt.format(Instant.parse(lastAttemptIso))} — no files uploaded"
            }
        }
    }

    override suspend fun statusMessage(): String =
        "Cloud backup is not available yet. Your records remain on this device; this setting does not upload or protect them."

    override suspend fun requestSyncNow(): String {
        if (!isSyncEnabled()) return "Turn on Drive sync first."
        lastAttemptIso = Instant.now().toString()
        // TODO(Drive): export Room tables → JSON → Drive.appDataFolder
        return "No backup uploaded. Cloud backup is not available yet."
    }
}
