package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.BodyProfile
import org.junit.Assert.*
import org.junit.Test

class AnatomyZoomTest {
    @Test fun everyTrainingRegionBuildsImmediatelyWithDefaultPreferences(){
        val p=TrainerProfile()
        assertFalse(p.ready);assertFalse(p.adultConfirmed)
        for(muscle in Muscle.entries){
            val result=TrainerEngine.session(setOf(muscle),p,BodyProfile(),emptyList())
            assertTrue(result.isNotEmpty())
            assertTrue(result.flatMap{TrainerExerciseCatalog.byId(it.exerciseId)!!.muscles}.contains(muscle))
            result.forEach{it.validate()}
        }
    }
    @Test fun zoomAndPanKeepAllFourCoordinateSystemsInvertibleAndBounded(){
        val boxes=listOf(listOf(0f,0f,724f,1448f),listOf(724f,0f,724f,1448f),listOf(-50f,-40f,734f,1538f),listOf(756f,0f,774f,1448f))
        for(box in boxes)for(zoom in listOf(1f,1.5f,2f,3f))for(pan in listOf(-100000f,0f,100000f)){
            val t=AnatomyTransform.fit(box,324f,350f,zoom,pan,pan)
            val x=box[0]+box[2]*.4f;val y=box[1]+box[3]*.3f
            assertEquals(x,t.bodyX(t.canvasX(x)),.001f)
            assertEquals(y,t.bodyY(t.canvasY(y)),.001f)
            assertTrue(t.left<=324f && t.top<=350f)
            assertTrue(t.left+box[2]*t.scale>=0 && t.top+box[3]*t.scale>=0)
        }
    }
    @Test fun everyCatalogMovementHasEquipmentAndStepsAndGifIdsMatchRealExercises(){
        assertEquals(9,ExerciseGuideCatalog.gifIds.size)
        for(e in TrainerExerciseCatalog.all){
            assertTrue(ExerciseGuideCatalog.equipment(e).isNotBlank())
            assertEquals(3,ExerciseGuideCatalog.setup(e).size)
            assertTrue(ExerciseGuideCatalog.setup(e).all{it.isNotBlank()})
        }
        assertTrue(ExerciseGuideCatalog.gifIds.all{TrainerExerciseCatalog.byId(it)!=null})
        assertFalse(ExerciseGuideCatalog.hasGif("unknown"))
    }
}
