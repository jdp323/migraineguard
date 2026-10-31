package com.migraineguard.app.core.storage

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class CompactJsonTest {
    @Test fun repetitiveUnicodeJsonRoundTripsAndUsesLessSpace() {
        val plain = ("{\"notes\":\"" + "Measurement café 💧 ".repeat(2000) + "\"}").toByteArray()
        val packed = CompactJson.pack(plain)
        assertTrue(CompactJson.isCompressed(packed)); assertTrue(packed.size < plain.size / 4)
        assertArrayEquals(plain, CompactJson.unpack(packed))
    }
    @Test fun legacyAndTinyJsonRemainReadableWithoutCompressionBloat() {
        val tiny = "{}".toByteArray()
        assertArrayEquals(tiny, CompactJson.pack(tiny))
        assertArrayEquals(tiny, CompactJson.unpack(tiny))
        val legacy = "{\"formatVersion\":3,\"notes\":\"old export\"}".toByteArray()
        assertArrayEquals(legacy, CompactJson.unpack(legacy))
    }
    @Test fun rawAndExpandedBoundsRejectOversizedData() {
        val plain = "{\"x\":\"".toByteArray() + ByteArray(8192) { 'x'.code.toByte() } + "\"}".toByteArray()
        try { CompactJson.pack(plain, 1024); fail("Oversized raw data must fail") } catch (_: IllegalArgumentException) { }
        val packed = CompactJson.pack(plain)
        try { CompactJson.unpack(packed, 1024); fail("Expansion limit must fail") } catch (_: IllegalArgumentException) { }
        try { CompactJson.unpack(byteArrayOf()); fail("Empty data must fail") } catch (_: IllegalArgumentException) { }
    }
    @Test fun corruptAndTruncatedGzipCannotRestoreAsValidJson() {
        val packed = CompactJson.pack(("{\"x\":\"" + "hello".repeat(400) + "\"}").toByteArray())
        val corrupt = packed.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 0x80).toByte() }
        for (data in listOf(corrupt, packed.copyOf(8))) {
            try { CompactJson.unpack(data); fail("Corrupt gzip must fail") } catch (_: IOException) { }
        }
    }
}
