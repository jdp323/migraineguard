package com.migraineguard.app.core.data.db

import androidx.room.TypeConverter
import com.migraineguard.app.core.domain.MigraineSymptom
import com.migraineguard.app.core.domain.PainMarker
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PainMarkerDto(val id: String, val x: Float, val y: Float, val intensity: Int)

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun symptomsToString(value: List<MigraineSymptom>): String =
        value.joinToString(",") { it.name }

    @TypeConverter
    fun stringToSymptoms(value: String): List<MigraineSymptom> =
        if (value.isBlank()) emptyList()
        else value.split(",").mapNotNull { runCatching { MigraineSymptom.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun markersToString(value: List<PainMarker>): String =
        json.encodeToString(value.map { PainMarkerDto(it.id, it.x, it.y, it.intensity) })

    @TypeConverter
    fun stringToMarkers(value: String): List<PainMarker> =
        if (value.isBlank()) emptyList()
        else json.decodeFromString<List<PainMarkerDto>>(value).map {
            PainMarker(it.id, it.x, it.y, it.intensity)
        }
}
