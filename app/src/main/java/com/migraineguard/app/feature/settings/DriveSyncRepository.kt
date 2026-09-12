package com.migraineguard.app.feature.settings

/**
 * Google Drive backup / sync — interface only for this starter.
 * Implement with Drive REST API + Google Sign-In scopes later.
 */
interface DriveSyncRepository {
    suspend fun isSyncEnabled(): Boolean
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun lastSyncLabel(): String
}

class FakeDriveSyncRepository : DriveSyncRepository {
    @Volatile
    private var enabled = false

    override suspend fun isSyncEnabled(): Boolean = enabled

    override suspend fun setSyncEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override suspend fun lastSyncLabel(): String =
        if (enabled) "Never (Drive API not wired)" else "Sync off"
}
