package com.migraineguard.app.feature.insights

import com.migraineguard.app.core.domain.InsightReport
import java.time.LocalDate

interface InsightsAnalyzer {
    suspend fun weeklyTrends(): InsightReport
    suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport
}

class FakeInsightsAnalyzer : InsightsAnalyzer {
    override suspend fun weeklyTrends(): InsightReport = InsightReport(
        title = "Weekly trends",
        summary = "Fake: migraines clustered on low-pressure days; water intake below goal 3/7 days.",
        periodLabel = "Last 7 days",
    )

    override suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport =
        InsightReport(
            title = "Custom range",
            summary = "Fake report for $start → $end. Plug FallbackAiClient for narrative summaries.",
            periodLabel = "$start to $end",
        )
}
