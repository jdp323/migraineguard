package com.migraineguard.app.core.ai

import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.domain.HealthSnapshot
import com.migraineguard.app.core.domain.MedicationEntry
import com.migraineguard.app.core.domain.MigraineEpisode
import com.migraineguard.app.core.domain.NutritionScanRecord
import com.migraineguard.app.core.domain.SleepSnapshot
import com.migraineguard.app.core.domain.WeatherSnapshot
import com.migraineguard.app.core.domain.WorkoutEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Deterministic, high-quality offline analyst/coach. Always available as last fallback.
 */
class OfflineHealthBrain : BaseAiProvider("OfflineHealthBrain", priority = 99) {

    data class ContextPack(
        val prefs: UserPreferences,
        val migraines: List<MigraineEpisode>,
        val waterByDay: Map<String, Int>,
        val waterGoal: Int,
        val fitness: HealthSnapshot?,
        val sleep: SleepSnapshot?,
        val weather: WeatherSnapshot?,
        val workouts: List<WorkoutEntry>,
        val foods: List<NutritionScanRecord>,
        val meds: List<MedicationEntry>,
        val energyLevel: Int?,
    )

    @Volatile
    var latestPack: ContextPack? = null

    override suspend fun complete(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        val pack = latestPack
        val text = if (pack != null) {
            if (looksLikeCoach(prompt)) coachReport(pack, prompt) else analystReport(pack, prompt)
        } else {
            genericOffline(prompt)
        }
        Result.Success(text)
    }

    fun buildContextJson(pack: ContextPack): String {
        val zone = ZoneId.systemDefault()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val migraines = JSONArray()
        pack.migraines.take(14).forEach { e ->
            migraines.put(
                JSONObject()
                    .put("intensity", e.overallIntensity)
                    .put("triggers", e.triggers.joinToString(",") { it.label })
                    .put("symptoms", e.symptoms.joinToString(",") { it.label })
                    .put("day", java.time.Instant.ofEpochMilli(e.timestampMillis).atZone(zone).toLocalDate().format(fmt)),
            )
        }
        val profile = pack.prefs.bodyProfile
        val age = ageYears(profile.dateOfBirthIso)
        return JSONObject()
            .put("profile", JSONObject()
                .put("age", age)
                .put("bmi", profile.bmi)
                .put("sex", profile.sex?.label)
                .put("activity", profile.activityLevel?.label)
                .put("stepGoal", pack.prefs.dailyStepGoal)
                .put("waterGoalCups", pack.waterGoal)
                .put("sleepGoalHours", pack.prefs.sleepGoalHours))
            .put("migrainesRecent", migraines)
            .put("waterAdherence", waterAdherence(pack))
            .put("steps", pack.fitness?.let { if (it.stepSync != null) it.stepSync.total else it.steps })
            .put("stepsGoal", pack.prefs.dailyStepGoal)
            .put("sleepHours", pack.sleep?.hours)
            .put("sleepSource", pack.sleep?.sourceLabel)
            .put("pressureHpa", pack.weather?.pressureHpa)
            .put("weather", pack.weather?.condition)
            .put("workoutCount7d", pack.workouts.size)
            .put("workoutMinutes7d", pack.workouts.sumOf { it.durationMinutes })
            .put("foodScans", pack.foods.take(8).map {
                JSONObject().put("label", it.result.foodLabel).put("hint", it.result.migraineTriggerHint)
            }.let { arr -> JSONArray().also { a -> arr.forEach { a.put(it) } } })
            .put("meds", pack.meds.map { "${it.name} ${it.dose}" }.joinToString("; "))
            .put("energy", pack.energyLevel)
            .toString()
    }

    private fun looksLikeCoach(prompt: String): Boolean {
        val p = prompt.lowercase()
        return p.contains("coach") || p.contains("motivational") || p.contains("habit") ||
            p.contains("exercise of") || p.contains("accountability") ||
            p.contains(Personas.coachSystemPrompt.take(40).lowercase())
    }

