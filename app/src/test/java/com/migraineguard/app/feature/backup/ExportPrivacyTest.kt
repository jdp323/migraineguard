package com.migraineguard.app.feature.backup
import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.ai.AiConnection
import com.migraineguard.app.core.data.db.BodyProgressEntity
import org.junit.Assert.*
import org.junit.Test
class ExportPrivacyTest {
    @Test fun exportSettingsKeepPreferencesAndExcludeCredentialsAndConsent() {
        val value = ExportPrivacy.settings(UserPreferences(geminiApiKey="fixture-secret",groqApiKey="fixture-other",openRouterApiKey="fixture-third",aiConnections=mapOf("gemini" to AiConnection("fixture-secret","model")),deviceHealthEnabled=true,driveSyncEnabled=true,bodyPhotoDriveBackup=true,movementReminders=true,movementReminderMinutes=90))
        assertEquals("",value.geminiApiKey); assertEquals("",value.groqApiKey); assertEquals("",value.openRouterApiKey)
        assertTrue(value.aiConnections.isEmpty()); assertFalse(value.deviceHealthEnabled);assertFalse(value.driveSyncEnabled);assertFalse(value.bodyPhotoDriveBackup)
        assertTrue(value.movementReminders);assertEquals(90,value.movementReminderMinutes)
    }
    @Test fun spreadsheetTextCannotBecomeFormulaAndQuotesAreEscaped() {
        assertEquals("\"'=2+2\"", ExportPrivacy.csvCell("=2+2"))
        assertEquals("\"'  @SUM(A1)\"", ExportPrivacy.csvCell("  @SUM(A1)"))
        assertEquals("\"a,\"\"b\"\"\"", ExportPrivacy.csvCell("a,\"b\""))
        assertEquals("\"-2\"", ExportPrivacy.csvCell(-2))
    }
    private fun row() = BodyProgressEntity("fixture-id","2026-10-01","Front",modifiedAtMillis=1)
    @Test fun progressValidationRejectsMalformedMeasurementsAndUnsafeIds() {
        row().copy(weightKg=70f,waistCm=80f,bodyFatPercent=20f).validate()
        for(entry in listOf(row().copy(weightKg=Float.NaN),row().copy(waistCm=0f),row().copy(id="../escape"),row().copy(jpegBase64="invalid"),row().copy(bodyFatPercent=99f))) {
            try { entry.validate(); fail("Invalid body record accepted") } catch(_: IllegalArgumentException) { }
        }
    }
    @Test fun legacyJournalStillRestoresWhileNewPhotoFieldsRoundTrip() {
        JournalBackup(formatVersion=1,createdAtMillis=1,appVersion="old").validate()
        val source=JournalBackup(createdAtMillis=1,appVersion="new",bodyProgress=listOf(row().copy(notes="local fixture")))
        val json=kotlinx.serialization.json.Json
        val result=json.decodeFromString<JournalBackup>(json.encodeToString(JournalBackup.serializer(),source))
        assertEquals(source,result);result.validate()
    }
}
