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
                "Insights" to "See the bigger picture.", "Home" to "Find your rhythm.")
            for ((theme, suffix) in listOf(ThemeMode.LIGHT to "light", ThemeMode.DARK to "dark")) {
                runBlocking { container.preferencesRepository.setThemeMode(theme) }
                for ((label, heading) in pages) {
                    awaitText(label)
                    compose.onNode(hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
                        .performClick().assertIsSelected()
                    awaitText(heading)
                    compose.onNodeWithText(heading).assertIsDisplayed()
                    capture(label.lowercase() + "-" + suffix)
                }
            }
            compose.onAllNodesWithText("Add water").onFirst().performScrollTo().performClick()
            awaitText("One sip at a time.")
            capture("water-dark")
            compose.onNodeWithContentDescription("Back").performClick()
            awaitText("Find your rhythm.")
            compose.onNodeWithContentDescription("Settings").performClick()
            awaitText("Make it yours.")
            capture("settings-dark")

            } catch (failure: Throwable) {
                runCatching { capture("failure") }
                throw failure
            }
        }
    }
}