    private fun ageYears(dobIso: String): Int? {
        if (dobIso.isBlank()) return null
        return runCatching {
            Period.between(LocalDate.parse(dobIso.take(10)), LocalDate.now()).years
        }.getOrNull()
    }

    private fun waterAdherence(pack: ContextPack): Double {
        if (pack.waterByDay.isEmpty()) return 0.0
        val goal = pack.waterGoal.coerceAtLeast(1)
        val scores = pack.waterByDay.values.map { (it.toDouble() / goal).coerceAtMost(1.0) }
        return scores.average()
    }

    private fun coachReport(pack: ContextPack, prompt: String): String {
        val steps = pack.fitness?.let { fitness ->
            if (fitness.stepSync != null) fitness.stepSync.total else fitness.steps
        }
        val goal = pack.prefs.dailyStepGoal
        val cupsToday = pack.waterByDay[LocalDate.now().toString()] ?: 0
        val sleepH = pack.sleep?.hours ?: 0f
        val workouts = pack.workouts.size
        val stepPct = steps?.let { ((it.toFloat() / goal.coerceAtLeast(1)) * 100).toInt().coerceAtMost(200) }
        val waterPct = ((cupsToday.toFloat() / pack.waterGoal.coerceAtLeast(1)) * 100).toInt()
        val wins = buildList {
            if (stepPct != null && stepPct >= 80) add("Steps are on track ($steps / $goal).")
            if (waterPct >= 70) add("Hydration looks solid ($cupsToday cups).")
            if (sleepH >= pack.prefs.sleepGoalHours - 0.5f && sleepH > 0) add("Sleep near your goal (${"%.1f".format(sleepH)}h).")
            if (workouts > 0) add("You logged $workouts workout(s) recently — nice consistency.")
        }
        val nudges = buildList {
            if (stepPct != null && stepPct < 50) add("A 10–15 min walk would close the gap on steps.")
            if (waterPct < 50) add("Sip water now — dehydration is a common migraine trigger.")
            if (sleepH in 0.1f..5.9f) add("Prioritize an earlier wind-down tonight.")
            if (workouts == 0) add("Try today's Exercise of the Day when you have a free slot.")
            if (pack.migraines.any { it.timestampMillis > System.currentTimeMillis() - 48L * 3600_000 }) {
                add("Recent migraine logged — keep intensity low today and hydrate.")
            }
        }
        return buildString {
            appendLine("### MigraineGuard Coach")
            appendLine()
            appendLine(Personas.SAFETY_DISCLAIMER)
            appendLine()
            appendLine("**Quick pulse**")
            appendLine(if (steps != null) "- Steps: $steps / $goal (${stepPct}%)" else "- Steps: no current reading available")
            appendLine("- Water: $cupsToday / ${pack.waterGoal} cups")
            appendLine("- Sleep: ${if (sleepH > 0) "%.1fh".format(sleepH) else pack.sleep?.sourceLabel ?: "—"}")
            appendLine("- Workouts (7d): $workouts")
            appendLine()
            if (wins.isNotEmpty()) {
                appendLine("**Wins**")
                wins.forEach { appendLine("- $it") }
                appendLine()
            }
            appendLine("**Gentle nudges**")
            (nudges.ifEmpty { listOf("Keep logging — consistency beats perfection.") }).forEach {
                appendLine("- $it")
            }
            appendLine()
            appendLine("**Your ask**")
            appendLine(prompt.lines().lastOrNull { it.isNotBlank() && !it.startsWith("System:") }?.take(280) ?: "Check-in")
            appendLine()
            appendLine("_Offline coach · add a free Gemini key in Settings for richer chat._")
        }
    }

