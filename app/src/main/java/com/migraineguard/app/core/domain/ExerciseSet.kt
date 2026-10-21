package com.migraineguard.app.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSet(val weightKg: Float = 0f, val reps: Int = 10, val completed: Boolean = true) {
    fun validate() { require(weightKg.isFinite() && weightKg in 0f..2000f && reps in 1..1000) { "Use a valid weight and 1–1000 reps." } }
}
