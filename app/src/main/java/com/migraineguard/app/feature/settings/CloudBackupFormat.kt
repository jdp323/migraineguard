package com.migraineguard.app.feature.settings

import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.core.security.BackupCipher
import com.migraineguard.app.core.storage.CompactJson
import com.migraineguard.app.feature.backup.JournalBackup
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

@Serializable
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
data class CloudAccountBackup(
    @kotlinx.serialization.EncodeDefault val formatVersion: Int = 5,
    val owner: String,
    val savedAtMillis: Long,
    val settings: UserPreferences,
    val journal: JournalBackup,
) {
    fun validate(account: String) {
        require(formatVersion in 2..5 && owner == account && account.startsWith("google-")) {
            "This backup belongs to a different account or an unsupported app version."
        }
        settings.trainerProfile.validate(); require(settings.trainingPlans.size <= 5); settings.trainingPlans.forEach { it.validate() }
        require(savedAtMillis >= 0 && settings.modifiedAtMillis >= 0)
        require(listOf(settings.geminiApiKey, settings.groqApiKey, settings.openRouterApiKey).all { it.length <= 4096 })
        require(settings.aiConnections.size <= 20 && settings.aiConnections.all { (id, c) -> com.migraineguard.app.core.ai.AiCatalog.service(id) != null && c.apiKey.length <= 4096 && c.model.length <= 160 })
        require(settings.preferredAiProvider in listOf("auto", "offline") || com.migraineguard.app.core.ai.AiCatalog.service(settings.preferredAiProvider) != null)
        require(settings.weeklyWorkoutGoal in 1..7)
        require(listOf(settings.waterReminderMinutes, settings.foodReminderMinutes, settings.medicationReminderMinutes, settings.migraineReminderMinutes, settings.movementReminderMinutes).all { it in 15..1440 })
        require(settings.reminderStartHour in 0..23 && settings.reminderEndHour in 0..23 && settings.reminderStartHour != settings.reminderEndHour)
        journal.copy(profile = settings.bodyProfile, waterGoalCups = settings.waterGoalCups,
            stepGoal = settings.dailyStepGoal, sleepGoalHours = settings.sleepGoalHours).validate()
        journal.validate()
        require(journal.settings == null) { "Drive settings must have a single owner." }
    }
}

object CloudBackupFormat {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }
    const val FILE_NAME = "migraineguard-account-v2.json"
    const val MAX_BYTES = BackupCipher.MAX_BYTES

    fun encode(value: CloudAccountBackup): ByteArray {
        value.validate(value.owner)
        val plain = json.encodeToString(value).toByteArray(Charsets.UTF_8)
        return try { CompactJson.pack(plain, MAX_BYTES) } finally { plain.fill(0) }
    }
    fun decode(bytes: ByteArray, account: String): CloudAccountBackup {
        require(bytes.size in 1..MAX_BYTES) { "Invalid or oversized Drive backup." }
        val plain = CompactJson.unpack(bytes, MAX_BYTES)
        return try { json.decodeFromString<CloudAccountBackup>(String(plain, Charsets.UTF_8)).also { it.validate(account) } }
        finally { plain.fill(0) }
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
