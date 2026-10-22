package com.migraineguard.app

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.domain.BodyProfile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Genuine touch drags and native back dispatch, with synthetic local drafts and no AI/network calls. */
@RunWith(AndroidJUnit4::class)
class SwipeNavigationTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val app get()=ApplicationProvider.getApplicationContext<MigraineGuardApp>()
    @Before fun prepare() = runBlocking {
        app.container.authRepository.signInDemo()
        app.container.preferencesRepository.setSetupComplete(true)
        app.container.preferencesRepository.setNotificationsEnabled(false)
        app.container.preferencesRepository.setUpdateChecks(false)
        app.container.preferencesRepository.saveBodyProfile(BodyProfile(displayName="Gesture fixture",profileComplete=true))
    }
    @After fun finish() { app.container.authRepository.signOut() }
    private fun selected(label:String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("tab-"+label).fetchSemanticsNodes().any { it.config.getOrElse(SemanticsProperties.Selected){false} } }
        compose.waitForIdle()
        compose.onNodeWithTag("tab-"+label).assertIsSelected()
    }
    private fun tap(label:String) { compose.onNodeWithTag("tab-"+label).performClick(); selected(label) }
    private fun swipe(left:Boolean) {
        compose.onNodeWithTag("main-pager").performTouchInput {
            swipe(Offset(width*(if(left).8f else .2f),height*.25f),Offset(width*(if(left).2f else .8f),height*.25f),400)
        }
    }

    @Test fun horizontalSwipesMoveTabsAndKeepOffscreenDrafts() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            selected("home")
            swipe(true); selected("migraine")
            swipe(true); selected("fitness")
            swipe(false); selected("migraine")
            swipe(false); selected("home")
            tap("food")
            compose.onNodeWithTag("food-description").performScrollTo().performTextReplacement("Gesture fixture lunch")
            tap("home"); tap("food")
            compose.onNodeWithTag("food-description").performScrollTo().assertTextContains("Gesture fixture lunch")
            tap("fitness")
            compose.onNodeWithText("Training",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithTag("exercise-name").performScrollTo().performTextReplacement("Gesture fixture exercise")
            tap("home"); tap("fitness")
            compose.onNodeWithTag("exercise-name").performScrollTo().assertTextContains("Gesture fixture exercise")
            tap("migraine")
            compose.onNodeWithText("Log migraine").performScrollTo().performClick()
            compose.onNodeWithTag("pain-map").performScrollTo().performTouchInput { swipeLeft() }
            selected("migraine")
            compose.onNodeWithTag("migraine-notes").performScrollTo().performTextReplacement("Unsaved gesture fixture")
            tap("insights"); tap("migraine")
            compose.onNodeWithTag("migraine-notes").performScrollTo().assertTextContains("Unsaved gesture fixture")
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithText("Make space to check in.").assertIsDisplayed()
        }
    }

    @Test fun nativeBackReturnsThroughDetailsAndRecreationKeepsSelectedPage() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            selected("home")
            compose.onNodeWithContentDescription("Settings").performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("Make it yours.").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithText("Edit profile").performScrollTo().performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("Your profile").fetchSemanticsNodes().isNotEmpty()}
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.waitUntil(10_000){compose.onAllNodesWithText("Edit profile").fetchSemanticsNodes().isNotEmpty()}
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            selected("home")
            compose.onAllNodesWithText("Add water").onFirst().performScrollTo().performClick()
            compose.waitUntil(10_000){compose.onAllNodesWithText("One sip at a time.").fetchSemanticsNodes().isNotEmpty()}
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            selected("home")
            tap("food")
            compose.onNodeWithTag("food-description").performScrollTo().performTextReplacement("Recreated meal draft")
            compose.waitForIdle()
            compose.onNodeWithTag("food-description").assertTextContains("Recreated meal draft")
            repeat(3) {
                scenario.recreate()
                selected("food")
                compose.onNodeWithTag("food-description").performScrollTo().assertTextContains("Recreated meal draft")
                compose.waitForIdle()
            }
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            selected("home")
        }
    }
}
