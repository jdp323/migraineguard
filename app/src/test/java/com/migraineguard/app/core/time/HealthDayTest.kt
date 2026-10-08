package com.migraineguard.app.core.time

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthDayTest {
    private val newYork = ZoneId.of("America/New_York")

    @Test fun todayStartsAtLocalMidnightInsteadOfTwentyFourHoursAgo() {
        val now = Instant.parse("2026-10-08T18:00:00Z")
        assertEquals(Instant.parse("2026-10-08T04:00:00Z"), healthDayStart(now, newYork))
    }

    @Test fun daylightSavingDayCanContainTwentyFiveHours() {
        val start = healthDayStart(Instant.parse("2026-11-01T18:00:00Z"), newYork)
        val next = healthDayStart(Instant.parse("2026-11-02T18:00:00Z"), newYork)
        assertEquals(25L, Duration.between(start, next).toHours())
    }
    @Test fun earlyMorningQueryIncludesTheWholeCurrentCalendarDay() {
        val now = Instant.parse("2026-10-08T05:41:00Z")
        assertEquals(Instant.parse("2026-10-09T04:00:00Z"), healthDayEnd(now, newYork))
        assertEquals(24L, Duration.between(healthDayStart(now, newYork), healthDayEnd(now, newYork)).toHours())
    }

    @Test fun springForwardDayEndsAfterTwentyThreeHours() {
        val now = Instant.parse("2026-03-08T18:00:00Z")
        assertEquals(23L, Duration.between(healthDayStart(now, newYork), healthDayEnd(now, newYork)).toHours())
    }

    @Test fun fallBackDayEndsAfterTwentyFiveHours() {
        val now = Instant.parse("2026-11-01T18:00:00Z")
        assertEquals(25L, Duration.between(healthDayStart(now, newYork), healthDayEnd(now, newYork)).toHours())
    }
}
