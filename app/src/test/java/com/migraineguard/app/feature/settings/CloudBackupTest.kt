package com.migraineguard.app.feature.settings

import com.migraineguard.app.core.data.prefs.UserPreferences
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.backup.JournalBackup
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import okio.ByteString.Companion.toByteString
import kotlinx.serialization.encodeToString

class CloudBackupTest {
    private val owner = AccountStorage.google("fixture@example.invalid")
    private fun backup() = CloudAccountBackup(owner = owner, savedAtMillis = 100,
        settings = UserPreferences(modifiedAtMillis = 50, geminiApiKey = "fixture-personal-key",
            bodyProfile = BodyProfile(displayName = "Fixture", dateOfBirthIso = "1990-01-01",
                heightCm = 180f, weightKg = 70f, profileComplete = true), setupComplete = true),
        journal = JournalBackup(createdAtMillis = 100, appVersion = "fixture"))

    @Test fun portableBackupRoundTripsProfileAndKeysButNotPhoneConsent() {
        val data = backup().copy(settings = CloudBackupFormat.portable(backup().settings.copy(deviceHealthEnabled = true)))
        val restored = CloudBackupFormat.decode(CloudBackupFormat.encode(data), owner)
        assertEquals("fixture-personal-key", restored.settings.geminiApiKey)
        assertEquals("1990-01-01", restored.settings.bodyProfile.dateOfBirthIso)
        assertEquals(70f, restored.settings.bodyProfile.weightKg!!, 0f)
        assertFalse(restored.settings.deviceHealthEnabled)
        assertTrue(restored.settings.setupComplete)
    }
    @Test(expected = IllegalArgumentException::class) fun anotherAccountsBackupIsRejected() {
        CloudBackupFormat.decode(CloudBackupFormat.encode(backup()), AccountStorage.google("other@example.invalid"))
    }
    @Test fun unchangedBackupDoesNotBecomeDirtyBecauseOfExportTimes() {
        assertEquals(CloudBackupFormat.fingerprint(backup()),
            CloudBackupFormat.fingerprint(backup().copy(savedAtMillis = 500,
                journal = backup().journal.copy(createdAtMillis = 500))))
    }
    @Test fun driveTransportUsesAppDataAndConditionalUpdate() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            val client = DriveRestClient(server.url("/").toString().trimEnd('/'))
            server.enqueue(MockResponse().setBody("""{"email":"fixture@example.invalid","email_verified":true}"""))
            client.verifyAccount("fixture-token", "fixture@example.invalid")
            assertEquals("Bearer fixture-token", server.takeRequest().getHeader("Authorization"))
            val packedData = CloudBackupFormat.encode(backup())
            server.enqueue(MockResponse().setBody("{\"files\":[{\"id\":\"fixture-file\",\"size\":\"" + packedData.size + "\"}]}"))
            server.enqueue(MockResponse().addHeader("ETag", "fixture-etag").setBody(okio.Buffer().write(CloudBackupFormat.encode(backup()))))
            val remote = client.download("fixture-token")!!
            assertTrue(server.takeRequest().path!!.contains("spaces=appDataFolder"))
            assertEquals("/drive/v3/files/fixture-file?alt=media", server.takeRequest().path)
            assertEquals(packedData.size.toLong(), remote.storedBytes)
            server.enqueue(MockResponse().setBody("""{"id":"fixture-file"}"""))
            client.upload("fixture-token", CloudBackupFormat.encode(backup()), remote)
            val upload = server.takeRequest()
            assertEquals("PATCH", upload.method)
            assertEquals("fixture-etag", upload.getHeader("If-Match"))
            assertEquals("application/gzip",upload.getHeader("Content-Type"))
            assertEquals("fixture-personal-key",CloudBackupFormat.decode(remote.bytes,owner).settings.geminiApiKey)
        }
    }
    @Test fun mismatchedAuthorizedEmailCannotAccessBackup() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            val client = DriveRestClient(server.url("/").toString().trimEnd('/'))
            server.enqueue(MockResponse().setBody("""{"email":"other@example.invalid","email_verified":true}"""))
            try { client.verifyAccount("fixture-token", "fixture@example.invalid"); fail("Must reject wrong account") }
            catch (_: IllegalArgumentException) { }
            assertEquals(1, server.requestCount)
        }
    }
    @Test fun firstBackupCreatesPrivateAppDataFile() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            val client = DriveRestClient(server.url("/").toString().trimEnd('/'))
            server.enqueue(MockResponse().setBody("""{"id":"new-fixture-file"}"""))
            assertEquals("new-fixture-file", client.upload("fixture-token", CloudBackupFormat.encode(backup()), null))
            val upload = server.takeRequest()
            assertEquals("POST", upload.method)
            assertTrue(upload.path!!.contains("uploadType=multipart"))
            assertTrue(upload.getHeader("Content-Type")!!.startsWith("multipart/related"))
            val captured = upload.body.clone()
            assertTrue(captured.indexOf(CloudBackupFormat.encode(backup()).toByteString()) >= 0)
            val body = captured.readString(Charsets.ISO_8859_1)
            assertTrue(body.contains("appDataFolder"))
            assertTrue(body.contains(CloudBackupFormat.FILE_NAME))
            assertTrue(body.contains("application/gzip"))
        }
    }
    @Test fun legacyPlainJsonAndNewCompressedBackupsUseTheSameRestorePath() {
        val legacy = backup().copy(formatVersion=4,journal=backup().journal.copy(formatVersion=3))
        val oldJson = org.json.JSONObject(kotlinx.serialization.json.Json { encodeDefaults=true }.encodeToString(legacy))
        oldJson.getJSONObject("journal").remove("bodyMeasurements")
        val raw = oldJson.toString().toByteArray()
        assertEquals(legacy,CloudBackupFormat.decode(raw,owner))
        val packed = CloudBackupFormat.encode(backup())
        assertTrue(com.migraineguard.app.core.storage.CompactJson.isCompressed(packed))
        assertTrue(packed.size < CloudBackupFormat.json.encodeToString(backup()).toByteArray().size)
    }
    @Test fun uploadFailureDoesNotClaimSuccess() = runBlocking {
        MockWebServer().use { server ->
            server.start()
            val client = DriveRestClient(server.url("/").toString().trimEnd('/'))
            server.enqueue(MockResponse().setResponseCode(403))
            try { client.upload("fixture-token", CloudBackupFormat.encode(backup()), null); fail("Must report denial") }
            catch (error: IllegalStateException) { assertTrue(error.message!!.contains("denied")) }
        }
    }
}
