package com.migraineguard.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.data.db.*
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.design.MigraineGuardTheme
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.feature.backup.BackupRepository
import com.migraineguard.app.feature.fitness.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class TrainerFlowTest {
    @get:Rule val compose=createEmptyComposeRule()
    private val context get()=ApplicationProvider.getApplicationContext<Context>()
    private val body=BodyProfile(displayName="Trainer fixture",dateOfBirthIso="1990-01-01",heightCm=180f,weightKg=75f,useMetric=true,profileComplete=true)
    private val profile=TrainerProfile(ready=true,adultConfirmed=true,equipment=setOf(TrainingEquipment.BODYWEIGHT,TrainingEquipment.GYM))
    private fun await(text:String) { compose.waitUntil(15000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
    private fun capture(name:String) {
        compose.waitForIdle();val dir=File(context.getExternalFilesDir(null),"verification").apply{mkdirs()}
        File(dir,name+".png").outputStream().use{compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it)}
    }
    @Test fun bodySelectionRotationPlanHandoffAndWeeklySaveWorkAcrossModes() {
        val app=context as MigraineGuardApp;app.container.authRepository.signInDemo();val c=app.container
        runBlocking { c.preferencesRepository.saveBodyProfile(body);c.preferencesRepository.setSetupComplete(true);c.preferencesRepository.setNotificationsEnabled(false);c.preferencesRepository.setUpdateChecks(false);c.preferencesRepository.clearAllAiKeys();c.preferencesRepository.setThemeMode(ThemeMode.LIGHT);c.preferencesRepository.setDynamicColor(false);c.preferencesRepository.saveTrainerProfile(profile) }
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithTag("tab-fitness").performClick();compose.onNodeWithTag("fitness-trainer").performScrollTo().performClick()
            compose.onNodeWithTag("body-front").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-body").performScrollTo().performTouchInput { click(androidx.compose.ui.geometry.Offset(width*.5f,height*.31f)) }
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().assertIsSelected()
            compose.onNodeWithTag("muscle-QUADS").performScrollTo().performClick().assertIsSelected()
            compose.onNodeWithTag("muscle-body").performScrollTo();capture("trainer-body-front-light")
            compose.onNodeWithTag("muscle-body").performTouchInput{swipeLeft(durationMillis=500)}
            compose.onNodeWithTag("tab-fitness").assertIsSelected()
            compose.onNodeWithTag("body-back").performScrollTo().performClick()
            compose.waitUntil(10000){compose.onNodeWithTag("muscle-body").fetchSemanticsNode().config[SemanticsProperties.StateDescription].startsWith("Back")}
            compose.onNodeWithTag("muscle-body").performScrollTo();capture("trainer-body-back-light")
            compose.onNodeWithTag("muscle-CHEST").assertIsSelected();compose.onNodeWithTag("muscle-QUADS").assertIsSelected()
            compose.onNodeWithTag("trainer-build-session").performScrollTo().performClick();await("Today's focus")
            compose.onNodeWithTag("trainer-log-machine-chest").performScrollTo();capture("trainer-prescription-light")
            compose.onNodeWithTag("trainer-log-machine-chest").performClick();await("Trainer draft")
            compose.onNodeWithTag("exercise-name").performScrollTo().assertTextContains("Chest press machine")
            compose.onNodeWithTag("set-weight-0").performScrollTo().performTextReplacement("10")
            compose.onNodeWithTag("set-done-0").performScrollTo().performClick()
            compose.onNodeWithTag("workout-rir").performScrollTo().performTextInput("3")
            compose.onNodeWithText("Save exercise").performScrollTo().performClick();await("Exercise saved ✓")
            val saved=runBlocking{c.workoutRepository.inRange(0,Long.MAX_VALUE)}.filter{it.details.trainerExerciseId=="machine-chest"}.maxBy{it.timestampMillis}
            assertEquals(3,saved.details.repsInReserve);assertEquals(10f,saved.sets.first{it.completed}.weightKg,0f)
            compose.onNodeWithTag("fitness-trainer").performScrollTo().performClick()
            compose.onNodeWithTag("muscle-CHEST").performScrollTo().assertIsSelected()
            compose.onNodeWithTag("trainer-ai-week").performScrollTo().assertIsNotEnabled()
            compose.onNodeWithTag("trainer-build-week").performScrollTo().performClick();await("Review your week")
            compose.onNodeWithTag("trainer-save-week").performScrollTo();capture("trainer-week-review")
            compose.onNodeWithTag("trainer-save-week").performClick()
            compose.waitUntil(10000){runBlocking{c.preferencesRepository.preferences.first().trainingPlans.isNotEmpty()}}
            runBlocking{c.preferencesRepository.setThemeMode(ThemeMode.DARK)}
            compose.onNodeWithTag("muscle-body").performScrollTo();capture("trainer-body-dark")
            compose.onNodeWithTag("tab-home").performClick();await("Your day, at a glance.");capture("glass-home-dark")
        }
    }
    @Test fun trainerPrefillProtectsExistingDraftAndTimedHoldsSaveActualDetails() {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val prefs=UserPreferencesRepository(context,"trainer-log-"+UUID.randomUUID());val repo=WorkoutRepository(db.workoutDao())
        val request=mutableStateOf<String?>(null)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity{it.setContent{MigraineGuardTheme{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){WorkoutJournal(repo,prefs,trainerRequestJson=request.value)}}}}
                compose.onNodeWithTag("exercise-name").performScrollTo().performTextInput("Retained fixture draft")
                val p=ExercisePrescription("plank",2,8,12,holdSeconds=20,loadKg=0f)
                compose.runOnIdle{request.value=Json.encodeToString(TrainerLogRequest("request-one",p))}
                await("Replace this exercise draft?");compose.onNodeWithText("Keep my draft").performClick()
                compose.onNodeWithTag("exercise-name").assertTextContains("Retained fixture draft")
                compose.runOnIdle{request.value=Json.encodeToString(TrainerLogRequest("request-two",p))}
                await("Replace this exercise draft?");compose.onNodeWithText("Use trainer draft").performClick()
                compose.onNodeWithTag("set-weight-0").assertDoesNotExist()
                compose.onNodeWithTag("stretch-hold").performScrollTo().assertTextContains("20")
                capture("trainer-timed-log")
                compose.onNodeWithText("Save exercise").performScrollTo().performClick();await("Exercise saved ✓")
                val saved=runBlocking{db.backupDao().workouts().single().toDomain()}
                assertEquals("plank",saved.details.trainerExerciseId);assertEquals(20,saved.details.holdSeconds);assertEquals(2,saved.details.rounds)
            }
        } finally {db.close()}
    }
    @Test fun plansEffortAndGlassPreferenceRestoreWithoutCrossAccountLeakage()=runBlocking {
        val first=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build();val second=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        val a=UserPreferencesRepository(context,"trainer-source-"+UUID.randomUUID());val b=UserPreferencesRepository(context,"trainer-target-"+UUID.randomUUID())
        val other=UserPreferencesRepository(context,"trainer-other-"+UUID.randomUUID());val file=File(context.cacheDir,"trainer-"+UUID.randomUUID()+".mgbak")
        try {
            a.saveBodyProfile(body);a.saveTrainerProfile(profile);a.setReduceTransparency(true);a.saveTrainingPlan(TrainerEngine.weekly(profile,body,emptyList(),setOf(Muscle.CHEST)))
            first.workoutDao().insert(WorkoutEntity.fromDomain(WorkoutEntry(timestampMillis=1000,type=WorkoutType.STRENGTH,durationMinutes=10,exerciseName="Chest press machine",sets=listOf(ExerciseSet(10f,10,true)),details=WorkoutDetails(repsInReserve=3,trainerExerciseId="machine-chest"))))
            a.setGeminiApiKey("fixture-source-key");b.setGeminiApiKey("fixture-preserved-target-key")
            BackupRepository(context,first,a).export(Uri.fromFile(file),"fixture trainer password".toCharArray())
            BackupRepository(context,second,b).restore(Uri.fromFile(file),"fixture trainer password".toCharArray(),true)
            val restored=b.preferences.first();assertEquals(1,restored.trainingPlans.size);assertEquals(profile,restored.trainerProfile);assertTrue(restored.reduceTransparency)
            assertEquals("fixture-preserved-target-key",restored.geminiApiKey)
            assertEquals(3,second.backupDao().workouts().single().toDomain().details.repsInReserve)
            assertTrue(other.preferences.first().trainingPlans.isEmpty());assertFalse(other.preferences.first().trainerProfile.ready)
        } finally {first.close();second.close();file.delete();a.clearAllAiKeys();b.clearAllAiKeys()}
    }
    @Test fun cloudPlannerRequiresExplicitConsentAndCancellationDoesNotFallback()=runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build();val prefs=UserPreferencesRepository(context,"trainer-ai-"+UUID.randomUUID())
        try {
            prefs.saveBodyProfile(body);prefs.saveTrainerProfile(profile)
            val local=TrainerEngine.weekly(profile,body,emptyList(),emptySet())
            val json=Json.encodeToString(AiTrainingArrangement(local.days.map{AiTrainingDay(it.weekday,it.exercises.map{e->e.exerciseId})}))
            var calls=0
            val repo=TrainerRepository(prefs,WorkoutRepository(db.workoutDao())){calls++;Result.Success(json)}
            try{repo.week(emptySet(),true,false);fail("Consent required")}catch(_:IllegalArgumentException){}
            assertEquals(0,calls)
            assertTrue(repo.week(emptySet(),true,true).plan.source.startsWith("AI"));assertEquals(1,calls)
            val invalid=TrainerRepository(prefs,WorkoutRepository(db.workoutDao())){Result.Success("not a week")}
            assertEquals("On-device planner",invalid.week(emptySet(),true,true).plan.source)
            val cancelled=TrainerRepository(prefs,WorkoutRepository(db.workoutDao())){throw CancellationException("fixture cancelled")}
            try{cancelled.week(emptySet(),true,true);fail("Cancellation must propagate")}catch(_:CancellationException){}
        }finally{db.close()}
    }
}
