package com.migraineguard.app.feature.fitness

/** Source totals are diagnostic comparisons, never added to the deduplicated total. */
fun stepSyncMessage(total: Int?, samsung: Int?, samsungReadFailed: Boolean = false): String = when {
    total == null -> "Steps could not be read. Check MigraineGuard's Steps permission and refresh."
    samsungReadFailed -> "Samsung's count could not be checked. The displayed total is from Health Connect."
    samsung == null || samsung == 0 ->
        "No Samsung steps received for today. Allow Samsung Health to write Steps in Health Connect, open Samsung Health and Sync now, then refresh here."
    samsung > total ->
        "Samsung has more steps than the combined total. Check Health Connect's activity source priority; put Samsung Health first if you want it to take precedence. Counts are not added together."
    else ->
        "These are today's steps received by Health Connect. Watch and Samsung Health updates can arrive later; open Samsung Health, Sync now, then refresh here."
}
