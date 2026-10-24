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

    @Test fun bodyProgressStaysLocalUntilPhotoOptInAndDeletionSurvivesCloudMerge() = runBlocking {
        val email="photo-"+UUID.randomUUID()+"@example.invalid"
        val owner=AccountStorage.google(email)
        val prefs=UserPreferencesRepository(context,owner)
        val db=AppDatabase.get(context,owner)
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        val remote=MemoryDrive()
        val repository=DriveSyncRepository(owner,prefs,db,BackupRepository(context,db,prefs),scope,remote)
        val entry=BodyProgressEntity(UUID.randomUUID().toString(),"2026-10-01","Front",notes="private body fixture",weightKg=70f,modifiedAtMillis=1)
        try {
            db.bodyProgressDao().put(entry)
            repository.connect(email,email);repository.backupNow()
            assertTrue(CloudBackupFormat.decode(remote.saved!!,owner).journal.bodyProgress.isEmpty())
            prefs.setBodyPhotoDriveBackup(true);prefs.setMovementReminders(true,90);repository.backupNow()
            val included=CloudBackupFormat.decode(remote.saved!!,owner)
            assertEquals(entry,included.journal.bodyProgress.single())
            assertTrue(included.settings.movementReminders);assertEquals(90,included.settings.movementReminderMinutes)
            com.migraineguard.app.feature.body.BodyProgressRepository(context,db.bodyProgressDao()).remove(entry)
            repository.backupNow()
            assertTrue(CloudBackupFormat.decode(remote.saved!!,owner).journal.bodyProgress.single().deleted)
            prefs.setBodyPhotoDriveBackup(false);repository.backupNow()
            assertTrue(CloudBackupFormat.decode(remote.saved!!,owner).journal.bodyProgress.isEmpty())
            assertTrue(db.bodyProgressDao().all().single().deleted)
        } finally { repository.dispose();scope.cancel() }
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
            first.saveAiConnection("openai", "fixture-new-provider-key", "gpt-4.1-mini")
            first.setWeeklyWorkoutGoal(4)
            first.setReminderOptions(true,30,180,360,360,9,21)
            first.setDeviceHealthEnabled(true)
            first.setSetupComplete(true)
            sourceDb.migraineDao().insert(MigraineEpisodeEntity(timestampMillis = 1000, overallIntensity = 4,
                notes = "fixture restored journal", symptoms = emptyList(), markers = emptyList()))
            sourceDb.workoutDao().insert(WorkoutEntity.fromDomain(com.migraineguard.app.core.domain.WorkoutEntry(timestampMillis=1000,type=com.migraineguard.app.core.domain.WorkoutType.TREADMILL,durationMinutes=20,exerciseName="Treadmill fixture",details=com.migraineguard.app.core.domain.WorkoutDetails(speedKmh=6.5f,inclinePercent=3f,distanceKm=2f))))
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
            assertEquals("openai",restored.preferredAiProvider)
            assertEquals("fixture-new-provider-key",restored.aiConnections["openai"]!!.apiKey)
            assertEquals(4,restored.weeklyWorkoutGoal)
            assertTrue(restored.foodReminders)
            assertEquals(30,restored.waterReminderMinutes)
            assertEquals(drive.saved!!.size.toLong(),target.state.value.backupBytes)
            assertEquals(6.5f,targetDb.backupDao().workouts().single().toDomain().details.speedKmh!!,0.01f)
            assertTrue(restored.setupComplete)
            assertFalse(restored.deviceHealthEnabled)
            assertEquals("fixture restored journal", targetDb.backupDao().migraines().single().notes)
            val file = File(context.filesDir, "datastore/" + AccountStorage.settingsName(targetId) + ".preferences_pb")
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("fixture-cloud-gemini"))
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains("fixture-new-provider-key"))
            target.backupNow()
            assertEquals(1, targetDb.backupDao().migraines().size)
        } finally { source.dispose(); target.dispose(); sourceScope.cancel(); targetScope.cancel() }
    }

    @Test fun cancelledDriveCheckCanBeRetriedWithoutLosingLocalProfile() = runBlocking {
        val email="cancel-"+UUID.randomUUID()+"@example.invalid"
        val owner=AccountStorage.google(email)
        val prefs=UserPreferencesRepository(context,owner)
        prefs.saveBodyProfile(BodyProfile(displayName="Retained fixture",profileComplete=true))
        val db=AppDatabase.get(context,owner)
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        val started=CompletableDeferred<Unit>()
        var block=true
        val remote=object:DriveBackupTransport {
            override suspend fun verifyAccount(token:String,email:String) {}
            override suspend fun download(token:String):DriveFile? { if(block) { started.complete(Unit); awaitCancellation() }; return null }
            override suspend fun upload(token:String,payload:ByteArray,previous:DriveFile?)="fixture"
        }
        val repository=DriveSyncRepository(owner,prefs,db,BackupRepository(context,db,prefs),scope,remote)
        try {
            val job=launch { repository.connect("fixture-token",email) }
            started.await(); job.cancelAndJoin()
            assertFalse(repository.state.value.busy)
            assertEquals("Retained fixture",prefs.preferences.first().bodyProfile.displayName)
            block=false; repository.connect("fixture-token",email)
            assertTrue(repository.state.value.connected)
        } finally { repository.dispose(); scope.cancel() }
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
        prefs.setSetupComplete(true)
        val completed = prefs.preferences.first()
        assertTrue(prefs.restoreCloudSettings(completed.copy(setupComplete=false,modifiedAtMillis=completed.modifiedAtMillis+1000)))
        assertTrue(prefs.preferences.first().setupComplete)
    }
}
