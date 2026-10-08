package com.migraineguard.app.core.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Portable authenticated encryption; each export has a fresh salt and nonce. */
object BackupCipher {
    const val MAX_BYTES = 20 * 1024 * 1024
    private val magic = "MGBACK01".toByteArray(Charsets.US_ASCII)
    private const val ITERATIONS = 600_000
    private val random = SecureRandom()

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        return try {
            val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(encoded, "AES").also { encoded.fill(0) }
        } finally { spec.clearPassword() }
    }

    fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        require(password.size >= 8) { "Use a password with at least 8 characters." }
        require(plain.size <= MAX_BYTES) { "Backup is too large." }
        val salt = ByteArray(16).also(random::nextBytes)
        val iv = ByteArray(12).also(random::nextBytes)
        val header = magic + salt + iv
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, derive(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(header)
        return header + cipher.doFinal(plain)
    }

    fun decrypt(encrypted: ByteArray, password: CharArray): ByteArray {
        require(password.size >= 8) { "Enter the backup password." }
        require(encrypted.size in 52..(MAX_BYTES + 52)) { "Invalid or oversized backup." }
        require(encrypted.copyOfRange(0, 8).contentEquals(magic)) { "Unsupported backup format." }
        val header = encrypted.copyOfRange(0, 36)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, derive(password, encrypted.copyOfRange(8, 24)),
            GCMParameterSpec(128, encrypted.copyOfRange(24, 36)))
        cipher.updateAAD(header)
        return cipher.doFinal(encrypted.copyOfRange(36, encrypted.size))
    }
}
