package com.migraineguard.app.feature.settings

import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.core.security.BackupCipher
import com.migraineguard.app.feature.backup.JournalBackup
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

@Serializable
data class CloudAccountBackup(
    val formatVersion: Int = 2,
    val owner: String,
    val savedAtMillis: Long,
    val settings: UserPreferences,
    val journal: JournalBackup,
) {
    fun validate(account: String) {
        require(formatVersion == 2 && owner == account && account.startsWith("google-")) {
            "This backup belongs to a different account or an unsupported app version."
        }
        require(savedAtMillis >= 0 && settings.modifiedAtMillis >= 0)
        require(listOf(settings.geminiApiKey, settings.groqApiKey, settings.openRouterApiKey).all { it.length <= 4096 })
        journal.copy(profile = settings.bodyProfile, waterGoalCups = settings.waterGoalCups,
            stepGoal = settings.dailyStepGoal, sleepGoalHours = settings.sleepGoalHours).validate()
        journal.validate()
    }
}

object CloudBackupFormat {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    const val FILE_NAME = "migraineguard-account-v2.json"
    const val MAX_BYTES = BackupCipher.MAX_BYTES

    fun encode(value: CloudAccountBackup): ByteArray {
        value.validate(value.owner)
        return json.encodeToString(value).toByteArray(Charsets.UTF_8).also {
            require(it.size <= MAX_BYTES) { "Backup exceeds the 20 MB limit." }
        }
    }
    fun decode(bytes: ByteArray, account: String): CloudAccountBackup {
        require(bytes.size in 1..MAX_BYTES) { "Invalid or oversized Drive backup." }
        return json.decodeFromString<CloudAccountBackup>(String(bytes, Charsets.UTF_8))
            .also { it.validate(account) }
    }
    /** Ignore export timestamps when checking whether actual account data changed. */
    fun fingerprint(value: CloudAccountBackup): String = MessageDigest.getInstance("SHA-256")
        .digest(encode(value.copy(savedAtMillis = 0, journal = value.journal.copy(createdAtMillis = 0))))
        .joinToString("") { "%02x".format(it) }

    fun portable(settings: UserPreferences) = settings.copy(
        deviceHealthEnabled = false, healthConnectSkipped = false, googleWebClientId = "",
        driveSyncEnabled = true,
    )
}
