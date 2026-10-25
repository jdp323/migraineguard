package com.migraineguard.app.feature.fitness

import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.WorkoutEntry
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period
import java.util.UUID

@Serializable enum class Muscle(val label: String) {
    CHEST("Chest"), SHOULDERS("Shoulders"), BICEPS("Biceps"), TRICEPS("Triceps"), CORE("Core"), OBLIQUES("Obliques"),
    UPPER_BACK("Upper back"), LATS("Lats"), GLUTES("Glutes"), QUADS("Thighs / quads"), HAMSTRINGS("Hamstrings"), CALVES("Calves")
}
@Serializable enum class TrainingExperience(val label: String) { NEW("New / returning"), REGULAR("Regular"), EXPERIENCED("Experienced") }
@Serializable enum class TrainingGoal(val label: String) { GENERAL("Feel stronger"), MUSCLE("Build muscle"), STRENGTH("Strength") }
@Serializable enum class TrainingEquipment(val label: String) { BODYWEIGHT("Bodyweight"), DUMBBELLS("Dumbbells"), BANDS("Bands"), GYM("Gym machines") }
@Serializable data class TrainerProfile(
    val experience: TrainingExperience = TrainingExperience.NEW,
    val goal: TrainingGoal = TrainingGoal.GENERAL,
    val equipment: Set<TrainingEquipment> = setOf(TrainingEquipment.BODYWEIGHT),
    val daysPerWeek: Int = 3,
    val minutesPerSession: Int = 45,
    val avoid: Set<Muscle> = emptySet(),
    val adultConfirmed: Boolean = false,
    val ready: Boolean = false,
    val testedLoadsKg: Map<String, Float> = emptyMap(),
    val testedLoadsAt: Map<String, Long> = emptyMap(),
) {
    fun validate() {
        require(daysPerWeek in 2..4 && minutesPerSession in 20..75 && equipment.isNotEmpty())
        require(testedLoadsAt.size<=60 && testedLoadsAt.all { (id,time) -> id in testedLoadsKg && time>=0 })
        require(testedLoadsKg.size <= 60 && testedLoadsKg.all { (id,kg) -> TrainerExerciseCatalog.byId(id) != null && kg.isFinite() && kg in 0f..500f })
    }
}

data class TrainerExercise(val id: String, val name: String, val muscles: Set<Muscle>, val equipment: TrainingEquipment,
    val cue: String, val timed: Boolean = false, val perSide: Boolean = false)
