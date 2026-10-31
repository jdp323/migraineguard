package com.migraineguard.app.feature.body

import com.migraineguard.app.core.data.db.BodyMeasurementEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class BodyMeasurementTest {
    private fun row(date: String = "2026-10-01", values: Map<String,Float> = mapOf("waist" to 80f), time: Long = 1) =
        BodyMeasurementEntity("fixture-" + time, date, values, modifiedAtMillis = time)
    @Test fun metricImperialAndCommaInputsRoundTripWithCanonicalPrecision() {
        assertEquals(80.01f, BodyMeasurements.parse("31.5", false), .001f)
        assertEquals(80.01f, BodyMeasurements.parse("31,5", false), .001f)
        assertEquals("31.5", BodyMeasurements.draft(80.01f, false))
        assertEquals(101.25f, BodyMeasurements.parse("101.25", true), .001f)
        for (bad in listOf("0","-2","NaN","Infinity","501")) {
            try { BodyMeasurements.parse(bad,true); fail("Invalid measurement accepted: " + bad) } catch (_: Exception) { }
        }
    }
    @Test fun everyStandardAreaAndCustomNameHasValidatedStorage() {
        assertEquals(20, MeasurementArea.entries.size)
        row(values = MeasurementArea.entries.associate { it.id to 50f }).validate()
        val custom = BodyMeasurements.customId("  Upper   torso ")
        assertEquals("custom:upper torso", custom); row(values = mapOf(custom to 90f)).validate()
        for (values in listOf(mapOf("unknown" to 20f), mapOf("waist" to Float.NaN), mapOf("waist" to 0f))) {
            try { row(values=values).validate(); fail("Invalid map accepted") } catch (_: IllegalArgumentException) { }
        }
        try { BodyMeasurements.customId("../private"); fail("Unsafe label") } catch (_: IllegalArgumentException) { }
    }
    @Test fun trendUsesRecordedDatesAndKeepsDifferentAreasSeparate() {
        val records = listOf(row("2026-10-20",mapOf("waist" to 79f),3), row("2026-10-01",mapOf("waist" to 82f),1),
            row("2026-10-10",mapOf("chest" to 101f),2), row("2026-10-25",mapOf("waist" to 70f),4).copy(deleted=true))
        val points = BodyMeasurements.points(records,"waist")
        assertEquals(listOf("2026-10-01","2026-10-20"), points.map { it.dateIso })
        assertEquals(19, (points.last().epochDay - points.first().epochDay).toInt())
        assertEquals(-3f, points.last().centimetres-points.first().centimetres,0f)
    }
    @Test fun compactMapKeepsOnlyFilledAreasAndDeletionClearsValues() {
        val value = row(values=mapOf("waist" to 80f,"leftBiceps" to 34f))
        val json = Json { encodeDefaults=false }
        val text = json.encodeToString(value)
        assertFalse(text.contains("notes"));assertFalse(text.contains("rightBiceps"))
        assertEquals(value,json.decodeFromString<BodyMeasurementEntity>(text))
        value.copy(measurementsCm=emptyMap(),deleted=true).validate()
        try { value.copy(deleted=true).validate();fail("Deleted values must be removed") } catch (_:IllegalArgumentException) { }
    }
}
