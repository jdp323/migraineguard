package com.migraineguard.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.core.ai.connection
import com.migraineguard.app.feature.fitness.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Destructive actions affect only named synthetic fixtures in the hosted test emulator. */
@RunWith(AndroidJUnit4::class)
class FullAppSimulationTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val context get()=ApplicationProvider.getApplicationContext<Context>()
    private val app get()=context as MigraineGuardApp
    private fun setup(){
        app.container.authRepository.signInDemo()
        runBlocking{
            val p=app.container.preferencesRepository
            p.saveBodyProfile(BodyProfile(displayName="Simulation fixture",dateOfBirthIso="1990-01-01",heightCm=175f,weightKg=70f,useMetric=true,profileComplete=true))
            p.setSetupComplete(true);p.setReduceTransparency(false);p.clearAllAiKeys();p.setPreferredAiProvider("offline")
            p.setNotificationsEnabled(false);p.setUpdateChecks(false);p.setThemeMode(ThemeMode.LIGHT);p.setDynamicColor(false)
            p.saveTrainerProfile(TrainerProfile())
        }
    }
    private fun await(text:String){compose.waitUntil(20000){compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}}
    private fun capture(name:String){
        compose.waitForIdle()
        val dir=File(context.getExternalFilesDir(null),"verification").apply{mkdirs()}
        File(dir,name+".png").outputStream().use{compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it)}
    }
    private fun captureWindow(name:String){
        compose.waitForIdle()
        val bitmap=requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val dir=File(context.getExternalFilesDir(null),"verification").apply{mkdirs()}
        try { File(dir,name+".png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)} } finally { bitmap.recycle() }
    }
    private fun hideKeyboard(scenario: ActivityScenario<MainActivity>){
        scenario.onActivity { activity -> WindowCompat.getInsetsController(activity.window,activity.window.decorView).hide(WindowInsetsCompat.Type.ime()) }
        compose.waitUntil(10000){
            var hidden=false
            scenario.onActivity { activity -> hidden=ViewCompat.getRootWindowInsets(activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime())!=true }
            hidden
        }
        compose.waitForIdle()
    }
    private fun expectRecord(name:String,timeout:Long,predicate:()->Boolean){
        try { compose.waitUntil(timeout,predicate) } catch(e:Throwable){
            runCatching { captureWindow("failure-"+name); File(context.getExternalFilesDir(null),"verification/failure-"+name+".txt").writeText(compose.onRoot().printToString()) }
            throw e
        }
    }
    private fun settings(){compose.onNodeWithTag("tab-home").performClick();compose.onNodeWithContentDescription("Settings").performClick();await("Make it yours.")}
    private fun trainer(){compose.onNodeWithTag("tab-fitness").performClick();compose.onNodeWithTag("fitness-trainer").performScrollTo().performClick()}
    private fun input(label:String,value:String){compose.onNode(hasText(label) and hasSetTextAction()).performScrollTo().performTextReplacement(value)}

    @Test fun freshTrainerBuildsAndLogsWithoutCompletingPreferencesOrAgeConfirmation(){
        setup()
        ActivityScenario.launch(MainActivity::class.java).use{
            trainer()
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-QUADS").performScrollTo().performClick()
            compose.onNodeWithText("Clear selection").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().assertIsNotSelected().performClick()
            compose.onNodeWithTag("muscle-QUADS").performScrollTo().performClick()
            compose.onNodeWithTag("trainer-adult").assertDoesNotExist()
            compose.onNodeWithTag("trainer-save-preferences").assertDoesNotExist()
            compose.onNodeWithTag("trainer-build-session").performScrollTo().performClick()
            await("Today's focus")
            compose.onNodeWithText("Complete your adult training preferences first.").assertDoesNotExist()
            compose.onNodeWithTag("trainer-log-wall-pushup").performScrollTo().assertIsEnabled()
            capture("simulation-direct-workout")
            compose.onNodeWithTag("trainer-log-wall-pushup").performClick();await("Trainer draft")
            compose.onNodeWithTag("exercise-name").performScrollTo().assertTextContains("Wall push-up")
            compose.onNodeWithTag("set-done-0").performScrollTo().assertIsOff().performClick()
            compose.onNodeWithText("Save exercise").performScrollTo().performClick();await("Exercise saved ✓")
            assertTrue(runBlocking{app.container.workoutRepository.inRange(0,Long.MAX_VALUE)}.any{it.details.trainerExerciseId=="wall-pushup"&&it.sets.any{set->set.completed}})
            compose.onAllNodesWithText("Use as next session").onFirst().performScrollTo().performClick()
            compose.onNodeWithTag("set-done-0").performScrollTo().assertIsOff()
            compose.onNodeWithTag("fitness-trainer").performScrollTo().performClick()
            compose.onNodeWithTag("trainer-preferences").performScrollTo().performClick()
            compose.onNodeWithText("Gym machines").performScrollTo().performClick()
            compose.onNodeWithTag("trainer-save-preferences").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{app.container.preferencesRepository.preferences.first()}.trainerProfile.equipment.contains(TrainingEquipment.GYM)}
            compose.onNodeWithTag("trainer-build-session").performScrollTo().performClick()
            compose.onNodeWithTag("trainer-log-machine-chest").performScrollTo().assertIsEnabled()
            val priorPlans=runBlocking{app.container.preferencesRepository.preferences.first()}.trainingPlans.map{it.id}.toSet()
            compose.onNodeWithTag("trainer-build-week").performScrollTo().performClick();await("Review your week")
            compose.onNodeWithTag("trainer-save-week").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{app.container.preferencesRepository.preferences.first()}.trainingPlans.any{it.id !in priorPlans}}
            compose.onNode(hasText("Saved plans",substring=true) and hasClickAction()).performScrollTo().performClick()
            compose.onAllNodesWithText("Open plan").onFirst().performScrollTo().performClick()
            compose.onAllNodesWithText("Remove saved plan").onFirst().performScrollTo().performClick()
            compose.onNodeWithText("Keep").performClick()
            val planCount=runBlocking{app.container.preferencesRepository.preferences.first()}.trainingPlans.size
            compose.onAllNodesWithText("Remove saved plan").onFirst().performScrollTo().performClick()
            compose.onNodeWithText("Remove").performClick()
            compose.waitUntil(10000){runBlocking{app.container.preferencesRepository.preferences.first()}.trainingPlans.size==planCount-1}
        }
    }

    @Test fun zoomedSelectionPinchPanFitAndRearHeadSurviveActivityRecreation(){
        setup()
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            trainer()
            repeat(2){compose.onNodeWithTag("body-zoom-in").performScrollTo().performClick()}
            compose.onNodeWithTag("muscle-body").performScrollTo().assert(
                SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Front · Male ·  · Zoom 200%"))
            compose.onNodeWithTag("muscle-body").performTouchInput{
                val t=AnatomyTransform.fit(listOf(0f,0f,724f,1448f),width.toFloat(),height.toFloat(),2f)
                click(Offset(t.canvasX(310f),t.canvasY(380f)))
            }
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().assertIsSelected()
            compose.onNodeWithTag("body-zoom-fit").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-body").performScrollTo().performTouchInput {
                down(0,Offset(center.x-30f,center.y));down(1,Offset(center.x+30f,center.y))
                moveTo(0,Offset(center.x-80f,center.y));moveTo(1,Offset(center.x+80f,center.y))
                up(0);up(1)
            }
            val state=compose.onNodeWithTag("muscle-body").fetchSemanticsNode().config[SemanticsProperties.StateDescription]
            assertTrue(state.substringAfter("Zoom ").substringBefore("%").toInt()>100)
            compose.onNodeWithTag("body-zoom-fit").performScrollTo().performClick()
            compose.onNodeWithTag("body-back").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-body").performScrollTo();capture("simulation-rear-head")
            repeat(2){compose.onNodeWithTag("body-zoom-in").performScrollTo().performClick()}
            compose.onNodeWithTag("muscle-body").performScrollTo().performTouchInput{swipeDown(durationMillis=500)}
            assertTrue(compose.onNodeWithTag("muscle-body").fetchSemanticsNode().config[SemanticsProperties.StateDescription].startsWith("Back"))
            capture("simulation-zoomed-head")
            scenario.recreate()
            compose.onNodeWithTag("muscle-body").performScrollTo()
            assertTrue(compose.onNodeWithTag("muscle-body").fetchSemanticsNode().config[SemanticsProperties.StateDescription].contains("Zoom 200%"))
            compose.onNodeWithTag("body-zoom-fit").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().assertIsSelected()
        }
    }

    @Test fun exerciseLibraryFiltersPlaysGifAndProtectsAnExistingDraft(){
        setup()
        ActivityScenario.launch(MainActivity::class.java).use{
            compose.onNodeWithTag("tab-fitness").performClick();compose.onNodeWithTag("fitness-training").performScrollTo().performClick()
            compose.onNodeWithText("Edit").performScrollTo().performClick();compose.onNodeWithText("Cancel").performClick()
            compose.onNodeWithText("Edit").performScrollTo().performClick()
            compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).performSemanticsAction(SemanticsActions.SetProgress){it(4f)}
            compose.onNodeWithText("Save goal").performClick()
            compose.waitUntil(10000){runBlocking{app.container.preferencesRepository.preferences.first()}.weeklyWorkoutGoal==4}
            compose.onNodeWithTag("exercise-name").performScrollTo().performTextInput("Keep this simulation draft")
            compose.onNodeWithTag("training-library").performScrollTo().performClick()
            compose.onNodeWithTag("exercise-library-search").performTextInput("leg press")
            compose.onNodeWithTag("library-only-gifs").performScrollTo().performClick()
            compose.onNodeWithTag("exercise-library-list").performScrollToNode(hasTestTag("library-guide-leg-press"))
            compose.onNodeWithTag("library-guide-leg-press").performClick()
            compose.onNodeWithTag("exercise-gif").assertExists()
            compose.onNodeWithTag("demo-play").performScrollTo().performClick()
            compose.onNodeWithTag("demo-status").assertTextContains("Playing", substring = true)
            compose.onNodeWithTag("demo-play").performClick()
            compose.onNodeWithTag("demo-status").assertTextContains("Paused", substring = true)
            compose.onNodeWithTag("demo-restart").performClick()
            captureWindow("simulation-exercise-gif")
            compose.onNodeWithTag("demo-close").performClick()
            compose.onNodeWithTag("exercise-library-list").performScrollToNode(hasTestTag("library-use-leg-press"))
            compose.onNodeWithTag("library-use-leg-press").performClick()
            await("Replace this exercise draft?")
            compose.onNodeWithText("Keep my draft").performClick()
            compose.onNodeWithTag("exercise-name").performScrollTo().assertTextContains("Keep this simulation draft")
            compose.onNodeWithTag("training-library").performScrollTo().performClick()
            compose.onNodeWithTag("exercise-library-search").performTextInput("leg press")
            compose.onNodeWithTag("exercise-library-list").performScrollToNode(hasTestTag("library-use-leg-press"))
            compose.onNodeWithTag("library-use-leg-press").performClick()
            compose.onNodeWithText("Use trainer draft").performClick()
            compose.onNodeWithTag("exercise-name").performScrollTo().assertTextContains("Leg press")
            compose.onNodeWithTag("set-done-0").performScrollTo().assertIsOff()
            compose.onNodeWithText("Add set").performScrollTo().performClick()
            compose.onNodeWithTag("set-reps-2").assertExists()
            compose.onNodeWithText("Remove last set").performScrollTo().performClick()
            compose.onNodeWithTag("set-reps-2").assertDoesNotExist()
        }
    }

    @Suppress("DEPRECATION")
    @Test fun allOfflineGifAssetsHaveMultipleFramesAndDistributedAttribution(){
        val list=context.assets.open("exercise_demos/index.json").bufferedReader().use{JSONArray(it.readText())}
        assertEquals(ExerciseGuideCatalog.gifIds.size,list.length())
        repeat(list.length()){index->
            val record=list.getJSONObject(index)
            assertTrue(ExerciseGuideCatalog.hasGif(record.getString("exerciseId")))
            assertTrue(record.getString("source").startsWith("https://wger.de/media/"))
            assertEquals("CC BY-SA 4.0",record.getString("license"))
            val movie=context.assets.open("exercise_demos/"+record.getString("file")).use{Movie.decodeStream(it)}
            assertNotNull(movie);assertTrue(movie.duration()>=4000)
            val bitmap=Bitmap.createBitmap(80,140,Bitmap.Config.ARGB_8888)
            fun pixels(time:Int):IntArray {
                bitmap.eraseColor(Color.WHITE);movie.setTime(time)
                val c=Canvas(bitmap);c.scale(80f/movie.width(),140f/movie.height());movie.draw(c,0f,0f)
                return IntArray(80*140).also{bitmap.getPixels(it,0,80,0,0,80,140)}
            }
            val start=pixels(0)
            assertTrue("Motion missing: "+record.getString("exerciseId"),listOf(1000,2200,3500).any{!start.contentEquals(pixels(it))})
            bitmap.recycle()
        }
        val license=context.assets.open("licenses/wger-exercise-media.txt").bufferedReader().use{it.readText()}
        assertTrue(license.contains("Creative Commons"));assertTrue(license.contains("Goulart"))
    }

    @Test fun waterFoodMedicationAndWeightActionsPersistActualSyntheticRecords(){
        setup()
        val c=app.container
        ActivityScenario.launch(MainActivity::class.java).use{ scenario ->
            compose.onAllNodesWithText("Add water").onFirst().performScrollTo().performClick();await("One sip at a time.")
            val cups=runBlocking{c.waterIntakeRepository.cupsToday.first()}
            compose.onNodeWithContentDescription("Add cup").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.waterIntakeRepository.cupsToday.first()}==cups+1}
            compose.onNodeWithContentDescription("Remove cup").performClick()
            compose.waitUntil(10000){runBlocking{c.waterIntakeRepository.cupsToday.first()}==cups}
            compose.onNodeWithContentDescription("Back").performClick()
            compose.onNodeWithTag("tab-food").performClick()
            val before=runBlocking{c.nutritionAnalyzer.observeRecent(100).first()}.size
            compose.onNodeWithTag("food-description").performScrollTo().performTextInput("Simulation banana and plain yogurt")
            try {
                hideKeyboard(scenario)
                compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex) and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
                    .performScrollToNode(hasText("Analyze description") and hasClickAction())
                compose.onNodeWithText("Analyze description").assertIsDisplayed().performClick()
                expectRecord("food-submit",20000){runBlocking{c.nutritionAnalyzer.observeRecent(100).first()}.size>before}
            } catch(e:Throwable) {
                runCatching { captureWindow("failure-food-submit") }
                runCatching { File(context.getExternalFilesDir(null),"verification/failure-food-submit.txt").writeText(compose.onRoot().printToString()) }
                throw e
            }
            capture("simulation-food-saved")
            settings()
            compose.onNodeWithText("Medications & supplements").performScrollTo().performClick();await("Keep your routine close.")
            input("Name","Simulation supplement");input("Dose","Fixture label")
            compose.onNode(hasText("Add") and hasClickAction()).performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.medicationRepository.observeMedications().first()}.any{it.name=="Simulation supplement"}}
            val med=runBlocking{c.medicationRepository.observeMedications().first()}.first{it.name=="Simulation supplement"}
            compose.onNodeWithTag("medication-taken-"+med.id).performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.medicationRepository.observeMedications().first()}.first{it.id==med.id}.takenToday}
            compose.onNodeWithTag("medication-delete-"+med.id).performClick()
            compose.waitUntil(10000){runBlocking{c.medicationRepository.observeMedications().first()}.none{it.id==med.id}}
            compose.onNodeWithContentDescription("Back").performClick()
            compose.onNodeWithText("Body progress / photos / weight").performScrollTo().performClick();await("Log weight")
            compose.onNodeWithTag("body-weight-input").performScrollTo().performTextInput("0")
            compose.onNodeWithText("Save weight").performScrollTo().assertIsNotEnabled()
            compose.onNodeWithTag("body-weight-input").performScrollTo().performTextReplacement("71")
            compose.onNodeWithText("Save weight").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.weightRepository.latest()}?.weightKg==71f && runBlocking{c.preferencesRepository.preferences.first()}.bodyProfile.weightKg==71f}
            assertEquals(71f,runBlocking{c.preferencesRepository.preferences.first()}.bodyProfile.weightKg!!,0f)
        }
    }

    @Test fun migraineMapUndoClearSaveOpenAndDeleteWorkThroughTheRealScreens(){
        setup()
        val c=app.container
        ActivityScenario.launch(MainActivity::class.java).use{
            compose.onNodeWithTag("tab-migraine").performClick()
            compose.onNodeWithText("Log migraine").performScrollTo().performClick()
            compose.onNodeWithTag("migraine-notes").performScrollTo().performTextInput("Simulation episode fixture")
            compose.onNodeWithContentDescription("Rotate left").performScrollTo().performClick()
            compose.onNodeWithContentDescription("Rotate right").performScrollTo().performClick()
            compose.onNodeWithTag("pain-map").performScrollTo().performTouchInput{click(center)}
            compose.onNodeWithText("Undo point").performScrollTo().performClick()
            compose.onNodeWithTag("pain-map").performScrollTo().performTouchInput{click(center)}
            compose.onNodeWithText("Clear view (1)").performScrollTo().performClick()
            compose.onNodeWithText("Nausea").performScrollTo().performClick()
            compose.onNodeWithText("Stress").performScrollTo().performClick()
            compose.onNodeWithText("Save episode · 0 points").performScrollTo().performClick()
            compose.waitUntil(20000){runBlocking{c.migraineRepository.observeEpisodes().first()}.any{it.notes=="Simulation episode fixture"}}
            await("Episode history")
            compose.onAllNodesWithText("Open").onFirst().performScrollTo().performClick()
            compose.onNodeWithText("Notes: Simulation episode fixture").performScrollTo().assertIsDisplayed()
            capture("simulation-migraine-detail")
            compose.onNodeWithText("Delete episode").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.migraineRepository.observeEpisodes().first()}.none{it.notes=="Simulation episode fixture"}}
        }
    }

    @Test fun settingsReminderIntervalsAiKeysAppearanceAndOfflineCoachAreFunctional(){
        setup()
        val c=app.container
        ActivityScenario.launch(MainActivity::class.java).use{ scenario ->
            settings()
            compose.onNodeWithText("AI",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithTag("ai-provider-picker").performScrollTo().performClick()
            compose.onNodeWithText("OpenAI / ChatGPT API").performClick()
            compose.onNodeWithTag("ai-provider-key").performScrollTo().performTextReplacement("simulation-key-not-a-real-secret")
            compose.onNodeWithText("Save provider").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.preferencesRepository.preferences.first()}.preferredAiProvider=="openai"}
            compose.onNodeWithText("Remove selected key").performScrollTo().performClick();await("Selected provider key removed.")
            compose.waitUntil(10000){runBlocking{c.preferencesRepository.preferences.first()}.connection("openai").apiKey.isBlank()}
            compose.onNodeWithText("Use on-device AI only").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.preferencesRepository.preferences.first()}.preferredAiProvider=="offline"}
            compose.onNodeWithText("Reminders",useUnmergedTree=true).performScrollTo().performClick()
            for(label in listOf("Drinking water","Stand, stretch or walk","Log meals / food photos","Medication check-in","Migraine journal check-in")){
                val node=compose.onNodeWithTag("reminder-"+label).performScrollTo()
                if(!node.fetchSemanticsNode().config[SemanticsProperties.ToggleableState].let{it==androidx.compose.ui.state.ToggleableState.On})node.performClick()
                compose.waitUntil(10000){compose.onNodeWithTag("reminder-"+label).fetchSemanticsNode().config[SemanticsProperties.ToggleableState]==androidx.compose.ui.state.ToggleableState.On}
                node.assertIsOn()
            }
            for((label,value)in listOf("Water" to 60,"Movement break" to 45,"Food" to 180,"Medication" to 720,"Migraine" to 1440)){
                compose.onNodeWithTag("interval-"+label).performScrollTo().performClick()
                compose.onNodeWithText(if(value==1440)"Every day" else "Every "+value+" minutes").performScrollTo().performClick()
            }
            input("Start hour 0–23","8");input("End hour 0–23","8")
            compose.onNodeWithText("Save active hours").performScrollTo().performClick();await("Choose different hours from 0 to 23.")
            input("End hour 0–23","21");compose.onNodeWithText("Save active hours").performScrollTo().performClick();await("Hours saved.")
            val p=runBlocking{c.preferencesRepository.preferences.first()}
            assertEquals(60,p.waterReminderMinutes);assertEquals(45,p.movementReminderMinutes);assertEquals(180,p.foodReminderMinutes);assertEquals(720,p.medicationReminderMinutes);assertEquals(1440,p.migraineReminderMinutes);assertEquals(21,p.reminderEndHour)
            compose.onNodeWithText("App",useUnmergedTree=true).performScrollTo().performClick()
            compose.onNodeWithTag("check-updates").performScrollTo().performClick()
            compose.waitUntil(30000){compose.onAllNodesWithTag("update-status").fetchSemanticsNodes().any{n->n.config[SemanticsProperties.Text].joinToString{it.text}!="Checking GitHub…"}}
            val updateStatus=compose.onNodeWithTag("update-status").fetchSemanticsNode().config[SemanticsProperties.Text].joinToString{it.text}
            File(context.getExternalFilesDir(null),"verification").apply{mkdirs()}.resolve("updates-check-status.txt").writeText(updateStatus)
            capture("simulation-updates")
            compose.onNodeWithContentDescription("Reduce transparency").performScrollTo().performClick()
            compose.waitUntil(10000){runBlocking{c.preferencesRepository.preferences.first()}.reduceTransparency}
            compose.onAllNodesWithText("Back").onLast().performScrollTo().performClick()
            compose.onNodeWithTag("tab-coach").performClick()
            val count=runBlocking{c.aiCoachRepository.observeSessions().first()}.size
            compose.onNodeWithTag("coach-question").performScrollTo().performTextInput("Suggest a gentle walking habit")
            hideKeyboard(scenario)
            compose.onNodeWithText("Send").performScrollTo().assertIsDisplayed()
            captureWindow("coach-before-submit")
            compose.onNodeWithText("Send").performClick()
            captureWindow("coach-after-tap")
            expectRecord("coach-submit",30000){runBlocking{c.aiCoachRepository.observeSessions().first()}.size>count}
            assertTrue(runBlocking{c.aiCoachRepository.observeSessions().first()}.first().fullResponse.isNotBlank())
            compose.onNodeWithTag("tab-insights").performClick();await("See the bigger picture.")
            capture("simulation-insights")
            runBlocking{c.preferencesRepository.setNotificationsEnabled(false);c.preferencesRepository.setReduceTransparency(false)}
        }
    }
}
