package com.migraineguard.app.feature.profile

import kotlin.math.roundToInt

fun imperialHeight(cm: Float): Pair<Int, Int> {
    val inches = (cm / 2.54f).roundToInt()
    return inches / 12 to inches % 12
}

fun convertedWeight(value: Float, fromMetric: Boolean, toMetric: Boolean): Float = when {
    fromMetric == toMetric -> value
    toMetric -> value / 2.2046226f
    else -> value * 2.2046226f
}
