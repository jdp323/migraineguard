package com.migraineguard.app.feature.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.migraineguard.app.BuildConfig
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.security.BackupCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@Serializable
data class JournalBackup(
    val formatVersion: Int = 1,
    val createdAtMillis: Long,
    val appVersion: String,
    val profile: BodyProfile? = null,
    val waterGoalCups: Int = 8,
    val stepGoal: Int = 8000,
    val sleepGoalHours: Float = 8f,
    val migraines: List<MigraineEpisodeEntity> = emptyList(),
    val nutrition: List<NutritionScanEntity> = emptyList(),
    val water: List<WaterDayEntity> = emptyList(),
    val workouts: List<WorkoutEntity> = emptyList(),
    val weights: List<WeightEntryEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val sessions: List<AiSessionEntity> = emptyList(),
) {
    fun validate() {
        require(formatVersion == 1) { "Unsupported backup version." }
        require(listOf(migraines.size, nutrition.size, water.size, workouts.size,
            weights.size, medications.size, sessions.size).sum() <= 50_000)
        require(waterGoalCups in 1..20 && stepGoal in 1000..30000 && sleepGoalHours in 4f..12f)
        require(migraines.all { it.overallIntensity in 1..10 && it.timestampMillis >= 0 &&
            it.markers.all { marker -> marker.x in 0f..1f && marker.y in 0f..1f && marker.intensity in 1..10 } })
        water.forEach { require(it.cups in 0..1000); LocalDate.parse(it.dateIso) }
        require(workouts.all { it.durationMinutes in 1..1440 && it.timestampMillis >= 0 })
        require(weights.all { it.weightKg > 0 && it.weightKg <= 1000 && it.timestampMillis >= 0 })
        profile?.let {
            require(it.heightCm == null || it.heightCm in 20f..300f)
            require(it.weightKg == null || it.weightKg > 0 && it.weightKg <= 1000)
            if (it.dateOfBirthIso.isNotBlank()) require(!LocalDate.parse(it.dateOfBirthIso).isAfter(LocalDate.now()))
        }
    }
}

class BackupRepository(
    context: Context,
    private val db: AppDatabase,
    private val preferences: UserPreferencesRepository,
) {
    private val resolver = context.applicationContext.contentResolver
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun snapshot(): JournalBackup = withContext(Dispatchers.IO) {
        val prefs = preferences.preferences.first()
        val payload = db.withTransaction {
            val dao = db.backupDao()
            JournalBackup(createdAtMillis = System.currentTimeMillis(), appVersion = BuildConfig.VERSION_NAME,
                profile = prefs.bodyProfile, waterGoalCups = prefs.waterGoalCups,
                stepGoal = prefs.dailyStepGoal, sleepGoalHours = prefs.sleepGoalHours,
                migraines = dao.migraines(), nutrition = dao.nutrition().map { it.copy(imageUri = null) },
                water = dao.water(), workouts = dao.workouts(), weights = dao.weights(),
                medications = dao.medications(), sessions = dao.sessions())
        }
        payload.validate()
        payload
    }

    suspend fun export(uri: Uri, password: CharArray) = withContext(Dispatchers.IO) {
        val payload = snapshot()
        val plain = json.encodeToString(payload).toByteArray(Charsets.UTF_8)
        val bytes = try { BackupCipher.encrypt(plain, password) } finally { plain.fill(0); password.fill('\u0000') }
        resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) } ?: error("Could not open the selected file.")
    }

    suspend fun restore(uri: Uri, password: CharArray, restoreProfile: Boolean): Int = withContext(Dispatchers.IO) {
        val encrypted = resolver.openInputStream(uri)?.use { input ->
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(result.size() + count <= BackupCipher.MAX_BYTES + 52) { "Backup is too large." }
                result.write(buffer, 0, count)
            }
            result.toByteArray()
        } ?: error("Could not read the selected file.")
        val plain = try { BackupCipher.decrypt(encrypted, password) } finally { password.fill('\u0000') }
        val payload = try { json.decodeFromString<JournalBackup>(String(plain, Charsets.UTF_8)) } finally { plain.fill(0) }
        mergeSnapshot(payload, restoreProfile)
    }

    suspend fun mergeSnapshot(payload: JournalBackup, restoreProfile: Boolean): Int = withContext(Dispatchers.IO) {
        payload.validate()
        val restored = db.withTransaction {
            val dao = db.backupDao()
            var count = 0
            count += merge(payload.migraines, dao.migraines(), { it.copy(id = 0) }) { db.migraineDao().insert(it) }
            count += merge(payload.nutrition, dao.nutrition(), { it.copy(id = 0, imageUri = null) }) { db.nutritionDao().insert(it) }
            count += merge(payload.workouts, dao.workouts(), { it.copy(id = 0) }) { db.workoutDao().insert(it) }
            count += merge(payload.weights, dao.weights(), { it.copy(id = 0) }) { db.weightDao().insert(it) }
            count += merge(payload.medications, dao.medications(), { it.copy(id = 0) }) { db.medicationDao().insert(it) }
            count += merge(payload.sessions, dao.sessions(), { it.copy(id = 0) }) { db.aiSessionDao().insert(it) }
            for (day in payload.water) {
                val existing = db.waterHistoryDao().get(day.dateIso)
                if (existing == null || day.cups > existing.cups) {
                    db.waterHistoryDao().upsert(day); count++
                }
            }
            count
        }
        if (restoreProfile) {
            payload.profile?.let { preferences.saveBodyProfile(it) }
            preferences.setWaterGoalCups(payload.waterGoalCups)
            preferences.setDailyStepGoal(payload.stepGoal)
            preferences.setSleepGoalHours(payload.sleepGoalHours)
        }
        restored
    }
}

internal suspend fun <T> merge(
    incoming: List<T>, existing: List<T>, normalize: (T) -> T, insert: suspend (T) -> Unit,
): Int {
    val seen = existing.map(normalize).toMutableSet()
    var count = 0
    for (row in incoming) {
        val normalized = normalize(row)
        if (seen.add(normalized)) { insert(normalized); count++ }
    }
    return count
}
