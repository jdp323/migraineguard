package com.migraineguard.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.feature.backup.*
import com.migraineguard.app.feature.body.*
import com.migraineguard.app.feature.fitness.FakeHealthDataRepository
import com.migraineguard.app.feature.settings.SettingsScreen
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream

@RunWith(AndroidJUnit4::class)
class BodyProgressTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private fun prefs() = UserPreferencesRepository(context,"body-test-"+UUID.randomUUID())
    private fun photo(): File {
        val file = File(context.cacheDir,"synthetic-progress-"+UUID.randomUUID()+".png")
        val bitmap = Bitmap.createBitmap(240,320,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap); canvas.drawColor(Color.rgb(225,235,250))
        val paint = Paint().apply { color=Color.rgb(55,75,150);textSize=20f }
        canvas.drawCircle(120f,90f,35f,paint); canvas.drawRoundRect(80f,130f,160f,260f,20f,20f,paint)
        canvas.drawText("SYNTHETIC",55f,300f,paint)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle();return file
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        val target=File(context.getExternalFilesDir(null),"verification").apply { mkdirs() }
        File(target,name+".png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    @Test fun normalizedPhotoRestoresOnceAndDeletedPhotoCannotReturnFromOldSnapshot() = runBlocking {
        val first=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val second=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val image=photo();val file=File(context.cacheDir,"body-recovery-"+UUID.randomUUID()+".mgbak")
        try {
            val repository=BodyProgressRepository(context,first.bodyProgressDao())
            repository.add(Uri.fromFile(image),"2026-10-01","Front","private fixture",70f,80f,20f)
            val source=BackupRepository(context,first,prefs());val snapshot=source.snapshot()
            assertTrue(snapshot.bodyProgress.single().jpegBase64.startsWith("/9j/"))
            assertTrue(source.snapshot(includeBodyPhotos=false).bodyProgress.isEmpty())
            source.export(Uri.fromFile(file),"fixture body password".toCharArray())
            assertFalse(String(file.readBytes(),Charsets.ISO_8859_1).contains("private fixture"))
            val target=BackupRepository(context,second,prefs())
            assertEquals(1,target.restore(Uri.fromFile(file),"fixture body password".toCharArray(),false))
            assertEquals(0,target.mergeSnapshot(snapshot,false))
            val restored=second.bodyProgressDao().all().single()
            assertEquals(snapshot.bodyProgress.single().jpegBase64,restored.jpegBase64)
            BodyProgressRepository(context,second.bodyProgressDao()).remove(restored)
            assertEquals(0,target.mergeSnapshot(snapshot,false))
            val deleted=second.bodyProgressDao().all().single();assertTrue(deleted.deleted);assertEquals("",deleted.jpegBase64);assertEquals("",deleted.notes)
            assertEquals(1,source.mergeSnapshot(target.snapshot(),false))
            assertTrue(first.bodyProgressDao().all().single().deleted)
        } finally { first.close();second.close();image.delete();file.delete() }
    }
    @Test fun readableExportContainsBodyJpegAndCsvButNoConfiguredKeysOrLoginConsent() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build();val image=photo();val file=File(context.cacheDir,"readable-"+UUID.randomUUID()+".zip")
        val preferences=prefs()
        try {
            preferences.setGeminiApiKey("fixture-secret-must-not-export")
            BodyProgressRepository(context,db.bodyProgressDao()).add(Uri.fromFile(image),"2026-10-01","Side","=SUM(A1)",70f,80f,null)
            BackupRepository(context,db,preferences).exportReadable(Uri.fromFile(file))
            val files=mutableMapOf<String,ByteArray>()
            ZipInputStream(file.inputStream()).use { zip -> while(true) {val entry=zip.nextEntry?:break;files[entry.name]=zip.readBytes()} }
            assertTrue(files.keys.any { it.startsWith("body-photos/") && it.endsWith(".jpg") })
            assertEquals(12,files.size)
            val structured=String(files.getValue("journal-and-settings.json"))
            assertFalse(structured.contains("fixture-secret-must-not-export"))
            assertTrue(String(files.getValue("body-progress.csv")).contains("'=SUM(A1)"))
            assertTrue(String(files.getValue("README.txt")).contains("NOT encrypted"))
            val decoded=kotlinx.serialization.json.Json.decodeFromString<JournalBackup>(structured)
            assertFalse(decoded.settings!!.deviceHealthEnabled);assertTrue(decoded.settings!!.aiConnections.isEmpty())
        } finally { db.close();image.delete();file.delete();preferences.clearAllAiKeys() }
    }
    @Test fun privateTimelineShowsComparisonAndCanSaveNoteCheckIn() {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build();val image=photo();val preferences=prefs();val repository=BodyProgressRepository(context,db.bodyProgressDao())
        try {
            runBlocking {
                repository.add(Uri.fromFile(image),"2026-09-01","Front","Synthetic first check-in",72f,82f,22f)
                repository.add(Uri.fromFile(image),"2026-10-01","Front","Synthetic later check-in",70f,80f,21f)
            }
            compose.setContent { MigraineGuardTheme { BodyMetricsScreen(WeightRepository(db.weightDao()),repository,preferences,{}) } }
            compose.waitUntil(10000) { compose.onAllNodesWithText("Compare over time").fetchSemanticsNodes().isNotEmpty() }
            capture("body-progress-overview")
            compose.onNodeWithText("Compare over time").performScrollTo();capture("body-progress-compare")
            compose.onNodeWithText("Recorded weight change: -2.0 kg").performScrollTo().assertExists()
            compose.onNodeWithTag("body-add").performScrollTo().performClick()
            compose.onNodeWithTag("body-notes").performScrollTo().performTextInput("Synthetic note-only check-in")
            compose.onNodeWithTag("body-save").performScrollTo().performClick()
            compose.waitUntil(10000) { compose.onAllNodesWithText("Check-in saved privately on this phone.").fetchSemanticsNodes().isNotEmpty() }
            assertEquals(3,runBlocking { db.bodyProgressDao().all().size })
            assertFalse(runBlocking { preferences.preferences.first().bodyPhotoDriveBackup })
        } finally { db.close();image.delete() }
    }
    @Test fun movementIntervalAndDataExportControlsAreReachable() {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build();val preferences=prefs()
        try {
            runBlocking { preferences.setNotificationsEnabled(false);preferences.setUpdateChecks(false) }
            compose.setContent { MigraineGuardTheme { SettingsScreen(preferences,BackupRepository(context,db,preferences),FakeHealthDataRepository(),onSignOut={},onBack={},initialGroup="Reminders") } }
            compose.onNodeWithTag("reminder-Stand, stretch or walk").performScrollTo().performClick()
            compose.waitUntil(10000) { compose.onAllNodesWithText("Movement break: every 60 min ▾").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Movement break: every 60 min ▾").performScrollTo().performClick()
            compose.onNodeWithText("Every 90 minutes").performClick()
            compose.waitUntil(10000) { runBlocking { preferences.preferences.first().movementReminderMinutes==90 } }
            capture("settings-movement")
            compose.onNodeWithText("Data",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithText("Export my data").assertIsDisplayed();capture("settings-data-export")
            compose.onNodeWithText("Export readable data & body photos").assertExists()
        } finally { db.close() }
    }
}
