package com.migraineguard.app.feature.coach

import com.migraineguard.app.core.ai.AiPersona
import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.ai.OfflineHealthBrain
import com.migraineguard.app.core.ai.Personas
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.db.AiSessionDao
import com.migraineguard.app.core.data.db.AiSessionEntity
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.AiSession
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.WorkoutRepository
import com.migraineguard.app.feature.meds.MedicationRepository
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.nutrition.NutritionAnalyzer
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class AiCoachRepository(
    private val aiClient: FallbackAiClient,
    private val sessionDao: AiSessionDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val migraineRepository: MigraineRepository,
    private val waterIntakeRepository: WaterIntakeRepository,
    private val healthDataRepository: HealthDataRepository,
    private val workoutRepository: WorkoutRepository,
    private val nutritionAnalyzer: NutritionAnalyzer,
    private val weatherRepository: WeatherRepository,
    private val medicationRepository: MedicationRepository,
) {
    fun observeSessions(): Flow<List<AiSession>> =
        sessionDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getSession(id: Long): AiSession? = sessionDao.getById(id)?.toDomain()

    suspend fun buildContextPack(): OfflineHealthBrain.ContextPack {
        val prefs = preferencesRepository.preferences.first()
        val zone = ZoneId.systemDefault()
        val end = LocalDate.now()
        val start = end.minusDays(13)
        val startMs = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val waterHistory = waterIntakeRepository.observeHistory(14).first()
        val waterMap = waterHistory.associate { it.dateIso to it.cups }
        val cupsToday = waterIntakeRepository.cupsToday.first()
        val todayIso = end.toString()
        val waterWithToday = waterMap.toMutableMap().apply {
            put(todayIso, cupsToday)
        }
        val meds = medicationRepository.observeMedications().first()
        val energy = if (prefs.energyDateIso == todayIso) prefs.energyLevel else null
        val pack = OfflineHealthBrain.ContextPack(
            prefs = prefs,
            migraines = migraineRepository.inRange(startMs, endMs),
            waterByDay = waterWithToday,
            waterGoal = prefs.waterGoalCups,
            fitness = runCatching { healthDataRepository.latestFitness() }.getOrNull(),
            sleep = runCatching { healthDataRepository.latestSleep() }.getOrNull(),
            weather = runCatching { weatherRepository.currentWeather() }.getOrNull(),
            workouts = workoutRepository.inRange(startMs, endMs),
            foods = nutritionAnalyzer.inRange(startMs, endMs),
            meds = meds,
            energyLevel = energy,
        )
        aiClient.offline()?.latestPack = pack
        return pack
    }

    suspend fun ask(
        persona: AiPersona,
        userMessage: String,
        onChunk: (suspend (String) -> Unit)? = null,
    ): Result<AiSession> {
        val pack = buildContextPack()
        val brain = aiClient.offline()
        val contextJson = brain?.buildContextJson(pack)
            ?: """{"note":"offline brain missing"}"""
        val result = aiClient.completePersona(persona, userMessage, contextJson, onChunk)
        return when (result) {
            is Result.Success -> {
                val full = result.data
                val summary = full.lineSequence().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith(">") }
                    .take(3).joinToString(" ").take(160)
                val title = Personas.defaultTitle(persona, userMessage)
                val id = sessionDao.insert(
                    AiSessionEntity(
                        title = title,
                        personaId = persona.id,
                        summary = summary.ifBlank { title },
                        fullResponse = full,
                        userPrompt = userMessage,
                        timestampMillis = System.currentTimeMillis(),
                    ),
                )
                Result.Success(
                    AiSession(
                        id = id,
                        title = title,
                        personaId = persona.id,
                        summary = summary.ifBlank { title },
                        fullResponse = full,
                        userPrompt = userMessage,
                        timestampMillis = System.currentTimeMillis(),
                    ),
                )
            }
            is Result.Error -> Result.Error(result.message, result.cause)
        }
    }

    suspend fun deleteSession(id: Long) = sessionDao.delete(id)

    private fun AiSessionEntity.toDomain() = AiSession(
        id = id,
        title = title,
        personaId = personaId,
        summary = summary,
        fullResponse = fullResponse,
        userPrompt = userPrompt,
        timestampMillis = timestampMillis,
    )
}
