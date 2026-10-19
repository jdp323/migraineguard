package com.migraineguard.app.feature.fitness

import kotlin.math.roundToLong

data class StepInterval(val startMillis: Long, val endMillis: Long, val count: Long, val source: String)

/** One writer at a time. Overlapping intervals contribute their maximum density, never their sum. */
fun conservativeSteps(records: List<StepInterval>, start: Long, end: Long): Int? {
    val valid = records.filter { it.count >= 0 && it.endMillis > it.startMillis &&
        it.startMillis < end && it.endMillis > start }
    if (valid.isEmpty()) return null
    require(valid.map { it.source }.distinct().size == 1) { "Do not combine raw writers." }
    val points = (valid.flatMap { listOf(maxOf(it.startMillis, start), minOf(it.endMillis, end)) }).distinct().sorted()
    data class Slice(val start: Long, val end: Long, val density: Double)
    val slices = valid.map { Slice(maxOf(start, it.startMillis), minOf(end, it.endMillis),
        it.count.toDouble() / (it.endMillis - it.startMillis)) }.sortedBy { it.start }
    val active = java.util.PriorityQueue<Slice>(compareByDescending { it.density })
    var index = 0
    var count = 0.0
    for ((a, b) in points.zipWithNext()) {
        while (index < slices.size && slices[index].start <= a) active.add(slices[index++])
        while (active.isNotEmpty() && (active.peek()?.end ?: Long.MAX_VALUE) <= a) active.poll()
        count += (b - a) * (active.peek()?.density ?: 0.0)
    }
    return count.coerceIn(0.0, Int.MAX_VALUE.toDouble()).roundToLong().toInt()
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
