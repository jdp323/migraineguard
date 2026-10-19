package com.migraineguard.app.core.security

import org.junit.Assert.*
import org.junit.Test

class AccountStorageTest {
    @Test fun accountPathsAreStablePrivateAndSeparated() {
        val a = AccountStorage.google(" Test@example.invalid ")
        assertEquals(a, AccountStorage.google("test@EXAMPLE.invalid"))
        assertNotEquals(a, AccountStorage.google("other@example.invalid"))
        assertFalse(a.contains("test"))
        assertFalse(a.contains("@"))
        assertNotEquals(AccountStorage.databaseName(a), AccountStorage.databaseName(AccountStorage.LOCAL))
        assertEquals("migraineguard.db", AccountStorage.databaseName(AccountStorage.LOCAL))
        assertEquals("user_settings", AccountStorage.settingsName(AccountStorage.LOCAL))
        assertNotEquals(AccountStorage.settingsName(a), AccountStorage.settingsName(AccountStorage.SIGNED_OUT))
    }
}
