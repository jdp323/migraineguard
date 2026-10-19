package com.migraineguard.app.core.time

import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/** Re-evaluates local date after midnight, timezone changes and wall-clock adjustments. */
fun localDates(
    today: () -> LocalDate = { LocalDate.now() },
    pollIntervalMillis: Long = 30_000L,
): Flow<LocalDate> {
    require(pollIntervalMillis > 0)
    return flow {
        while (true) {
            emit(today())
            delay(pollIntervalMillis)
        }
    }.distinctUntilChanged()
}
