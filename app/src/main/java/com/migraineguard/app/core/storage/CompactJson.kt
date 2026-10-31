package com.migraineguard.app.core.storage

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/** Compact JSON at rest/in transit; legacy UTF-8 JSON remains readable. Both sides are bounded. */
object CompactJson {
    const val MAX_BYTES = 20 * 1024 * 1024
    fun isCompressed(bytes: ByteArray) = bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()
    fun pack(plain: ByteArray, limit: Int = MAX_BYTES): ByteArray {
        require(plain.size in 1..limit) { "Backup exceeds the supported data limit." }
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(plain) }
        val compressed = output.toByteArray()
        return if (compressed.size < plain.size) compressed else plain.copyOf()
    }
    fun unpack(bytes: ByteArray, limit: Int = MAX_BYTES): ByteArray {
        require(bytes.size in 1..limit) { "Invalid or oversized backup." }
        if (!isCompressed(bytes)) return bytes.copyOf()
        val output = ByteArrayOutputStream()
        GZIPInputStream(ByteArrayInputStream(bytes)).use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size().toLong() + count <= limit) { "Expanded backup exceeds the supported data limit." }
                output.write(buffer, 0, count)
            }
        }
        return output.toByteArray().also { require(it.isNotEmpty()) { "Empty backup." } }
    }
}
