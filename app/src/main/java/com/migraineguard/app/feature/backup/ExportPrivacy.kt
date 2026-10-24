package com.migraineguard.app.feature.backup

import com.migraineguard.app.core.data.prefs.UserPreferences

object ExportPrivacy {
    fun settings(value: UserPreferences) = value.copy(
        geminiApiKey = "", groqApiKey = "", openRouterApiKey = "", aiConnections = emptyMap(),
        googleWebClientId = "", deviceHealthEnabled = false, healthConnectSkipped = false,
        driveSyncEnabled = false, bodyPhotoDriveBackup = false,
    )
    /** Prevent spreadsheet formulas when users open CSV text in spreadsheet software. */
    fun csvCell(value: Any?): String {
        var text = value?.toString().orEmpty()
        if (value !is Number && text.trimStart().firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r', '\n')) text = "'" + text
        return "\"" + text.replace("\"", "\"\"") + "\""
    }
}
