package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TrainerEngineTest {
    private val body=BodyProfile(dateOfBirthIso="1990-01-01",heightCm=180f,weightKg=75f)
    private val profile=TrainerProfile(ready=true,adultConfirmed=true,equipment=setOf(TrainingEquipment.BODYWEIGHT,TrainingEquipment.DUMBBELLS,TrainingEquipment.GYM))
    private fun logged(id:String="machine-chest",kg:Float=40f,reps:Int=12,rir:Int?=3,time:Long=System.currentTimeMillis())=WorkoutEntry(timestampMillis=time,type=WorkoutType.STRENGTH,durationMinutes=15,exerciseName=TrainerExerciseCatalog.byId(id)!!.name,sets=List(2){ExerciseSet(kg,reps,true)},details=WorkoutDetails(repsInReserve=rir,trainerExerciseId=id))
    @Test fun multipleMusclesProduceEditablePrescriptionsWithoutInventedWorkingLoad() {
        val plan=TrainerEngine.session(setOf(Muscle.CHEST,Muscle.QUADS),profile,body,emptyList())
        val coverage=plan.flatMap{TrainerExerciseCatalog.byId(it.exerciseId)!!.muscles}.toSet()
        assertTrue(coverage.containsAll(setOf(Muscle.CHEST,Muscle.QUADS)))
        assertTrue(plan.all{it.sets in 1..3 && it.targetRir==3})
        assertNull(plan.first{TrainerExerciseCatalog.byId(it.exerciseId)!!.equipment!=TrainingEquipment.BODYWEIGHT}.loadKg)
        val changed=TrainerEngine.session(setOf(Muscle.CHEST),profile,body.copy(weightKg=150f,heightCm=200f),emptyList())
        assertNull(changed.single().loadKg)
    }
    @Test fun testedLoadAndComparableHistoryAreUsedConservatively() {
        val exercise=TrainerExerciseCatalog.byId("machine-chest")!!
        assertEquals(22f,TrainerEngine.prescription(exercise,profile.copy(testedLoadsKg=mapOf(exercise.id to 22f)),body,listOf(logged())).loadKg!!,0f)
        assertEquals(40f,TrainerEngine.prescription(exercise,profile,body,listOf(logged(rir=null))).loadKg!!,0f)
        assertEquals(41f,TrainerEngine.prescription(exercise,profile,body,listOf(logged())).loadKg!!,0f)
        assertEquals(2f,TrainerEngine.prescription(exercise,profile,body,listOf(logged(kg=2f))).loadKg!!,0f)
        assertNull(TrainerEngine.prescription(exercise,profile,body,listOf(logged(time=System.currentTimeMillis()-100L*86400000))).loadKg)
    }
    @Test fun avoidAreasAndAdultGateCannotBeBypassedByTargets() {
        val avoid=profile.copy(avoid=setOf(Muscle.TRICEPS))
        assertFalse(TrainerExerciseCatalog.available(avoid).any { Muscle.TRICEPS in it.muscles })
        try { TrainerEngine.session(setOf(Muscle.TRICEPS),avoid,body,emptyList());fail("Excluded area accepted") } catch(_:IllegalArgumentException){}
        try { TrainerEngine.session(setOf(Muscle.CHEST),profile,body.copy(dateOfBirthIso=LocalDate.now().minusYears(15).toString()),emptyList());fail("Adult routine for child accepted") }catch(_:IllegalArgumentException){}
    }
    @Test fun allSupportedWeeklyFrequenciesFitTimeAndHaveRecoveryBetweenRepeatedAreas() {
        for(days in 2..4) {
            val week=TrainerEngine.weekly(profile.copy(daysPerWeek=days),body,emptyList(),setOf(Muscle.CHEST,Muscle.QUADS))
            week.validate();assertEquals(days,week.days.size)
            for(a in week.days)for(b in week.days) if((b.weekday-a.weekday+7)%7==1) {
                val ma=a.exercises.flatMap { TrainerExerciseCatalog.byId(it.exerciseId)!!.muscles }.toSet()
                val mb=b.exercises.flatMap { TrainerExerciseCatalog.byId(it.exerciseId)!!.muscles }.toSet()
                assertTrue(ma.intersect(mb).isEmpty())
            }
            assertTrue(week.days.all { d -> 8+d.exercises.sumOf { it.sets*(1+it.restSeconds/60.0) }<=profile.minutesPerSession+2 })
        }
    }
    @Test fun aiUsesOnlyAllowedCatalogAndIgnoresUntrustedLoadInstructions() {
        val local=TrainerEngine.weekly(profile,body,emptyList(),emptySet())
        val arrangement=AiTrainingArrangement(local.days.map { AiTrainingDay(it.weekday,it.exercises.map{e->e.exerciseId}) })
        val raw=Json.encodeToString(arrangement)
        val parsed=TrainerArrangement.parse(raw,profile,body,emptyList(),emptySet())
        assertTrue(parsed.days.flatMap{it.exercises}.all{it.loadKg==null || it.loadKg==0f})
        val wrong=arrangement.copy(days=arrangement.days.mapIndexed{i,d->if(i==0)d.copy(exerciseIds=listOf("invented-heavy-lift"))else d})
        try{TrainerArrangement.parse(Json.encodeToString(wrong),profile,body,emptyList(),emptySet());fail("Unknown lift accepted")}catch(_:IllegalArgumentException){}
    }
    @Test fun promptExcludesIdentityNotesAndPhotosAndPlansRoundTrip() {
        val message=TrainerPrompt.build(profile,body.copy(displayName="private fixture"),listOf(logged().copy(notes="private journal fixture",equipmentPhotoUri="content://private/fixture")),setOf(Muscle.CHEST))
        assertFalse(message.contains("private fixture"));assertFalse(message.contains("private journal fixture"));assertFalse(message.contains("content://"));assertFalse(message.contains("1990-01-01"))
        val week=TrainerEngine.weekly(profile,body,emptyList(),emptySet())
        assertEquals(week,Json.decodeFromString<TrainingWeek>(Json.encodeToString(week)))
    }
    @Test fun bodyMeshContainsFrontBackRegionsAndHitTestingUsesVisibleSurface() {
        assertTrue(MuscleBodyMesh.faces.size>500)
        val front=MuscleBodyMesh.project(0f,400f,600f)
        assertTrue(front.all { it.a.x.isFinite() && it.b.y.isFinite() })
        assertNull(MuscleBodyMesh.hit(0f,400f,600f,0f,0f))
        assertEquals(Muscle.CHEST,MuscleBodyMesh.hit(0f,400f,600f,200f,210f))
        val back=MuscleBodyMesh.hit(180f,400f,600f,200f,210f)
        assertTrue(back==Muscle.UPPER_BACK || back==Muscle.LATS)
    }
}
