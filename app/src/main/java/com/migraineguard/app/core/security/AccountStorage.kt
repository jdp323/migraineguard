package com.migraineguard.app.core.security

import java.security.MessageDigest
import java.util.Locale

/** Separate local and Google stores; file names never contain an email address. */
object AccountStorage {
    const val LOCAL = "local"
    const val SIGNED_OUT = "signed-out"
    fun google(id: String): String {
        require(id.isNotBlank())
        val canonical = id.trim().lowercase(Locale.ROOT)
        return "google-" + MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
    fun settingsName(account: String): String = if (account == LOCAL) "user_settings" else "settings_$account"
    fun databaseName(account: String): String = if (account == LOCAL) "migraineguard.db" else "migraineguard_$account.db"
}