    private fun analystReport(pack: ContextPack, prompt: String): String {
        val migraines = pack.migraines
        val avgIntensity = if (migraines.isEmpty()) 0.0 else migraines.map { it.overallIntensity }.average()
        val triggerCounts = migraines.flatMap { it.triggers }.groupingBy { it.label }.eachCount()
        val topTriggers = triggerCounts.entries.sortedByDescending { it.value }.take(5)
        val pressure = pack.weather?.pressureHpa
        val pressureNote = when {
            pressure == null -> "No weather sample"
            pressure < 1005 -> "Current pressure is relatively low (${pressure} hPa) — some people notice more attacks around falls in barometric pressure."
            pressure > 1020 -> "Current pressure is relatively high (${pressure} hPa)."
            else -> "Pressure near typical indoor range (${pressure} hPa)."
        }
        val foodHints = pack.foods.mapNotNull { it.result.migraineTriggerHint }.distinct().take(5)
        val adherence = (waterAdherence(pack) * 100).toInt()
        val age = ageYears(pack.prefs.bodyProfile.dateOfBirthIso)
        val bmi = pack.prefs.bodyProfile.bmi
        return buildString {
            appendLine("### Health Analyst · educational review")
            appendLine()
            appendLine("> **${Personas.SAFETY_DISCLAIMER}**")
            appendLine()
            appendLine("#### 1. Snapshot")
            appendLine("- Profile: age ${age ?: "—"}, BMI ${bmi?.let { "%.1f".format(it) } ?: "—"} (${pack.prefs.bodyProfile.bmiLabel})")
            appendLine("- Migraines in window: **${migraines.size}** · avg intensity **${"%.1f".format(avgIntensity)}**/10")
            appendLine("- Water adherence ~**$adherence%** of goal")
            appendLine("- Steps today: ${pack.fitness?.steps ?: "—"} / ${pack.prefs.dailyStepGoal}")
            appendLine("- Sleep: ${pack.sleep?.let { if (it.hours > 0) "%.1fh (%s)".format(it.hours, it.qualityLabel) else it.sourceLabel } ?: "—"}")
            appendLine("- Workouts: ${pack.workouts.size} (${pack.workouts.sumOf { it.durationMinutes }} min)")
            appendLine("- Energy check-in: ${pack.energyLevel ?: "—"}")
            appendLine()
            appendLine("#### 2. Trigger patterns (logged)")
            if (topTriggers.isEmpty()) {
                appendLine("- No trigger tags yet — tag stress, sleep, weather, food, dehydration when you log.")
            } else {
                topTriggers.forEach { appendLine("- ${it.key}: ${it.value}×") }
            }
            appendLine()
            appendLine("#### 3. Weather / pressure")
            appendLine("- $pressureNote")
            appendLine()
            appendLine("#### 4. Nutrition signals")
            if (foodHints.isEmpty()) {
                appendLine("- No food-scan trigger hints yet.")
            } else {
                foodHints.forEach { appendLine("- $it") }
            }
            appendLine()
            appendLine("#### 5. Educational differentials (not diagnoses)")
            appendLine("- If sleep is short and migraines cluster afterward → discuss sleep regularity with a clinician.")
            appendLine("- If low water days precede attacks → hydration protocol is a reasonable experiment.")
            appendLine("- If pressure dips coincide with logs → consider a weather alert habit (correlation ≠ causation).")
            appendLine("- Red flags needing urgent care: sudden worst-ever headache, neuro deficits, fever + stiff neck, post-trauma.")
            appendLine()
            appendLine("#### 6. Medications / supplements on file")
            if (pack.meds.isEmpty()) appendLine("- None logged.")
            else pack.meds.forEach { appendLine("- ${it.name} ${it.dose} (${it.timeLabel})") }
            appendLine()
            appendLine("#### Prompt focus")
            appendLine(prompt.lines().lastOrNull { it.isNotBlank() }?.take(320) ?: "Weekly review")
            appendLine()
            appendLine("_Generated by OfflineHealthBrain using your on-device stats. Optional cloud AI (Gemini → Groq → OpenRouter) upgrades narrative quality when keys are set._")
        }
    }

    private fun genericOffline(prompt: String): String =
        buildString {
            appendLine("### OfflineHealthBrain")
            appendLine(Personas.SAFETY_DISCLAIMER)
            appendLine()
            appendLine("No live context pack was attached. Open **Coach** once so stats can load, or add a Gemini key in Settings.")
            appendLine()
            appendLine("Prompt echo: ${prompt.take(400)}")
        }
}
