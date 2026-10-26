package com.migraineguard.app.feature.fitness

data class ExerciseDemo(val exerciseId:String,val asset:String,val author:String,val source:String)
object ExerciseGuideCatalog {
    val gifIds = setOf("machine-row","machine-shoulder","db-shoulder","machine-calf","db-curl","cable-overhead-triceps","db-triceps","leg-press","leg-curl")
    fun hasGif(id:String) = id in gifIds
    fun equipment(e:TrainerExercise):String = when(e.equipment) {
        TrainingEquipment.BODYWEIGHT -> "Bodyweight · a stable support where the movement calls for one"
        TrainingEquipment.DUMBBELLS -> "Dumbbells · use a weight you can control; match the per-hand or single-dumbbell convention"
        TrainingEquipment.BANDS -> "Resistance band · inspect it and secure its anchor before starting"
        TrainingEquipment.GYM -> "Gym machine / cable station · adjust the seat or pads and check the pin and safety catches"
    }
    fun setup(e:TrainerExercise):List<String> {
        val specific=when(e.id) {
            "machine-chest" -> "Set the seat so the handles meet your mid-chest. Keep feet planted and your back supported."
            "machine-shoulder" -> "Set handles around shoulder level and use the machine's back support."
            "machine-row" -> "Set the cable handle and seat so you can start upright without rounding your back. A seated cable-row variant is shown."
            "machine-curl" -> "Align the machine pivot with your elbows and rest upper arms on the pad."
            "machine-triceps" -> "Fit a cable handle to a high pulley. Stand balanced and keep upper arms close to your torso."
            "cable-overhead-triceps" -> "Fit a suitable handle to a high pulley. Face away from the station with a stable stance and the cable behind your head; keep upper arms steady."
            "leg-press" -> "Adjust seat and safety stops; place feet securely on the platform. Keep hips and back supported."
            "leg-curl" -> "Align knees with the machine pivot; set the thigh pad and ankle roller comfortably."
            "machine-calf" -> "Adjust the pad and foot support. The demo shows a seated calf machine; check your model's instructions."
            "wall-pushup" -> "Choose a clear, stable wall. Set hands at chest height and step back a comfortable distance."
            "chair-squat" -> "Use a stable chair that will not slide; leave room around it."
            "plank" -> "Use a fixed elevated surface that will not tip or move."
            else -> equipment(e)
        }
        return listOf(specific,e.cue,if(e.timed) "Keep breathing through the hold. Record actual seconds and rounds."
            else "Move with control in both directions. Record the sets and repetitions you actually perform.")
    }
}
