package com.migraineguard.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.db.AppDatabase
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.core.security.AccountStorage
import com.migraineguard.app.feature.auth.*
import com.migraineguard.app.feature.reminders.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SessionAndWaterTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<MigraineGuardApp>()
    private fun fixture() = UserProfile("fixture-"+UUID.randomUUID()+"@example.invalid","Returning fixture",null).let { it.copy(email=it.id) }
    private suspend fun activate(profile: UserProfile) = withContext(Dispatchers.Main) { (app.container.authRepository as DefaultAuthRepository).activateGoogleProfile(profile) }

    @Test fun rememberedIdentityIsEncryptedAndExplicitSignOutClearsIt() {
        val file = "session-fixture-"+UUID.randomUUID()
        val store = RememberedSession(app,file)
        try {
            val profile = fixture()
            store.save(AuthState(true,profile,false))
            assertEquals(profile,RememberedSession(app,file).load()!!.user)
            val raw = app.getSharedPreferences(file,0).getString("session","")!!
            assertFalse(raw.contains(profile.id))
            store.save(AuthState())
            assertNull(RememberedSession(app,file).load())
            app.getSharedPreferences(file,0).edit().putString("session","broken").commit()
            assertNull(store.load())
        } finally { app.getSharedPreferences(file,0).edit().clear().commit() }
    }

    @Test fun returningProfileOpensHomeAndSurvivesActivityRecreation() = runBlocking {
        val auth = app.container.authRepository as DefaultAuthRepository
        try {
            activate(fixture())
            app.container.preferencesRepository.setSetupComplete(true)
            app.container.preferencesRepository.setDriveSyncEnabled(false)
            val remembered = RememberedSession(app).load()!!
            withContext(Dispatchers.Main) { auth.signOut(); auth.restoreRememberedSession(remembered) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                fun home() { compose.waitUntil(15_000) { compose.onAllNodesWithText("Your day, at a glance.").fetchSemanticsNodes().isNotEmpty() }; compose.onNodeWithText("Your day, at a glance.").assertIsDisplayed(); compose.onNodeWithText("Start setup").assertDoesNotExist() }
                home(); scenario.recreate(); home()
                withContext(Dispatchers.Main) { auth.signOut() }
                compose.waitUntil(15_000) { compose.onAllNodesWithText("Start setup").fetchSemanticsNodes().isNotEmpty() }
                assertNull(RememberedSession(app).load())
            }
        } finally { withContext(Dispatchers.Main) { auth.signOut() } }
    }

    @Test fun waterActionsAreAtomicAndRejectWrongAccountExpiredOrRepeatedTaps() = runBlocking {
        val auth = app.container.authRepository as DefaultAuthRepository
        try {
            val profile = fixture(); activate(profile)
            val account = app.container.account
            val db = AppDatabase.get(app,account)
            val owner = app.getSharedPreferences("reminder_owner",0)
            app.container.preferencesRepository.setNotificationsEnabled(true)
            val today = LocalDate.now().toString()
            for(amount in 1..3) {
                val token = UUID.randomUUID().toString()
                owner.edit().putString("water_token",token).putLong("water_issued",System.currentTimeMillis()).commit()
                assertFalse(WaterQuickAdd.apply(app,account,token,4))
                assertTrue(WaterQuickAdd.apply(app,account,token,amount))
                assertFalse(WaterQuickAdd.apply(app,account,token,amount))
            }
            assertEquals(6,db.waterHistoryDao().get(today)!!.cups)
            val expired = UUID.randomUUID().toString()
            owner.edit().putString("water_token",expired).putLong("water_issued",1).commit()
            assertFalse(WaterQuickAdd.apply(app,account,expired,1))
            owner.edit().putString("water_token",expired).putLong("water_issued",System.currentTimeMillis()).commit()
            activate(fixture())
            assertFalse(WaterQuickAdd.apply(app,account,expired,3))
            assertEquals(6,db.waterHistoryDao().get(today)!!.cups)
            assertNull(AppDatabase.get(app,app.container.account).waterHistoryDao().get(today))
        } finally { withContext(Dispatchers.Main) { auth.signOut() } }
    }

    @Test fun notificationButtonsEnqueueOneLocalChoiceWithoutOpeningApp() = runBlocking {
        val auth = app.container.authRepository as DefaultAuthRepository
        try {
            activate(fixture())
            val account = app.container.account
            app.container.preferencesRepository.setNotificationsEnabled(true)
            val notification = WaterQuickAdd.addActions(app,NotificationCompat.Builder(app,ReminderWorker.CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info),account).build()
            assertEquals(listOf("1 glass","2 glasses","3 glasses"),notification.actions.map { it.title.toString() })
            val automation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
            fun shell(command: String) { android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() } }
            shell("pm grant "+app.packageName+" android.permission.POST_NOTIFICATIONS")
            androidx.core.app.NotificationManagerCompat.from(app).notify(WaterQuickAdd.NOTIFICATION_ID,
                android.app.Notification.Builder(app,ReminderWorker.CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Time for water").setContentText("1 glass = 250 ml · choose what you drank")
                    .setActions(*notification.actions).build())
            shell("cmd statusbar expand-notifications")
            automation.waitForIdle(300,5000)
            val directory = java.io.File(app.getExternalFilesDir(null),"verification").apply { mkdirs() }
            automation.takeScreenshot()?.let { bitmap -> java.io.File(directory,"water-notification.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle() }
            shell("cmd statusbar collapse")
            notification.actions[1].actionIntent.send()
            val dao = AppDatabase.get(app,account).waterHistoryDao()
            withTimeout(15_000) { while(dao.get(LocalDate.now().toString())?.cups!=2) delay(100) }
            notification.actions[2].actionIntent.send()
            delay(300)
            assertEquals(2,dao.get(LocalDate.now().toString())!!.cups)
        } finally { withContext(Dispatchers.Main) { auth.signOut() } }
    }
}
