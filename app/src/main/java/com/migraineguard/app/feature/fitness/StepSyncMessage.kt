package com.migraineguard.app.feature.fitness

/** Source totals are diagnostic comparisons, never added to the deduplicated total. */
fun stepSyncMessage(total: Int?, samsung: Int?, samsungReadFailed: Boolean = false): String = when {
    total == null -> "Steps permission is granted, but Health Connect returned no steps for today. Allow your source health app to write Steps, sync your device there, then refresh. If the records exist in Health Connect, check its activity source priority."
    samsungReadFailed -> "Samsung's count could not be checked. The displayed total is from Health Connect."
    samsung == null || samsung == 0 ->
        "Showing Health Connect steps from the available sources. No Samsung count is required. Sync your device in its source app if the total is delayed."
    samsung > total ->
        "Samsung has more steps than the combined total. Check Health Connect's activity source priority; put Samsung Health first if you want it to take precedence. Counts are not added together."
    else ->
        "These are today's steps received by Health Connect. Device updates can arrive later; sync in your source health app, then refresh here."
}
