package com.migraineguard.app.feature.body

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.migraineguard.app.core.data.db.BodyProgressDao
import com.migraineguard.app.core.data.db.BodyProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID

/** Images are resized/re-encoded into the account's private database; no URI or EXIF is retained. */
class BodyProgressRepository(context: Context, private val dao: BodyProgressDao) {
    private val resolver = context.applicationContext.contentResolver
    fun observe() = dao.observe()
    suspend fun add(uri: Uri?, date: String, pose: String, notes: String, kg: Float?, waist: Float?, fat: Float?) = withContext(Dispatchers.IO) {
        val image = uri?.let { normalizeImage(it) }.orEmpty()
        require(image.isNotEmpty() || kg != null || waist != null || fat != null || notes.isNotBlank()) { "Add a photo, measurement or note first." }
        val entry = BodyProgressEntity(UUID.randomUUID().toString(), date, pose, notes.trim(), kg, waist, fat, image, System.currentTimeMillis())
        entry.validate(); dao.put(entry)
    }
    suspend fun remove(entry: BodyProgressEntity) = withContext(Dispatchers.IO) {
        // Keep only an ID/timestamp tombstone so a later merge cannot resurrect a deleted photo.
        dao.put(entry.copy(notes = "", weightKg = null, waistCm = null, bodyFatPercent = null,
            jpegBase64 = "", deleted = true, modifiedAtMillis = maxOf(System.currentTimeMillis(), entry.modifiedAtMillis + 1)))
    }
    private fun normalizeImage(uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                require(info.size.width.toLong() * info.size.height <= 150_000_000L) { "Choose a smaller image." }
                val factor = minOf(1.0, 1280.0 / maxOf(info.size.width, info.size.height))
                decoder.setTargetSize((info.size.width * factor).toInt().coerceAtLeast(1), (info.size.height * factor).toInt().coerceAtLeast(1))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth.toLong() * bounds.outHeight <= 150_000_000L)
            val options = BitmapFactory.Options().apply { inSampleSize = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 1280) inSampleSize *= 2 }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: error("Cannot read this image.")
        }
        try {
            val bytes = ByteArrayOutputStream()
            for (quality in listOf(85, 70, 55, 40)) {
                bytes.reset(); check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytes))
                if (bytes.size() <= 360_000) return Base64.getEncoder().encodeToString(bytes.toByteArray())
            }
            error("This image is too detailed. Choose a smaller or simpler photo.")
        } finally { bitmap.recycle() }
    }
}
