package com.migraineguard.app.feature.insights

import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.domain.HeadView
import com.migraineguard.app.core.domain.InsightReport
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.fitness.WorkoutRepository
import com.migraineguard.app.feature.migraine.MigraineRepository
import com.migraineguard.app.feature.nutrition.NutritionAnalyzer
import com.migraineguard.app.feature.water.WaterIntakeRepository
import com.migraineguard.app.feature.weather.WeatherRepository
import java.time.LocalDate
import java.time.ZoneId

interface InsightsAnalyzer {
    suspend fun weeklyTrends(): InsightReport
    suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport
    suspend fun aiNarrative(report: InsightReport): String
}

class DefaultInsightsAnalyzer(
    private val migraineRepository: MigraineRepository,
    private val waterIntakeRepository: WaterIntakeRepository,
    private val weatherRepository: WeatherRepository,
    private val healthDataRepository: HealthDataRepository,
    private val workoutRepository: WorkoutRepository,
    private val nutritionAnalyzer: NutritionAnalyzer,
    private val aiClient: FallbackAiClient,
) : InsightsAnalyzer {

    override suspend fun weeklyTrends(): InsightReport {
        val end = LocalDate.now()
        val start = end.minusDays(6)
        return customRange(start, end).copy(title = "Weekly trends")
    }

    override suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport {
        val zone = ZoneId.systemDefault()
        val startMs = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val episodes = migraineRepository.inRange(startMs, endMs)
        val avgIntensity = if (episodes.isEmpty()) 0f else episodes.map { it.overallIntensity }.average().toFloat()
        val avgWater = waterIntakeRepository.averageCups(start, end)
        val sleep = healthDataRepository.latestSleep()
        val fitness = healthDataRepository.latestFitness()
        val workouts = workoutRepository.inRange(startMs, endMs)
        val foods = nutritionAnalyzer.inRange(startMs, endMs)
        val weather = weatherRepository.currentWeather()
        val pressureNote = if (weather == null || weather.isCachedOrFake) {
            "Current weather unavailable or stale. No pressure history collected."
        } else {
            "Current pressure ${weather.pressureHpa} hPa. No pressure history or personal correlation calculated."
        }
        val sideCounts = episodes.flatMap { it.markers }.groupingBy {
            runCatching { HeadView.valueOf(it.viewId).label }.getOrDefault(it.viewId)
        }.eachCount()
        val sideSummary = if (sideCounts.isEmpty()) {
            "No head-map points yet"
        } else {
            sideCounts.entries.sortedByDescending { it.value }
                .joinToString { "${it.key}: ${it.value}" }
        }
        val topFoods = foods.groupingBy { it.result.foodLabel }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { "${it.key} (${it.value})" }
        val summary = buildString {
            append("Migraines logged: ${episodes.size}. ")
            if (episodes.isNotEmpty()) append("Avg intensity ${"%.1f".format(avgIntensity)}/10. ")
            append("Avg water ${"%.1f".format(avgWater)} cups per recorded day. ")
            append("Recent sleep ${sleep.hours}h (${sleep.qualityLabel}). ")
            append("${workouts.size} workouts · ${workouts.sumOf { it.durationMinutes }} min. ")
            append("Steps today ${fitness.steps}. ")
            append("Sleep and steps are current snapshots, not averages or totals for this period. ")
            append(pressureNote)
            append(".")
        }
        return InsightReport(
            title = "Custom range",
            summary = summary,
            periodLabel = "$start → $end",
            migraineCount = episodes.size,
            avgIntensity = avgIntensity,
            avgWaterCups = avgWater,
            avgSleepHours = sleep.hours,
            pressureTrend = pressureNote,
            foodScanCount = foods.size,
            topFoods = topFoods,
            totalSteps = fitness.steps,
            workoutCount = workouts.size,
            workoutMinutes = workouts.sumOf { it.durationMinutes },
            sideSummary = sideSummary,
        )
    }

    override suspend fun aiNarrative(report: InsightReport): String {
        val blob = """
            Period: ${report.periodLabel}
            Migraines: ${report.migraineCount}, avg intensity ${report.avgIntensity}
            Head sides: ${report.sideSummary}
            Water average over recorded days, cups: ${report.avgWaterCups}
            Latest sleep session snapshot, hours (not period average): ${report.avgSleepHours}
            Food scans: ${report.foodScanCount}, top: ${report.topFoods.joinToString()}
            Workouts: ${report.workoutCount} (${report.workoutMinutes} min), steps today: ${report.totalSteps}
            Current weather status (not period trend): ${report.pressureTrend}
            Stats summary: ${report.summary}
        """.trimIndent()
        return aiClient.summarizeInsights(blob)
    }
}
