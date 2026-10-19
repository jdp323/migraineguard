package com.migraineguard.app

import android.graphics.Bitmap
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
        val image = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory, name + ".png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
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
            awaitText("Start setup")
            compose.onNodeWithText("MigraineGuard v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
            compose.onNodeWithText("Start setup").performClick()
            awaitText("Continue locally")
            compose.onNodeWithText("MigraineGuard v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
            compose.onNodeWithText("Continue locally").performClick()
            awaitText("Skip Health Connect for now")
            compose.onNodeWithText("Skip Health Connect for now").performScrollTo().performClick()
            awaitText("Skip — use offline brain only")
            compose.onNodeWithText("Skip — use offline brain only").performScrollTo().performClick()
            awaitText("Complete your profile")
            compose.onNodeWithText("Imperial (ft/in · lb)").performScrollTo().performClick()
            compose.onNodeWithText("154.3").assertExists()
            compose.onNodeWithText("Metric (cm / kg)").performScrollTo().performClick()
            compose.onNodeWithText("Save & continue").performScrollTo().performClick()
            awaitText("Enter MigraineGuard")
            val saved = runBlocking { container.preferencesRepository.preferences.first() }
            assertEquals(70f, saved.bodyProfile.weightKg!!, 0.1f)
            compose.onNodeWithText("Enter MigraineGuard").performClick()
            for (label in listOf("Migraine", "Fitness", "Food", "Coach", "Insights", "Home")) {
                awaitText(label)
                compose.onNode(hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick().assertIsSelected()
                if (label == "Fitness") { awaitText("Move, rest, recover."); capture("fitness-light") }
                if (label == "Home") capture("home-light")
            }
            runBlocking { container.preferencesRepository.setThemeMode(ThemeMode.DARK) }
            compose.waitForIdle()
            capture("home-dark")
            compose.onNode(hasText("Fitness") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
            awaitText("Move, rest, recover.")
            capture("fitness-dark")
        }
    }
}