object TrainerExerciseCatalog {
    private fun e(id:String,name:String,muscle:Muscle,equipment:TrainingEquipment,cue:String,secondary:Muscle?=null,timed:Boolean=false,perSide:Boolean=false)=
        TrainerExercise(id,name,setOfNotNull(muscle,secondary),equipment,cue,timed,perSide)
    val all = listOf(
        e("wall-pushup","Wall push-up",Muscle.CHEST,TrainingEquipment.BODYWEIGHT,"Hands at chest height. Keep a straight body line; bend elbows comfortably and press away.",Muscle.TRICEPS),
        e("db-floor-press","Dumbbell floor press",Muscle.CHEST,TrainingEquipment.DUMBBELLS,"Lie on the floor, knees bent. Lower elbows gently to the floor; press without locking or bouncing.",Muscle.TRICEPS),
        e("machine-chest","Chest press machine",Muscle.CHEST,TrainingEquipment.GYM,"Adjust the seat so handles are near mid-chest. Use a comfortable range and a controlled return.",Muscle.TRICEPS),
        e("band-press","Standing band chest press",Muscle.CHEST,TrainingEquipment.BANDS,"Use a secure chest-height anchor; inspect the band and keep your trunk steady.",Muscle.TRICEPS),
        e("db-shoulder","Seated dumbbell shoulder press",Muscle.SHOULDERS,TrainingEquipment.DUMBBELLS,"Use back support. Press through a comfortable overhead range without arching your back.",Muscle.TRICEPS),
        e("wall-slide","Wall slide",Muscle.SHOULDERS,TrainingEquipment.BODYWEIGHT,"Stand supported at a wall. Slide arms upward only through a comfortable range.",Muscle.UPPER_BACK),
        e("machine-shoulder","Shoulder press machine",Muscle.SHOULDERS,TrainingEquipment.GYM,"Set the seat to align handles around shoulder level. Keep your back supported.",Muscle.TRICEPS),
        e("band-pull","Band pull-apart",Muscle.UPPER_BACK,TrainingEquipment.BANDS,"Keep arms around chest level with a soft elbow bend; pull apart without shrugging.",Muscle.SHOULDERS),
        e("prone-w","Prone W raise",Muscle.UPPER_BACK,TrainingEquipment.BODYWEIGHT,"Lie face down with forehead supported. Gently lift elbows in a W; avoid straining the neck.",Muscle.SHOULDERS),
        e("db-row","Supported dumbbell row",Muscle.LATS,TrainingEquipment.DUMBBELLS,"Support one hand on a stable surface. Pull the elbow toward your hip without twisting.",Muscle.UPPER_BACK,perSide=true),
        e("band-row","Seated band row",Muscle.LATS,TrainingEquipment.BANDS,"Use a secure anchor. Pull elbows back with a tall trunk and a controlled return.",Muscle.UPPER_BACK),
        e("machine-row","Seated row machine",Muscle.LATS,TrainingEquipment.GYM,"Keep chest supported when available. Pull toward the lower ribs without jerking.",Muscle.UPPER_BACK),
        e("prone-pull","Prone lat pull",Muscle.LATS,TrainingEquipment.BODYWEIGHT,"Lie face down, keep neck neutral and draw elbows toward the ribs slowly. This is an unloaded control exercise.",Muscle.UPPER_BACK),
        e("db-curl","Dumbbell curl",Muscle.BICEPS,TrainingEquipment.DUMBBELLS,"Keep elbows close to your sides. Curl without swinging and lower slowly."),
        e("band-curl","Band curl",Muscle.BICEPS,TrainingEquipment.BANDS,"Stand securely on the band; keep elbows near your sides and avoid snapping the return."),
        e("machine-curl","Biceps curl machine",Muscle.BICEPS,TrainingEquipment.GYM,"Align the machine pivot with your elbow and keep the upper arm supported."),
        e("self-curl","Self-resisted curl",Muscle.BICEPS,TrainingEquipment.BODYWEIGHT,"Apply gentle opposing pressure with your other hand. Move slowly; switch arms.",perSide=true),
        e("band-triceps","Band triceps pressdown",Muscle.TRICEPS,TrainingEquipment.BANDS,"Use a secure high anchor. Keep upper arms still and straighten comfortably."),
        e("machine-triceps","Cable triceps pushdown",Muscle.TRICEPS,TrainingEquipment.GYM,"Keep elbows close and upper arms still. Lower the handle smoothly without leaning on it."),
        e("db-triceps","Lying dumbbell triceps extension",Muscle.TRICEPS,TrainingEquipment.DUMBBELLS,"Use very light dumbbells on a stable surface. Keep upper arms still; bend elbows slowly."),
        e("dead-bug","Dead bug",Muscle.CORE,TrainingEquipment.BODYWEIGHT,"Keep your lower back comfortably supported. Extend opposite limbs slowly while breathing.",perSide=true),
        e("plank","Elevated plank",Muscle.CORE,TrainingEquipment.BODYWEIGHT,"Use a stable elevated surface. Keep a straight body line and breathe throughout.",timed=true),
        e("side-plank","Bent-knee side plank",Muscle.OBLIQUES,TrainingEquipment.BODYWEIGHT,"Support on your forearm and bent knees. Lift hips comfortably; repeat on each side.",timed=true,perSide=true),
        e("chair-squat","Chair squat",Muscle.QUADS,TrainingEquipment.BODYWEIGHT,"Use a stable chair. Sit back with control, knees tracking comfortably over feet, then stand.",Muscle.GLUTES),
        e("db-squat","Goblet squat",Muscle.QUADS,TrainingEquipment.DUMBBELLS,"Hold one dumbbell close to the chest. Squat to a comfortable depth, keeping the whole foot grounded.",Muscle.GLUTES),
        e("leg-press","Leg press",Muscle.QUADS,TrainingEquipment.GYM,"Adjust the seat; keep your back supported. Lower only as far as your pelvis stays stable; do not lock the knees.",Muscle.GLUTES),
        e("split-squat","Supported split squat",Muscle.QUADS,TrainingEquipment.BODYWEIGHT,"Hold a stable support. Lower a short split stance comfortably, then push through the front foot.",Muscle.GLUTES,perSide=true),
        e("bridge","Glute bridge",Muscle.GLUTES,TrainingEquipment.BODYWEIGHT,"Lie with knees bent. Lift hips using your glutes without over-arching the lower back.",Muscle.HAMSTRINGS),
        e("db-hinge","Dumbbell hip hinge",Muscle.HAMSTRINGS,TrainingEquipment.DUMBBELLS,"Keep dumbbells close. Push hips back with a soft knee bend and a neutral, comfortable trunk.",Muscle.GLUTES),
        e("heel-bridge","Heel-dig bridge",Muscle.HAMSTRINGS,TrainingEquipment.BODYWEIGHT,"Lie with heels grounded and knees bent. Gently lift hips while keeping pressure through the heels.",Muscle.GLUTES),
        e("leg-curl","Seated leg curl",Muscle.HAMSTRINGS,TrainingEquipment.GYM,"Align knees with the machine pivot and position pads comfortably. Curl with control."),
        e("calf-raise","Supported calf raise",Muscle.CALVES,TrainingEquipment.BODYWEIGHT,"Use a stable support. Rise onto the balls of the feet and lower slowly without bouncing."),
        e("db-calf","Dumbbell calf raise",Muscle.CALVES,TrainingEquipment.DUMBBELLS,"Hold a support if needed and a light dumbbell. Rise and lower with control."),
        e("machine-calf","Calf raise machine",Muscle.CALVES,TrainingEquipment.GYM,"Set pads comfortably; move through a controlled ankle range without bouncing.")
    )
    fun byId(id:String) = all.find { it.id == id }
    fun available(p:TrainerProfile) = all.filter { (it.equipment == TrainingEquipment.BODYWEIGHT || it.equipment in p.equipment) && it.muscles.none(p.avoid::contains) }
}

