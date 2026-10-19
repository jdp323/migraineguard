package com.migraineguard.app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.UserProfile
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.auth.DefaultAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Synthetic identities only; verifies storage through the same account activation path as Google. */
@RunWith(AndroidJUnit4::class)
class AccountIsolationTest {
    @Test fun switchingAndSigningOutSeparateAllTablesProfilesKeysAndHealthConsent() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<MigraineGuardApp>()
        val auth = app.container.authRepository as DefaultAuthRepository
        val suffix = UUID.randomUUID().toString()
        val a = UserProfile("a-$suffix@example.invalid", "Fixture A", "a-$suffix@example.invalid")
        val b = UserProfile("b-$suffix@example.invalid", "Fixture B", "b-$suffix@example.invalid")
        try {
            withContext(Dispatchers.Main) { auth.activateGoogleProfile(a) }
            val first = app.container
            first.preferencesRepository.setGeminiApiKey("fixture-key-A")
            first.preferencesRepository.setGroqApiKey("fixture-groq-A")
            first.preferencesRepository.setOpenRouterApiKey("fixture-router-A")
            first.preferencesRepository.saveBodyProfile(BodyProfile(displayName = "Private A", weightKg = 70f))
            first.healthDataRepository.allowDeviceHealth()
            val db = AppDatabase.get(app, first.account)
            db.migraineDao().insert(MigraineEpisodeEntity(timestampMillis = 1000, overallIntensity = 5,
                notes = "A journal", symptoms = emptyList(), markers = emptyList()))
            db.waterHistoryDao().upsert(WaterDayEntity("2026-10-01", 4))
            db.nutritionDao().insert(NutritionScanEntity(timestampMillis = 1000, foodLabel = "A meal",
                caloriesEstimate = null, proteinG = null, carbsG = null, fatG = null,
                notes = "A food", imageUri = null, migraineTriggerHint = null))
            db.workoutDao().insert(WorkoutEntity(timestampMillis = 1000, typeName = "WALK",
                durationMinutes = 10, calories = null, notes = "A workout"))
            db.weightDao().insert(WeightEntryEntity(timestampMillis = 1000, weightKg = 70f))
            db.medicationDao().insert(MedicationEntity(name = "A medicine", dose = "fixture", timeLabel = "08:00"))
            db.aiSessionDao().insert(AiSessionEntity(title = "A AI", personaId = "coach",
                summary = "A summary", fullResponse = "A response", timestampMillis = 1000))
            withContext(Dispatchers.Main) { auth.signOut() }
            assertEquals(AccountStorage.SIGNED_OUT, app.container.account)
            assertEquals("", app.container.preferencesRepository.getGeminiApiKey())
            assertEquals("", app.container.preferencesRepository.preferences.first().bodyProfile.displayName)
            withContext(Dispatchers.Main) { auth.activateGoogleProfile(b) }
            val second = app.container
            val prefsB = second.preferencesRepository.preferences.first()
            assertEquals("Fixture B", prefsB.bodyProfile.displayName)
            assertNull(prefsB.bodyProfile.weightKg)
            assertFalse(prefsB.deviceHealthEnabled)
            assertEquals("", prefsB.geminiApiKey)
            assertEquals("", prefsB.groqApiKey)
            assertEquals("", prefsB.openRouterApiKey)
            assertNull(second.healthDataRepository.latestFitness().observedSteps)
            val empty = AppDatabase.get(app, second.account).backupDao()
            assertTrue(empty.migraines().isEmpty())
            assertTrue(empty.water().isEmpty())
            assertTrue(empty.nutrition().isEmpty())
            assertTrue(empty.workouts().isEmpty())
            assertTrue(empty.weights().isEmpty())
            assertTrue(empty.medications().isEmpty())
            assertTrue(empty.sessions().isEmpty())
            second.preferencesRepository.setGeminiApiKey("fixture-key-B")
            withContext(Dispatchers.Main) { auth.signOut(); auth.activateGoogleProfile(a) }
            assertNotSame(first, app.container)
            val restored = app.container.preferencesRepository.preferences.first()
            assertEquals("Private A", restored.bodyProfile.displayName)
            assertEquals("fixture-key-A", restored.geminiApiKey)
            assertTrue(restored.deviceHealthEnabled)
            assertEquals("A journal", AppDatabase.get(app, app.container.account).backupDao().migraines().single().notes)
            assertEquals("A response", AppDatabase.get(app, app.container.account).backupDao().sessions().single().fullResponse)
            withContext(Dispatchers.Main) { auth.signOut(); auth.signInDemo() }
            assertEquals(AccountStorage.LOCAL, app.container.account)
            assertEquals("migraineguard.db", AccountStorage.databaseName(app.container.account))
        } finally {
            withContext(Dispatchers.Main) { auth.signOut() }
        }
    }
}
