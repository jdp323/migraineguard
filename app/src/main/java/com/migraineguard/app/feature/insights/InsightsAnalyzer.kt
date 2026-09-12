package com.migraineguard.app.feature.insights

import com.migraineguard.app.core.ai.FallbackAiClient
import com.migraineguard.app.core.domain.InsightReport
import com.migraineguard.app.feature.fitness.HealthDataRepository
import com.migraineguard.app.feature.migraine.MigraineRepository
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
        val weather = weatherRepository.currentWeather()
        val pressureNote = when {
            weather.pressureHpa < 1005 -> "Low pressure now — historically linked to migraine risk"
            weather.pressureHpa > 1020 -> "High pressure — often calmer for many people"
            else -> "Pressure near average"
        }
        val summary = buildString {
            append("Migraines logged: ${episodes.size}. ")
            if (episodes.isNotEmpty()) append("Avg intensity ${"%.1f".format(avgIntensity)}/10. ")
            append("Avg water ${"%.1f".format(avgWater)} cups/day. ")
            append("Recent sleep ${sleep.hours}h (${sleep.qualityLabel}). ")
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
        )
    }

    override suspend fun aiNarrative(report: InsightReport): String {
        val blob = """
            Period: ${report.periodLabel}
            Migraines: ${report.migraineCount}, avg intensity ${report.avgIntensity}
            Water avg cups: ${report.avgWaterCups}
            Sleep hours: ${report.avgSleepHours}
            Pressure: ${report.pressureTrend}
            Stats summary: ${report.summary}
        """.trimIndent()
        return aiClient.summarizeInsights(blob)
    }
}