@Serializable data class ExercisePrescription(val exerciseId: String, val sets: Int, val minReps: Int, val maxReps: Int,
    val holdSeconds: Int? = null, val restSeconds: Int = 90, val loadKg: Float? = null, val loadReason: String = "", val targetRir: Int = 3) {
    fun validate() {
        require(TrainerExerciseCatalog.byId(exerciseId)!=null && sets in 1..4 && minReps in 5..20 && maxReps in minReps..20)
        require(holdSeconds == null || holdSeconds in 10..60)
        require(restSeconds in 45..180 && targetRir in 2..4 && loadReason.length <= 500)
        require(loadKg == null || loadKg.isFinite() && loadKg in 0f..500f)
    }
}
@Serializable data class TrainerLogRequest(val requestId:String,val prescription:ExercisePrescription)
@Serializable data class TrainingDay(val weekday: Int, val title: String, val exercises: List<ExercisePrescription>)
@Serializable data class TrainingWeek(val id: String = UUID.randomUUID().toString(), val startDateIso: String,
    val source: String = "On-device planner", val note: String = "", val days: List<TrainingDay>, val createdAtMillis: Long = System.currentTimeMillis()) {
    fun validate() {
        require(id.length in 1..64 && startDateIso.length <= 10); LocalDate.parse(startDateIso)
        require(source.length <= 100 && note.length <= 1200 && days.size in 2..4 && createdAtMillis >= 0)
        require(days.map { it.weekday }.distinct().size == days.size)
        days.forEach { d -> require(d.weekday in 0..6 && d.title.length <= 100 && d.exercises.size in 1..6); require(d.exercises.map { it.exerciseId }.distinct().size==d.exercises.size); d.exercises.forEach { it.validate() } }
    }
}

