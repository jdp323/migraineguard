package com.migraineguard.app.core.security

import org.junit.Assert.*
import org.junit.Test

class BackupCipherTest {
    private val password get() = "test password for backup".toCharArray()

    @Test fun roundTripAndRandomizedExports() {
        val plain = "health journal with unicode →".toByteArray()
        val a = BackupCipher.encrypt(plain, password)
        val b = BackupCipher.encrypt(plain, password)
        assertFalse(a.contentEquals(b))
        assertArrayEquals(plain, BackupCipher.decrypt(a, password))
    }
    @Test fun wrongPasswordAndTamperingAreRejected() {
        val a = BackupCipher.encrypt("records".toByteArray(), password)
        assertThrows(Exception::class.java) { BackupCipher.decrypt(a, "wrong password".toCharArray()) }
        a[a.lastIndex] = (a.last().toInt() xor 1).toByte()
        assertThrows(Exception::class.java) { BackupCipher.decrypt(a, password) }
    }
    @Test fun invalidFilesAndShortPasswordsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { BackupCipher.decrypt(ByteArray(12), password) }
        assertThrows(IllegalArgumentException::class.java) { BackupCipher.encrypt(ByteArray(1), "short".toCharArray()) }
    }
}
