package com.migraineguard.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.migraineguard.app.core.domain.ThemeMode
import java.io.File
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.domain.BodyProfile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic emulator-only first run; never uses real account/health/cloud credentials. */
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {
    @get:Rule val compose = createEmptyComposeRule()

    private fun awaitText(text: String) {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.getExternalFilesDir(null), "verification").apply { mkdirs() }
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory, name + ".png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        // Compose owns the captured bitmap; do not recycle shared image storage.
    }

    @Test fun onboardingPreservesWeightAndMainTabsOpen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as MigraineGuardApp
        app.container.authRepository.signInDemo()
        val container = app.container
        runBlocking {
            container.preferencesRepository.setSetupComplete(false)
            container.preferencesRepository.setDynamicColor(false)
            container.preferencesRepository.setThemeMode(ThemeMode.LIGHT)
            container.preferencesRepository.clearAllAiKeys()
            container.preferencesRepository.setNotificationsEnabled(false)
            container.preferencesRepository.setUpdateChecks(false)
            container.preferencesRepository.saveBodyProfile(BodyProfile(
                displayName = "Emulator fixture", dateOfBirthIso = "1990-01-01",
                heightCm = 180f, weightKg = 70f, useMetric = true,
            ))
        }
        container.authRepository.signOut()
        ActivityScenario.launch(MainActivity::class.java).use {
            try {
            awaitText("Start setup")
            compose.onNodeWithText("MigraineGuard v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
            capture("setup-welcome")
            compose.onNodeWithText("Start setup").performScrollTo().assertIsDisplayed().performClick()
            awaitText("Continue locally")
            compose.onNodeWithText("MigraineGuard v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
            compose.onNodeWithText("Continue locally").performScrollTo().assertIsDisplayed().performClick()
            awaitText("Skip Health Connect for now")
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Connection check completed").fetchSemanticsNodes().isNotEmpty() }
            capture("setup-health")
            compose.onNodeWithText("Skip Health Connect for now").performScrollTo().performClick()
            awaitText("Skip verification for now")
            compose.onNodeWithText("Skip verification for now").performScrollTo().performClick()
            awaitText("Complete your profile")
            compose.onNodeWithText("Imperial (ft/in · lb)").performScrollTo().performClick()
            compose.onNodeWithText("154.3").assertExists()
            compose.onNodeWithText("Metric (cm / kg)").performScrollTo().performClick()
            capture("setup-profile")
            compose.onNodeWithText("Save & continue").performScrollTo().performClick()
            awaitText("Enter MigraineGuard")
            compose.onNodeWithTag("verification-Health").assert(hasAnyDescendant(hasContentDescription("Skipped")))
            compose.onNodeWithTag("verification-AI").assert(hasAnyDescendant(hasContentDescription("Skipped")))
            capture("setup-review")
            val saved = runBlocking { container.preferencesRepository.preferences.first() }
            assertEquals(70f, saved.bodyProfile.weightKg!!, 0.1f)
            compose.onNodeWithText("Enter MigraineGuard").performScrollTo().assertIsDisplayed().performClick()
            val pages = listOf("Migraine" to "Make space to check in.", "Fitness" to "Move, rest, recover.",
                "Food" to "Eat with awareness.", "Coach" to "A little guidance.",
                "Insights" to "See the bigger picture.", "Home" to "Your day, at a glance.")
            for ((theme, suffix) in listOf(ThemeMode.LIGHT to "light", ThemeMode.DARK to "dark")) {
                runBlocking { container.preferencesRepository.setThemeMode(theme) }
                for ((label, heading) in pages) {
                    awaitText(label)
                    compose.onNodeWithTag("tab-"+label.lowercase())
                        .performClick().assertIsSelected()
                    awaitText(heading)
                    compose.onNodeWithText(heading).assertIsDisplayed()
                    capture(label.lowercase() + "-" + suffix)
                }
            }
            compose.onNodeWithTag("tab-fitness").performClick()
            compose.onNodeWithText("Training", useUnmergedTree = true).performScrollTo().performClick()
            awaitText("Your training goal")
            capture("training-overview")
            compose.onNodeWithTag("exercise-name").performScrollTo().performTextInput("Leg press fixture")
            compose.onNodeWithTag("set-weight-0").performScrollTo().performTextInput("40")
            compose.onNodeWithTag("set-reps-0").performScrollTo().performTextReplacement("8")
            compose.onNodeWithTag("set-done-0").performScrollTo().performClick()
            capture("training-set-entry")
            compose.onNodeWithText("Save exercise").performScrollTo().performClick()
            awaitText("Exercise saved ✓")
            val logged = runBlocking { container.workoutRepository.inRange(0,Long.MAX_VALUE) }.first { it.exerciseName == "Leg press fixture" }
            assertEquals(40f,logged.sets.single().weightKg,0.01f)
            assertEquals(8,logged.sets.single().reps)
            compose.onNodeWithText("Leg press fixture").performScrollTo().assertIsDisplayed()
            capture("training-history")
            compose.onNodeWithText("Log exercise",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithTag("activity-TREADMILL").performScrollTo().performClick()
            compose.onNodeWithTag("activity-OTHER").performScrollTo().assertIsDisplayed()
            capture("training-cardio-picker")
            compose.onNodeWithTag("exercise-name").performScrollTo().performTextReplacement("Treadmill fixture")
            compose.onNodeWithTag("set-weight-0").assertDoesNotExist()
            compose.onNodeWithTag("cardio-speed").performScrollTo().performTextInput("6.5")
            compose.onNodeWithTag("cardio-incline").performScrollTo().performTextInput("3")
            compose.onNodeWithTag("cardio-distance").performScrollTo().performTextInput("2")
            capture("training-cardio-details")
            compose.onNodeWithText("Save exercise").performScrollTo().performClick()
            awaitText("Exercise saved ✓")
            val cardio = runBlocking { container.workoutRepository.inRange(0,Long.MAX_VALUE) }.first { it.exerciseName=="Treadmill fixture" }
            assertEquals(6.5f,cardio.details.speedKmh!!,0.01f); assertEquals(3f,cardio.details.inclinePercent!!,0.01f); assertEquals(2f,cardio.details.distanceKm!!,0.01f); assertEquals(0,cardio.sets.size)
            compose.onNodeWithText("Log exercise",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithTag("activity-STRETCHING").performScrollTo().performClick()
            compose.onNodeWithTag("exercise-name").performScrollTo().performTextReplacement("Stretching fixture")
            compose.onNodeWithTag("cardio-speed").assertDoesNotExist()
            compose.onNodeWithTag("set-weight-0").assertDoesNotExist()
            compose.onNodeWithTag("stretch-hold").performScrollTo().performTextInput("30")
            compose.onNodeWithTag("stretch-rounds").performScrollTo().performTextInput("3")
            capture("training-stretch-details")
            compose.onNodeWithText("Save exercise").performScrollTo().performClick()
            awaitText("Exercise saved ✓")
            val stretch = runBlocking { container.workoutRepository.inRange(0,Long.MAX_VALUE) }.first { it.exerciseName=="Stretching fixture" }
            assertEquals(30,stretch.details.holdSeconds); assertEquals(3,stretch.details.rounds); assertEquals(0,stretch.sets.size)
            runBlocking { container.workoutRepository.delete(cardio.id); container.workoutRepository.delete(stretch.id) }
            compose.onNodeWithTag("tab-home").performClick()
            compose.onAllNodesWithText("Add water").onFirst().performScrollTo().performClick()
            awaitText("One sip at a time.")
            capture("water-dark")
            compose.onNodeWithContentDescription("Back").performClick()
            awaitText("Your day, at a glance.")
            compose.onNodeWithContentDescription("Settings").performClick()
            awaitText("Make it yours.")
            capture("settings-dark")
            compose.onNodeWithText("AI", useUnmergedTree = true).performScrollTo().performClick()
            awaitText("AI providers")
            capture("settings-ai")
            compose.onNodeWithTag("ai-provider-picker").performScrollTo().performClick()
            compose.onNodeWithText("Anthropic Claude").performClick()
            compose.onNodeWithTag("ai-provider-model").assertTextContains("claude-haiku-4-5-20251001")
            compose.onNodeWithText("Reminders", useUnmergedTree = true).performScrollTo().performClick()
            awaitText("Notification controls")
            capture("settings-reminders")
            compose.onNodeWithText("App", useUnmergedTree = true).performScrollTo().performClick()
            compose.onNodeWithText("Check for updates").performScrollTo().assertIsDisplayed()
            capture("settings-updates")
            runBlocking { container.workoutRepository.delete(logged.id) }


            } catch (failure: Throwable) {
                runCatching { capture("failure") }
                throw failure
            }
        }
    }
}