object TrainerEngine {
    fun age(body:BodyProfile,today:LocalDate=LocalDate.now()):Int? = runCatching { Period.between(LocalDate.parse(body.dateOfBirthIso),today).years }.getOrNull()
    fun ensureReady(profile:TrainerProfile,body:BodyProfile) {
        profile.validate()
        require(profile.ready && profile.adultConfirmed && (age(body)?.let { it >= 18 } != false)) { "Complete your adult training preferences first." }
    }
    fun prescription(exercise:TrainerExercise,p:TrainerProfile,body:BodyProfile,history:List<WorkoutEntry>):ExercisePrescription {
        val reduced = p.experience == TrainingExperience.NEW || p.minutesPerSession <= 30 || (age(body) ?: 0) >= 65 || body.activityLevel == com.migraineguard.app.core.domain.ActivityLevel.SEDENTARY
        val strength = p.goal == TrainingGoal.STRENGTH && !reduced
        val sets = if(reduced) 2 else 3
        val min = if(strength) 6 else 8; val max = if(strength) 8 else 12
        val last = history.filter { it.timestampMillis >= System.currentTimeMillis()-90L*86400000 && it.timestampMillis<=System.currentTimeMillis() &&
            (it.details.trainerExerciseId == exercise.id || (it.details.trainerExerciseId == null && TrainingProgress.exerciseKey(it.exerciseName)==TrainingProgress.exerciseKey(exercise.name))) &&
            it.sets.any { s -> s.completed } }.maxByOrNull { it.timestampMillis }
        val completed = last?.sets.orEmpty().filter { it.completed && it.weightKg.isFinite() && it.weightKg in 0f..500f && it.reps in 5..20 }
        val previous = completed.minOfOrNull { it.weightKg }
        val tested = p.testedLoadsKg[exercise.id]?.takeIf { last==null || (p.testedLoadsAt[exercise.id]?:Long.MAX_VALUE)>last.timestampMillis }
        var load = when { exercise.equipment==TrainingEquipment.BODYWEIGHT -> 0f; tested!=null -> tested; else -> previous }
        var reason = when { exercise.equipment==TrainingEquipment.BODYWEIGHT -> "Bodyweight / self-resistance; adjust leverage or range to keep about 3 reps in reserve."
            tested!=null -> "Your confirmed comfortable test load. Recheck the same equipment and units."
            previous!=null -> "Conservative repeat of your recent completed load. Verify the same machine, setup and units."
            else -> "No comparable working load yet. Start with the lightest setting and a controlled test set; choose a load leaving about 3 good reps in reserve." }
        if(tested==null && previous!=null && previous>0 && completed.size>=2 && completed.all { it.reps>=max } && (last?.details?.repsInReserve ?: -1)>=3) {
            val candidate = kotlin.math.floor(previous*1.025f*2f)/2f
            if(candidate>previous && candidate<=previous*1.10f) { load=candidate; reason="Optional small progression from completed top-range sets and your recorded effort. Use only an available increment you can control; keep the previous load if unsure." }
        }
        return ExercisePrescription(exercise.id,sets,min,max,if(exercise.timed)20 else null,if(strength)120 else 90,load,reason)
    }
    fun fitToTime(source:List<ExercisePrescription>,minutes:Int):List<ExercisePrescription> {
        val result=source.toMutableList()
        fun estimated()=8+result.sumOf { it.sets*(1.0+it.restSeconds/60.0) }
        while(estimated()>minutes+2 && result.any { it.sets>1 }) {
            val index=result.indices.maxBy { result[it].sets };val old=result[index]
            result[index]=old.copy(sets=old.sets-1,loadReason=old.loadReason.substringBefore(" Session volume")+" Session volume adjusted to your time budget.")
        }
        require(estimated()<=minutes+2) { "Choose fewer target areas or more session time." }
        return result
    }
    fun session(targets:Set<Muscle>,p:TrainerProfile,body:BodyProfile,history:List<WorkoutEntry>):List<ExercisePrescription> {
        ensureReady(p,body);require(targets.isNotEmpty()) { "Select the areas you want to train." }
        require(targets.none(p.avoid::contains)) { "Remove areas marked to avoid, or update your preferences." }
        val available = TrainerExerciseCatalog.available(p)
        val result = mutableListOf<TrainerExercise>();val remaining=targets.toMutableSet()
        val setCount=if(p.experience==TrainingExperience.NEW || p.minutesPerSession<=30 || (age(body)?:0)>=65 || body.activityLevel==com.migraineguard.app.core.domain.ActivityLevel.SEDENTARY)2 else 3
        val limit=6
        while(remaining.isNotEmpty() && result.size<limit) {
            val next=available.filter { it !in result }.maxByOrNull { e -> e.muscles.count(remaining::contains)*100 + if(e.equipment!=TrainingEquipment.BODYWEIGHT)10 else 0 } ?: break
            if(next.muscles.none(remaining::contains))break
            result+=next;remaining.removeAll(next.muscles)
        }
        require(remaining.isEmpty()) { "These targets do not fit the available time/equipment and areas to avoid. Choose fewer areas, more time or different equipment." }
        return fitToTime(result.map { prescription(it,p,body,history) },p.minutesPerSession)
    }
    fun weekdays(count:Int)=when(count){2->listOf(0,3);3->listOf(0,2,4);else->listOf(0,1,3,4)}
    fun weekly(p:TrainerProfile,body:BodyProfile,history:List<WorkoutEntry>,priorities:Set<Muscle>,start:LocalDate=LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY))):TrainingWeek {
        ensureReady(p,body)
        val upper=setOf(Muscle.CHEST,Muscle.LATS,Muscle.SHOULDERS)
        val lower=setOf(Muscle.QUADS,Muscle.HAMSTRINGS,Muscle.CALVES)
        val days=weekdays(p.daysPerWeek).mapIndexed { i,day ->
            val wanted=(if(p.daysPerWeek==4) (if(i%2==0)upper+Muscle.CORE else lower) else setOf(Muscle.CHEST,Muscle.LATS,Muscle.QUADS,Muscle.HAMSTRINGS,Muscle.CORE,Muscle.SHOULDERS)) - p.avoid
            val extras=priorities.filter { it !in p.avoid && (p.daysPerWeek!=4 || ((it in lower || it==Muscle.GLUTES)==(i%2==1))) }
            val targets=(wanted+extras).toSet()
            var chosen=runCatching { session(targets,p,body,history) }.getOrNull()
            if(chosen==null) chosen=session(wanted,p,body,history)
            TrainingDay(day,if(p.daysPerWeek==4) { if(i%2==0) "Upper body" else "Lower body" } else "Full body",chosen)
        }
        val covered=days.flatMap { it.exercises }.flatMap { TrainerExerciseCatalog.byId(it.exerciseId)!!.muscles }.toSet()
        require((priorities-p.avoid).all(covered::contains)) { "Some priorities do not fit this week. Choose fewer areas or more session time." }
        return TrainingWeek(startDateIso=start.toString(),days=days,note="5-minute easy warm-up; finish with a short easy cooldown. Keep 2–4 reps in reserve. Rest days can be easy walking or comfortable mobility. This is a general adult training template, not injury rehabilitation.").also { it.validate() }
    }
}
