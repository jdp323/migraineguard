package com.migraineguard.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.ThemeMode
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.core.security.BackupCipher
import com.migraineguard.app.core.storage.CompactJson
import com.migraineguard.app.feature.backup.*
import com.migraineguard.app.feature.body.*
import com.migraineguard.app.feature.settings.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import java.util.UUID

/** Every record, key and image in this class is a disposable synthetic fixture. */
@RunWith(AndroidJUnit4::class)
class BodyMeasurementsTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private fun preferences() = UserPreferencesRepository(context,"measurements-fixture-" + UUID.randomUUID())
    private fun capture(name: String) {
        compose.waitForIdle()
        val dir = File(context.getExternalFilesDir(null),"verification").apply { mkdirs() }
        File(dir,name+".png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it) }
    }
    private fun fill(tag: String, value: String) {
        compose.onNodeWithTag(tag).performScrollTo().performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString(value)) }
    }
    private fun tap(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()
    private fun await(predicate: () -> Boolean) = compose.waitUntil(20000,predicate)
    private fun photo(): ByteArray {
        val image = Bitmap.createBitmap(96,128,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(60,120,190)) }
        return ByteArrayOutputStream().also { image.compress(Bitmap.CompressFormat.JPEG,85,it);image.recycle() }.toByteArray()
    }

    @Test fun versionSevenMigrationKeepsPhotosAndJournalsAndOptimizationKeepsRecords() = runBlocking {
        val name = "measurement-v7-fixture-" + UUID.randomUUID() + ".db"
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        val jpeg = photo(); val base64 = Base64.getEncoder().encodeToString(jpeg)
        val schema = InstrumentationRegistry.getInstrumentation().context.assets.open("com.migraineguard.app.core.data.db.AppDatabase/7.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(file,null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                old.execSQL(entity.getString("createSql").replace("$" + "{TABLE_NAME}",entity.getString("tableName")))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) old.execSQL(setup.getString(index))
            old.execSQL("INSERT INTO body_progress VALUES (?,?,?,?,?,?,?,?,?,?)",arrayOf("legacy-photo","2026-10-01","Front","kept legacy note",70.5,81.25,21.75,base64,42,0))
            old.execSQL("INSERT INTO body_progress VALUES (?,?,?,?,?,?,?,?,?,?)",arrayOf("legacy-deletion","2026-09-01","Back","",null,null,null,"",43,1))
            old.execSQL("INSERT INTO water_history VALUES ('2026-10-01',4)")
            old.execSQL("INSERT INTO migraine_episodes(timestampMillis,overallIntensity,notes,symptoms,markers,triggers) VALUES(1000,5,'kept migraine','','[]','')")
            old.version = 7
        }
        val db = Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(*PreservingMigrations.ALL).build()
        try {
            val preserved = db.bodyProgressDao().get("legacy-photo")!!
            assertEquals(base64,preserved.jpegBase64);assertEquals(81.25f,preserved.waistCm!!,0f)
            assertTrue(db.bodyProgressDao().get("legacy-deletion")!!.deleted)
            assertEquals("kept migraine",db.backupDao().migraines().single().notes)
            assertEquals(4,db.waterHistoryDao().get("2026-10-01")!!.cups)
            db.openHelper.readableDatabase.query("SELECT typeof(jpegBytes),length(jpegBytes) FROM body_progress WHERE id='legacy-photo'").use {
                assertTrue(it.moveToFirst());assertEquals("blob",it.getString(0));assertEquals(jpeg.size,it.getInt(1))
            }
            assertTrue(jpeg.size < base64.length)
            val repository = BodyMeasurementRepository(db.bodyMeasurementDao())
            repository.save(date="2026-10-01",valuesCm=mapOf("waist" to 81.25f),notes="fixture")
            val maintenance = JournalStorageRepository(db)
            val before = maintenance.usage();val after = maintenance.optimize()
            assertEquals(1,after.measurementLogs.toInt());assertEquals(jpeg.size.toLong(),after.photoBytes)
            assertEquals(base64,db.bodyProgressDao().get("legacy-photo")!!.jpegBase64)
            assertEquals("kept migraine",db.backupDao().migraines().single().notes)
            val backup = BackupRepository(context,db,preferences()).snapshot()
            val plain = Json { encodeDefaults=false }.encodeToString(backup).toByteArray()
            val packed = CompactJson.pack(plain)
            val dir = File(context.getExternalFilesDir(null),"verification").apply { mkdirs() }
            File(dir,"storage-benchmark.json").writeText(JSONObject().put("syntheticFixture",true)
                .put("oldBase64PhotoBytes",base64.length).put("binaryPhotoBytes",jpeg.size)
                .put("journalJsonBytes",plain.size).put("packedJsonBytes",packed.size)
                .put("databaseBytesBeforeOptimization",before.databaseBytes).put("databaseBytesAfterOptimization",after.databaseBytes).toString())
        } finally { db.close();context.deleteDatabase(name) }
    }

    @Test fun measurementsRestoreFromCompressedAndLegacyBackupsWithoutResurrectingDeletes() = runBlocking {
        val first = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val second = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val other = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val prefs = preferences();val file = File(context.cacheDir,"measurements-fixture-"+UUID.randomUUID()+".mgbak")
        try {
            prefs.setGeminiApiKey("fixture-key-not-for-manual-export")
            val repository = BodyMeasurementRepository(first.bodyMeasurementDao())
            val row = repository.save(date="2026-10-01",valuesCm=mapOf("chest" to 101.5f,"waist" to 80f,"leftBiceps" to 33f),notes="private synthetic measurements")
            val source = BackupRepository(context,first,prefs);val target = BackupRepository(context,second,preferences())
            val old = source.snapshot(includeBodyPhotos=false)
            assertEquals(1,old.bodyMeasurements.size);assertTrue(old.bodyProgress.isEmpty())
            source.export(Uri.fromFile(file),"fixture measurement password".toCharArray())
            val decoded = CompactJson.unpack(BackupCipher.decrypt(file.readBytes(),"fixture measurement password".toCharArray()))
            assertFalse(String(decoded).contains("fixture-key-not-for-manual-export"))
            assertEquals(1,target.restore(Uri.fromFile(file),"fixture measurement password".toCharArray(),false))
            assertEquals(0,target.mergeSnapshot(old,false));assertEquals(row,second.bodyMeasurementDao().all().single())
            repository.save(row.id,"2026-10-02",mapOf("waist" to 79f),"edited fixture")
            assertEquals(1,target.mergeSnapshot(source.snapshot(),false));assertEquals(79f,second.bodyMeasurementDao().get(row.id)!!.measurementsCm.getValue("waist"),0f)
            repository.remove(row.id);assertEquals(1,target.mergeSnapshot(source.snapshot(),false))
            assertEquals(0,target.mergeSnapshot(old,false));assertTrue(second.bodyMeasurementDao().get(row.id)!!.deleted)
            assertTrue(second.bodyMeasurementDao().get(row.id)!!.measurementsCm.isEmpty());assertTrue(other.bodyMeasurementDao().all().isEmpty())
            val owner = AccountStorage.google("measurements-owner@example.invalid")
            val cloud = CloudAccountBackup(owner=owner,savedAtMillis=100,settings=CloudBackupFormat.portable(prefs.preferences.first()),journal=source.snapshot(includeBodyPhotos=false))
            val compressed = CloudBackupFormat.encode(cloud)
            assertTrue(CompactJson.isCompressed(compressed));assertTrue(CloudBackupFormat.decode(compressed,owner).journal.bodyMeasurements.single().deleted)
            try { CloudBackupFormat.decode(compressed,AccountStorage.google("other@example.invalid"));fail("Wrong owner accepted") } catch (_:IllegalArgumentException) { }
            val legacyPhoto = Base64.getEncoder().encodeToString(photo())
            val legacy = JournalBackup(formatVersion=3,createdAtMillis=100,appVersion="legacy-fixture",weights=listOf(WeightEntryEntity(timestampMillis=1000,weightKg=72f,notes="legacy encrypted JSON")),bodyProgress=listOf(BodyProgressEntity("legacy-manual-photo","2026-10-01","Front",jpegBase64=legacyPhoto,modifiedAtMillis=10)))
            val legacyJson = JSONObject(Json { encodeDefaults=true }.encodeToString(legacy)).apply { remove("bodyMeasurements") }.toString().toByteArray()
            file.writeBytes(BackupCipher.encrypt(legacyJson,"fixture measurement password".toCharArray()))
            assertEquals(2,target.restore(Uri.fromFile(file),"fixture measurement password".toCharArray(),false))
            assertEquals(legacyPhoto,second.bodyProgressDao().get("legacy-manual-photo")!!.jpegBase64)
            assertEquals("legacy encrypted JSON",second.backupDao().weights().single().notes)
        } finally { first.close();second.close();other.close();file.delete();prefs.clearAllAiKeys() }
    }

    @Test fun metricEditorRetainsGroupsAndSupportsDatedEditsHistoryTrendsAndDeletion() {
        val db = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val prefs = preferences();val repository = BodyMeasurementRepository(db.bodyMeasurementDao())
        try {
            runBlocking { prefs.saveBodyProfile(BodyProfile(useMetric=true)) }
            compose.setContent { MigraineGuardTheme(themeMode=ThemeMode.LIGHT) { Surface(Modifier.fillMaxSize().imePadding()) {
                BodyMetricsScreen(WeightRepository(db.weightDao()),BodyProgressRepository(context,db.bodyProgressDao()),prefs,{},measurementRepository=repository)
            } } }
            tap("measurements-add");fill("measurement-date","2026-10-01")
            fill("measurement-value-chest","101.5");fill("measurement-value-waist","80");fill("measurement-value-abdomen","85");fill("measurement-value-hips","100")
            tap("measurement-group-Upper body");fill("measurement-value-leftBiceps","33");fill("measurement-value-rightBiceps","34")
            tap("measurement-group-Lower body");fill("measurement-value-leftThigh","60")
            tap("measurement-group-Custom");fill("measurement-custom-name","Left knee");fill("measurement-custom-value","39")
            fill("measurement-notes","Synthetic tape measurements");tap("measurement-save")
            await { runBlocking { repository.observe().first().size } == 1 }
            val first = runBlocking { repository.observe().first().single() }
            assertEquals(8,first.measurementsCm.size);assertEquals(33f,first.measurementsCm.getValue("leftBiceps"),0f)
            assertEquals(39f,first.measurementsCm.getValue("custom:left knee"),0f)
            compose.onNodeWithText("Measurement history").performScrollTo();capture("measurements-history-metric")
            tap("measurement-edit-"+first.id);fill("measurement-date","2026-10-02");fill("measurement-value-waist","79");tap("measurement-save")
            await { runBlocking { db.bodyMeasurementDao().get(first.id) }?.dateIso == "2026-10-02" }
            assertEquals(1,runBlocking { repository.observe().first().size })
            tap("measurements-add");fill("measurement-date","2026-09-15");fill("measurement-value-waist","81");tap("measurement-save")
            await { runBlocking { repository.observe().first().size } == 2 }
            tap("measurements-trends")
            compose.onNodeWithTag("measurement-change").performScrollTo().assertTextContains("Since first log: -2.0 cm")
            capture("measurements-trend-metric")
            tap("measurement-delete-"+first.id);compose.onNodeWithTag("measurement-confirm-delete").assertIsDisplayed().performClick()
            await { runBlocking { repository.observe().first().size } == 1 }
            assertTrue(runBlocking { db.bodyMeasurementDao().get(first.id) }!!.deleted)
        } finally { db.close() }
    }

    @Test fun imperialCustomValuesValidationDraftProtectionAndStorageControlWork() {
        val db = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val repository = BodyMeasurementRepository(db.bodyMeasurementDao())
        val maintenance = JournalStorageRepository(db)
        try {
            compose.setContent { MigraineGuardTheme(themeMode=ThemeMode.LIGHT) { Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
                BodyMeasurementsPanel(repository,false);JournalStorageCard(maintenance)
            } } }
            tap("measurements-add");fill("measurement-date","2026-10-01");fill("measurement-value-waist","31.5");fill("measurement-value-chest","40");tap("measurement-save")
            await { runBlocking { repository.observe().first().size } == 1 }
            assertEquals(80.01f,runBlocking { repository.observe().first().first().measurementsCm.getValue("waist") },.001f)
            compose.onNodeWithText("Waist: 31.5 in").performScrollTo().assertIsDisplayed()
            tap("measurements-add");fill("measurement-date","2026-10-10");fill("measurement-value-waist","0");tap("measurement-save")
            compose.onNodeWithTag("measurement-message").performScrollTo().assertTextContains("Check Waist",substring=true)
            assertEquals(1,runBlocking { repository.observe().first().size })
            tap("measurements-add");tap("measurements-add");compose.onNodeWithTag("measurement-value-waist").performScrollTo().assertTextContains("0")
            fill("measurement-value-waist","33");tap("measurement-group-Custom");fill("measurement-custom-name","Left knee");fill("measurement-custom-value","15.5");tap("measurement-add-custom");tap("measurement-save")
            await { runBlocking { repository.observe().first().size } == 2 }
            val latest = runBlocking { repository.observe().first().first() }
            assertEquals(39.37f,latest.measurementsCm.getValue("custom:left knee"),.001f)
            tap("measurements-add");fill("measurement-value-waist","35");tap("measurement-discard");compose.onNodeWithText("Keep draft").performClick()
            compose.onNodeWithTag("measurement-value-waist").performScrollTo().assertTextContains("35")
            tap("measurement-discard");compose.onNodeWithTag("measurement-confirm-discard").assertIsDisplayed().performClick();assertEquals(2,runBlocking { repository.observe().first().size })
            tap("optimize-journal-storage")
            await { compose.onAllNodesWithTag("journal-storage-message").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("journal-storage-message").performScrollTo().assertTextContains("Storage optimized.",substring=true)
            assertEquals(2,runBlocking { repository.observe().first().size });capture("measurements-imperial-storage")
        } finally { db.close() }
    }
}
