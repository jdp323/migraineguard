package com.migraineguard.app.core.domain

import kotlinx.serialization.Serializable

/** Optional, canonical metric values. Empty details keep all older workouts compatible. */
@Serializable
data class WorkoutDetails(
    val speedKmh: Float? = null,
    val inclinePercent: Float? = null,
    val distanceKm: Float? = null,
    val holdSeconds: Int? = null,
    val rounds: Int? = null,
) {
    fun validate() {
        require(speedKmh == null || speedKmh.isFinite() && speedKmh > 0f && speedKmh <= 150f) { "Speed must be greater than 0 and at most 150 km/h." }
        require(inclinePercent == null || inclinePercent.isFinite() && inclinePercent in -30f..50f) { "Incline must be between -30% and 50%." }
        require(distanceKm == null || distanceKm.isFinite() && distanceKm > 0f && distanceKm <= 1000f) { "Distance must be greater than 0 and at most 1000 km." }
        require(holdSeconds == null || holdSeconds in 1..3600) { "Hold must be between 1 and 3600 seconds." }
        require(rounds == null || rounds in 1..1000) { "Rounds must be between 1 and 1000." }
    }
}

val WorkoutType.isMobility: Boolean get() = this == WorkoutType.STRETCHING || this == WorkoutType.YOGA
val WorkoutType.isCardio: Boolean get() = this in setOf(WorkoutType.TREADMILL, WorkoutType.WALK, WorkoutType.RUN, WorkoutType.CYCLING, WorkoutType.ROWING, WorkoutType.ELLIPTICAL)
