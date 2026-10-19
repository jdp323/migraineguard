package com.migraineguard.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.backup.BackupRepository
import com.migraineguard.app.feature.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.io.File

@RunWith(AndroidJUnit4::class)
class DriveRestoreTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private class MemoryDrive : DriveBackupTransport {
        var saved: ByteArray? = null
        override suspend fun verifyAccount(token: String, email: String) { require(token == email) }
        override suspend fun download(token: String) = saved?.let { DriveFile("fixture-drive-file", "fixture-version", it.copyOf()) }
        override suspend fun upload(token: String, payload: ByteArray, previous: DriveFile?): String {
            saved = payload.copyOf(); return "fixture-drive-file"
        }
    }

    @Test fun freshInstallationRestoresBirthdayMetricsKeysJournalAndSetup() = runBlocking {
        val uuid = UUID.randomUUID().toString()
        val email = "drive-$uuid@example.invalid"
        val owner = AccountStorage.google(email)
        val sourceId = AccountStorage.google("source-$uuid")
        val targetId = AccountStorage.google("target-$uuid")
        val first = UserPreferencesRepository(context, sourceId)
        val second = UserPreferencesRepository(context, targetId)
        val sourceDb = AppDatabase.get(context, sourceId)
        val targetDb = AppDatabase.get(context, targetId)
        val sourceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val targetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val drive = MemoryDrive()
        val source = DriveSyncRepository(owner, first, sourceDb,
            BackupRepository(context, sourceDb, first), sourceScope, drive)
        val target = DriveSyncRepository(owner, second, targetDb,
            BackupRepository(context, targetDb, second), targetScope, drive)
        try {
            first.saveBodyProfile(BodyProfile(displayName = "Cloud fixture", dateOfBirthIso = "1990-03-15",
                heightCm = 181f, weightKg = 72f, profileComplete = true))
            first.setGeminiApiKey("fixture-cloud-gemini")
            first.setGroqApiKey("fixture-cloud-groq")
            first.setOpenRouterApiKey("fixture-cloud-router")
            first.setDeviceHealthEnabled(true)
            first.setSetupComplete(true)
            sourceDb.migraineDao().insert(MigraineEpisodeEntity(timestampMillis = 1000, overallIntensity = 4,
                notes = "fixture restored journal", symptoms = emptyList(), markers = emptyList()))
            source.connect(email, email)
            source.backupNow()
            assertNotNull(drive.saved)
            target.connect(email, email)
            val restored = second.preferences.first()
            assertEquals("Cloud fixture", restored.bodyProfile.displayName)
            assertEquals("1990-03-15", restored.bodyProfile.dateOfBirthIso)
            assertEquals(181f, restored.bodyProfile.heightCm!!, 0f)
            assertEquals(72f, restored.bodyProfile.weightKg!!, 0f)
            assertEquals("fixture-cloud-gemini", restored.geminiApiKey)
            assertEquals("fixture-cloud-groq", restored.groqApiKey)
            assertEquals("fixture-cloud-router", restored.openRouterApiKey)
            assertTrue(restored.setupComplete)
            assertFalse(restored.deviceHealthEnabled)
            assertEquals("fixture restored journal", targetDb.backupDao().migraines().single().notes)
            val file = File(context.filesDir, "datastore/" + AccountStorage.settingsName(targetId) + ".preferences_pb")
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("fixture-cloud-gemini"))
            target.backupNow()
            assertEquals(1, targetDb.backupDao().migraines().size)
        } finally { source.dispose(); target.dispose(); sourceScope.cancel(); targetScope.cancel() }
    }

    @Test fun remoteRestoreCannotOverwriteNewerLocalProfileOrKeys() = runBlocking {
        val id = AccountStorage.google("revision-fixture-" + UUID.randomUUID())
        val prefs = UserPreferencesRepository(context, id)
        prefs.saveBodyProfile(BodyProfile(displayName = "Current", weightKg = 75f))
        prefs.setGeminiApiKey("fixture-new-key")
        val current = prefs.preferences.first()
        val applied = prefs.restoreCloudSettings(current.copy(modifiedAtMillis = 0,
            geminiApiKey = "fixture-old-key", bodyProfile = BodyProfile(displayName = "Old")))
        assertFalse(applied)
        assertEquals("Current", prefs.preferences.first().bodyProfile.displayName)
        assertEquals("fixture-new-key", prefs.getGeminiApiKey())
    }
}
