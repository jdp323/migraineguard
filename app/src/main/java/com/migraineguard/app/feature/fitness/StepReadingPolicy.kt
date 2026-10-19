package com.migraineguard.app.feature.fitness

import kotlin.math.roundToLong

data class StepInterval(val startMillis: Long, val endMillis: Long, val count: Long, val source: String)

/** One writer at a time. Choose the highest-count set of non-overlapping intervals. */
fun conservativeSteps(records: List<StepInterval>, start: Long, end: Long): Int? {
    val valid = records.filter { it.count >= 0 && it.endMillis > it.startMillis &&
        it.startMillis < end && it.endMillis > start }
    if (valid.isEmpty()) return null
    require(valid.map { it.source }.distinct().size == 1) { "Do not combine raw writers." }
    data class Slice(val start: Long, val end: Long, val count: Double)
    val slices = valid.map {
        val a = maxOf(start, it.startMillis)
        val b = minOf(end, it.endMillis)
        Slice(a, b, it.count.toDouble() * (b - a) / (it.endMillis - it.startMillis))
    }.sortedBy { it.end }
    // Weighted interval scheduling: a parent daily bucket and its hourly children
    // are alternatives, never additive. Partial overlaps can underestimate, safely.
    val best = DoubleArray(slices.size + 1)
    for (i in slices.indices) {
        var lo = 0
        var hi = i
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (slices[mid].end <= slices[i].start) lo = mid + 1 else hi = mid
        }
        best[i + 1] = maxOf(best[i], best[lo] + slices[i].count)
    }
    return best.last().coerceIn(0.0, Int.MAX_VALUE.toDouble()).roundToLong().toInt()
}

data class StepChoice(val total: Int?, val source: String, val estimated: Boolean = false)

fun chooseStepReading(combined: Int?, samsung: Int?, raw: Map<String, Int?>, preferSamsung: Boolean): StepChoice {
    val samsungPackage = HealthConnectPackages.SAMSUNG_HEALTH
    val rawSamsung = raw[samsungPackage]
    return when {
        preferSamsung && (samsung ?: 0) > 0 -> StepChoice(samsung, "Samsung Health")
        !preferSamsung && (combined ?: 0) > 0 -> StepChoice(combined, "Health Connect")
        preferSamsung && (rawSamsung ?: 0) > 0 -> StepChoice(rawSamsung, "Samsung Health · raw-record estimate", true)
        (combined ?: 0) > 0 -> StepChoice(combined, "Health Connect")
        (samsung ?: 0) > 0 -> StepChoice(samsung, "Samsung Health")
        raw.any { (it.value ?: 0) > 0 } -> {
            val best = raw.maxByOrNull { it.value ?: 0 }!!
            StepChoice(best.value, best.key + " · raw-record estimate", true)
        }
        else -> StepChoice(combined ?: samsung, "Health Connect")
    }
}
