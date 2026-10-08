package com.migraineguard.app.core.time

import java.time.Instant
import java.time.ZoneId

/** Calendar-day boundary, including daylight-saving changes. */
fun healthDayStart(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant =
    now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()

/** Exclusive next local midnight; daily cumulative records must not be clipped at now. */
fun healthDayEnd(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant =
    now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
