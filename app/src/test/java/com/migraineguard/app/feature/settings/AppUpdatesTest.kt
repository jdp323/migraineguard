package com.migraineguard.app.feature.settings
import org.junit.Assert.*
import org.junit.Test
class AppUpdatesTest {
    @Test fun semanticVersionsCompareNumerically() { assertTrue(AppUpdates.newer("v1.10.0-debug","1.9.9"));assertFalse(AppUpdates.newer("v1.9.0-debug","1.9.0"));assertTrue(AppUpdates.newer("2.0.0","1.99.99"));assertFalse(AppUpdates.newer("junk","1.0.0")) }
    @Test fun validReleaseStaysOnOwnedDownloadPage(){ val r=AppUpdates.parse("{\"tag_name\":\"v1.9.0-debug\",\"html_url\":\"https://github.com/jdp323/migraineguard/releases/tag/v1.9.0-debug\",\"assets\":[{\"name\":\"app.apk\"}],\"body\":\"New features\"}");assertEquals("v1.9.0-debug",r.version) }
    @Test(expected=IllegalArgumentException::class) fun wrongDownloadHostRejected(){AppUpdates.parse("{\"tag_name\":\"v1.9.0\",\"html_url\":\"https://example.invalid/release\",\"assets\":[]}")}
    @Test(expected=IllegalArgumentException::class) fun draftNotOffered(){AppUpdates.parse("{\"draft\":true}")}
}
