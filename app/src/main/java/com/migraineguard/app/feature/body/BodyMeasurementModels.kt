package com.migraineguard.app.feature.body

import com.migraineguard.app.core.data.db.BodyMeasurementEntity
import java.time.LocalDate
import java.util.Locale
import kotlin.math.round

/** Circumferences only. Values are canonical centimetres; unfilled areas occupy no map entry. */
enum class MeasurementArea(val id: String, val label: String, val group: String, val tip: String) {
    NECK("neck", "Neck", "Upper body", "Around the base of your neck, with the tape level."),
    SHOULDERS("shoulders", "Shoulders", "Upper body", "Around both shoulders with your arms relaxed."),
    CHEST("chest", "Chest", "Core", "Around the fullest chest, at a relaxed breath."),
    ABDOMEN("abdomen", "Abdomen / tummy", "Core", "Around the navel; use the same point each time."),
    WAIST("waist", "Waist", "Core", "Around the narrowest waist, without pulling the tape tight."),
    HIPS("hips", "Hips", "Core", "Around the widest hips and glutes."),
    LEFT_ARM("leftArm", "Left upper arm · relaxed", "Upper body", "Around the midpoint of the relaxed upper arm."),
    RIGHT_ARM("rightArm", "Right upper arm · relaxed", "Upper body", "Around the midpoint of the relaxed upper arm."),
    LEFT_BICEPS("leftBiceps", "Left biceps · flexed", "Upper body", "Around the fullest flexed biceps; use the same position."),
    RIGHT_BICEPS("rightBiceps", "Right biceps · flexed", "Upper body", "Around the fullest flexed biceps; use the same position."),
    LEFT_FOREARM("leftForearm", "Left forearm", "Upper body", "Around the fullest forearm."),
    RIGHT_FOREARM("rightForearm", "Right forearm", "Upper body", "Around the fullest forearm."),
    LEFT_WRIST("leftWrist", "Left wrist", "Upper body", "Around the wrist at the same landmark."),
    RIGHT_WRIST("rightWrist", "Right wrist", "Upper body", "Around the wrist at the same landmark."),
    LEFT_THIGH("leftThigh", "Left thigh", "Lower body", "Around the fullest upper thigh; note your landmark."),
    RIGHT_THIGH("rightThigh", "Right thigh", "Lower body", "Around the fullest upper thigh; note your landmark."),
    LEFT_CALF("leftCalf", "Left calf", "Lower body", "Around the fullest calf, standing consistently."),
    RIGHT_CALF("rightCalf", "Right calf", "Lower body", "Around the fullest calf, standing consistently."),
    LEFT_ANKLE("leftAnkle", "Left ankle", "Lower body", "Around the narrowest ankle at the same landmark."),
    RIGHT_ANKLE("rightAnkle", "Right ankle", "Lower body", "Around the narrowest ankle at the same landmark.")
}

object BodyMeasurements {
    val groups = listOf("Core", "Upper body", "Lower body", "Custom")
    fun customId(label: String): String {
        val name = label.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
        require(name.length in 1..40 && name.all { it.isLetterOrDigit() || it in " _-" }) { "Use a custom name of 1–40 letters, numbers or spaces." }
        return "custom:" + name
    }
    fun knownKey(key: String) = MeasurementArea.entries.any { it.id == key } ||
        (key.startsWith("custom:") && runCatching { customId(key.removePrefix("custom:")) == key }.getOrDefault(false))
    fun label(key: String): String = MeasurementArea.entries.find { it.id == key }?.label
        ?: (key.removePrefix("custom:").replaceFirstChar { it.titlecase(Locale.ROOT) } + " · custom")
    fun centimetres(value: Float, metric: Boolean): Float {
        val cm = if (metric) value.toDouble() else value.toDouble() * 2.54
        require(cm.isFinite() && cm > 0 && cm <= 500) { "Measurements must be greater than 0 and no more than 500 cm (196.85 in)." }
        return (round(cm * 100) / 100).toFloat().also { require(it > 0) { "Measurement is too small." } }
    }
    fun displayed(cm: Float, metric: Boolean) = if (metric) cm else cm / 2.54f
    fun parse(text: String, metric: Boolean): Float = centimetres(
        text.trim().replace(',', '.').toFloatOrNull() ?: error("Enter a valid measurement number."), metric)
    fun draft(cm: Float, metric: Boolean) = "%.2f".format(Locale.ROOT, displayed(cm, metric)).trimEnd('0').trimEnd('.')
    fun orderedKeys(keys: Set<String>) = MeasurementArea.entries.map { it.id }.filter { it in keys } + keys.filter { it.startsWith("custom:") }.sorted()
    fun points(entries: List<BodyMeasurementEntity>, key: String) = entries.filter { !it.deleted && key in it.measurementsCm }
        .sortedWith(compareBy<BodyMeasurementEntity> { it.dateIso }.thenBy { it.modifiedAtMillis })
        .map { MeasurementPoint(it.dateIso, it.measurementsCm.getValue(key)) }
}

data class MeasurementPoint(val dateIso: String, val centimetres: Float) {
    val epochDay get() = LocalDate.parse(dateIso).toEpochDay()
}
