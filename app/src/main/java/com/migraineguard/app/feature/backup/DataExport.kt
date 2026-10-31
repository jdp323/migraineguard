package com.migraineguard.app.feature.backup

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Human-readable archive: app-owned records/settings + normalized body photos, no tokens/keys. */
suspend fun BackupRepository.exportReadable(uri: Uri) = withContext(Dispatchers.IO) {
    val payload = snapshot(includeBodyPhotos = true, includeSettings = true)
    val json = Json { encodeDefaults = false; prettyPrint = false }
    exportOutput(uri).use { stream ->
        ZipOutputStream(stream).use { zip ->
            zip.setLevel(java.util.zip.Deflater.BEST_COMPRESSION)
            fun file(name: String, bytes: ByteArray) { zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry() }
            fun text(name: String, value: String) = file(name, value.toByteArray(Charsets.UTF_8))
            fun csv(name: String, header: List<String>, rows: List<List<Any?>>) = text(name, (listOf(header) + rows).joinToString("\r\n", postfix = "\r\n") { row -> row.joinToString(",") { ExportPrivacy.csvCell(it) } })
            text("README.txt", "MigraineGuard personal export\nThis ZIP is NOT encrypted. It contains private health/profile records and selected body photos. Keep it in a trusted location.\nAPI keys, login tokens, raw Health Connect data, meal photo files and equipment photo files are excluded. Original image EXIF is not retained in the normalized body photos.\nThe compact JSON has structured records/settings, dated body measurements (centimetres) and photo IDs; JPEG files are under body-photos/. CSV files are for reading/analysis. This ZIP is not imported by the app; use the password-encrypted .mgbak export for recovery.\nNo data is sent to a developer server. The Android file picker controls the destination, which may be a cloud document provider you choose.\n")
            text("journal-and-settings.json", json.encodeToString(payload.copy(bodyProgress = payload.bodyProgress.map { it.copy(jpegBase64 = "") })))
            csv("migraine.csv", listOf("timestampMillis", "intensity", "notes", "symptoms", "triggers", "painMarkers"), payload.migraines.map { listOf(it.timestampMillis,it.overallIntensity,it.notes,it.symptoms.joinToString(),it.triggers.joinToString(),json.encodeToString(it.markers)) })
            csv("food.csv", listOf("timestampMillis","food","caloriesEstimate","proteinG","carbsG","fatG","notes"), payload.nutrition.map { listOf(it.timestampMillis,it.foodLabel,it.caloriesEstimate,it.proteinG,it.carbsG,it.fatG,it.notes) })
            csv("water.csv", listOf("date","glasses250ml"), payload.water.map { listOf(it.dateIso,it.cups) })
            csv("workouts.csv", listOf("timestampMillis","type","exercise","durationMinutes","calories","setsJson","detailsJson","notes"), payload.workouts.map { listOf(it.timestampMillis,it.typeName,it.exerciseName,it.durationMinutes,it.calories,it.setsJson,it.detailsJson,it.notes) })
            csv("weight.csv", listOf("timestampMillis","weightKg","notes"), payload.weights.map { listOf(it.timestampMillis,it.weightKg,it.notes) })
            csv("medications.csv", listOf("name","dose","time","takenDate","lastTakenMillis"), payload.medications.map { listOf(it.name,it.dose,it.timeLabel,it.takenDateIso,it.lastTakenMillis) })
            csv("ai-history.csv", listOf("timestampMillis","title","persona","prompt","response"), payload.sessions.map { listOf(it.timestampMillis,it.title,it.personaId,it.userPrompt,it.fullResponse) })
            csv("body-progress.csv", listOf("id","date","pose","weightKg","waistCm","scaleBodyFatPercent","notes","photoFile","deleted"), payload.bodyProgress.map { listOf(it.id,it.dateIso,it.pose,it.weightKg,it.waistCm,it.bodyFatPercent,it.notes,if(it.jpegBase64.isEmpty()) "" else "body-photos/"+it.id+".jpg",it.deleted) })
            csv("body-measurements.csv", listOf("id","date","area","centimetres","notes","deleted"), payload.bodyMeasurements.flatMap { entry ->
                if (entry.measurementsCm.isEmpty()) listOf(listOf(entry.id,entry.dateIso,"","",entry.notes,entry.deleted))
                else com.migraineguard.app.feature.body.BodyMeasurements.orderedKeys(entry.measurementsCm.keys).map { key ->
                    listOf(entry.id,entry.dateIso,com.migraineguard.app.feature.body.BodyMeasurements.label(key),entry.measurementsCm.getValue(key),entry.notes,entry.deleted)
                }
            })
            payload.bodyProgress.filter { !it.deleted && it.jpegBase64.isNotEmpty() }.forEach { file("body-photos/"+it.id+".jpg",Base64.getDecoder().decode(it.jpegBase64)) }
        }
    }
}
