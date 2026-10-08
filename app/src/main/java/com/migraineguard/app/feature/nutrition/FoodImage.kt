package com.migraineguard.app.feature.nutrition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64

data class FoodImage(val base64: String, val mimeType: String = "image/jpeg")

/** Re-encoding strips EXIF/location metadata; bounded decoding avoids loading full-size watch/phone photos. */
suspend fun prepareFoodImage(context: Context, uri: Uri): FoodImage = withContext(Dispatchers.IO) {
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            require(out.size() + read <= 20 * 1024 * 1024) { "Choose a photo smaller than 20 MB." }
            out.write(buffer, 0, read)
        }
        out.toByteArray()
    } ?: error("Photo could not be opened.")
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unsupported photo format." }
    var sample = 1
    while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sample }) ?: error("Photo could not be decoded.")
    val encoded = try {
        ByteArrayOutputStream().use { output ->
            require(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output))
            output.toByteArray()
        }
    } finally { bitmap.recycle(); bytes.fill(0) }
    require(encoded.size <= 4 * 1024 * 1024) { "Photo is too large to analyze." }
    FoodImage(Base64.getEncoder().encodeToString(encoded)).also { encoded.fill(0) }
}
