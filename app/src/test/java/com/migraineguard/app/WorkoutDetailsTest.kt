package com.migraineguard.app

import com.migraineguard.app.core.domain.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class WorkoutDetailsTest {
    @Test fun oldEmptyDetailsAndCardioRoundTrip() {
        assertEquals(WorkoutDetails(),Json.decodeFromString<WorkoutDetails>("{}"))
        val value=WorkoutDetails(speedKmh=6.5f,inclinePercent=3f,distanceKm=2f)
        value.validate()
        assertEquals(value,Json.decodeFromString<WorkoutDetails>(Json.encodeToString(WorkoutDetails.serializer(),value)))
    }
    @Test fun rejectsInvalidCardioAndHolds() {
        for(value in listOf(WorkoutDetails(speedKmh=Float.NaN),WorkoutDetails(speedKmh=-1f),WorkoutDetails(inclinePercent=51f),WorkoutDetails(distanceKm=Float.POSITIVE_INFINITY),WorkoutDetails(holdSeconds=0),WorkoutDetails(rounds=-1))) {
            assertTrue(runCatching{value.validate()}.isFailure)
        }
        WorkoutDetails(inclinePercent=-5f,holdSeconds=30,rounds=3).validate()
    }
    @Test fun exerciseFamiliesDoNotUseStrengthFields() {
        assertTrue(WorkoutType.TREADMILL.isCardio)
        assertTrue(WorkoutType.STRETCHING.isMobility)
        assertFalse(WorkoutType.STRETCHING.isCardio)
        assertFalse(WorkoutType.STRENGTH.isMobility)
        assertFalse(WorkoutType.STRENGTH.isCardio)
    }
}
