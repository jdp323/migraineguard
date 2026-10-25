package com.migraineguard.app.feature.fitness

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainerScreen(repository:TrainerRepository,preferences:UserPreferencesRepository,workouts:WorkoutRepository,onLog:(ExercisePrescription)->Unit) {
    val prefs by preferences.preferences.collectAsStateWithLifecycle(UserPreferences())
    val entries by workouts.observeWorkouts().collectAsStateWithLifecycle(emptyList())
    val scope=rememberCoroutineScope()
    var musclesJson by rememberSaveable { mutableStateOf("[]") }
    val muscles=Json.decodeFromString<Set<Muscle>>(musclesJson)
    var editing by rememberSaveable { mutableStateOf(false) }
    var todayJson by rememberSaveable { mutableStateOf("[]") }
    val today=Json.decodeFromString<List<ExercisePrescription>>(todayJson)
    var weekJson by rememberSaveable { mutableStateOf<String?>(null) }
    val draftWeek=weekJson?.let { Json.decodeFromString<TrainingWeek>(it) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var share by remember { mutableStateOf(false) }
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    var showPlans by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<String?>(null) }
    SectionCard("Training studio",icon=Icons.Default.FitnessCenter) {
        MuscleBodySelector(muscles) { m -> musclesJson=Json.encodeToString(if(m in muscles)muscles-m else muscles+m);todayJson="[]" }
        if(muscles.isNotEmpty()) TextButton(onClick={musclesJson="[]";todayJson="[]"}) { Text("Clear selection") }
    }
    SectionCard("Made for your routine",icon=Icons.Default.Tune) {
        val body=prefs.bodyProfile
        val age=TrainerEngine.age(body)
        Text(listOfNotNull(age?.let{"$it years"},body.heightCm?.let{"%.0f cm".format(it)},body.weightKg?.let{if(body.useMetric)"%.1f kg".format(it) else "%.1f lb".format(it*2.2046226f)}).joinToString(" · ").ifBlank{"Your body profile is available in Account settings."})
        Text(prefs.trainerProfile.experience.label+" · "+prefs.trainerProfile.goal.label+" · "+prefs.trainerProfile.minutesPerSession+" min",style=MaterialTheme.typography.titleSmall)
        Text("Starting volume adapts to your routine and experience. Working loads use your own test loads or comparable completed sets.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick={editing=!editing},modifier=Modifier.fillMaxWidth().testTag("trainer-preferences")) { Text(if(editing)"Close preferences" else "Training preferences") }
        if(editing || !prefs.trainerProfile.ready) TrainerProfileEditor(prefs.trainerProfile) { value -> scope.launch { preferences.saveTrainerProfile(value);editing=false;message="Training preferences saved.";todayJson="[]" } }
        Button(onClick={scope.launch { busy=true;message="";try { todayJson=Json.encodeToString(repository.session(muscles));message="Your session is ready. Review the loads and choose each exercise to log it." } catch(e:CancellationException){throw e} catch(e:Exception){message=e.message?:"Could not build this session."} finally{busy=false} }},enabled=!busy && muscles.isNotEmpty(),modifier=Modifier.fillMaxWidth().testTag("trainer-build-session")) { Text(if(busy)"Building…" else "Build today's workout") }
        if(message.isNotBlank()) Text(message,modifier=Modifier.testTag("trainer-message"),style=MaterialTheme.typography.bodySmall)
    }
    if(today.isNotEmpty()) {
        SectionCard("Today's focus") {
            Text(muscles.joinToString { it.label },style=MaterialTheme.typography.titleMedium)
            Text("Warm up with about 5 minutes of easy movement and a light practice set. Leave 2–4 good reps in reserve; breathe throughout. Skip painful movements and get individual advice for an injury or unfamiliar technique.",style=MaterialTheme.typography.bodySmall)
            RestTimer()
        }
        today.forEach { prescription -> PrescriptionCard(prescription,prefs,entries.any { loggedToday(it,prescription.exerciseId) },preferences,entries,onLog) }
    }
    SectionCard("Your week, planned",icon=Icons.Default.CalendarMonth) {
        Text("Choose a balanced template on this device, or ask your selected AI to arrange allowed exercises around the same preferences. New weeks start on the next Monday, including today if it is Monday.")
        Button(onClick={scope.launch { busy=true;message="";try { val result=repository.week(muscles,false,false);weekJson=Json.encodeToString(result.plan);selectedDay=0;message=result.message }catch(e:CancellationException){throw e}catch(e:Exception){message=e.message?:"Could not build this week."}finally{busy=false} }},enabled=!busy,modifier=Modifier.fillMaxWidth().testTag("trainer-build-week")) { Text("Build week on this device") }
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(share,{share=it},enabled=!busy,modifier=Modifier.testTag("trainer-share"))
            Text("Share my training preferences, age/height/weight and up to 12 comparable recent workout logs with my selected cloud AI.",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)
        }
        Text("No photos, name, email, birthday, medication or migraine journal is included. Your provider handles this request under its own settings.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick={scope.launch {busy=true;message="";try{val result=repository.week(muscles,true,share);weekJson=Json.encodeToString(result.plan);selectedDay=0;message=result.message}catch(e:CancellationException){throw e}catch(e:Exception){message=e.message?:"AI could not plan this week."}finally{busy=false}}},enabled=share&&!busy,modifier=Modifier.fillMaxWidth().testTag("trainer-ai-week")) { Text(if(busy)"Planning…" else "Arrange week with my AI") }
        TextButton(onClick={showPlans=!showPlans}) { Text(if(showPlans)"Hide saved plans" else "Saved plans (${prefs.trainingPlans.size})") }
    }
    draftWeek?.let { week ->
        SectionCard("Review your week") {
            Text("Starts ${week.startDateIso} · ${week.source}",style=MaterialTheme.typography.titleSmall)
            Text(week.note,style=MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { week.days.forEachIndexed { index,d -> FilterChip(selectedDay==index,{selectedDay=index},label={Text(DayOfWeek.of(d.weekday+1).name.take(3))}) } }
            val day=week.days.getOrNull(selectedDay)?:week.days.first()
            Text(day.title+" · "+day.exercises.size+" movements")
            Button(onClick={scope.launch {try{preferences.saveTrainingPlan(week);message="Week saved to this account. Optional Drive/manual settings backups include it."}catch(e:CancellationException){throw e}catch(_:Exception){message="Could not save the plan. Your preview is kept."}}},modifier=Modifier.fillMaxWidth().testTag("trainer-save-week")) { Text("Save this week") }
            TextButton(onClick={weekJson=null}) { Text("Discard preview") }
            Text("Keeps your latest five saved schedules; actual workout history is kept separately.",style=MaterialTheme.typography.bodySmall)
            Text("Logging records what you do today; future planned sessions are not marked complete.",style=MaterialTheme.typography.bodySmall)
        }
        (week.days.getOrNull(selectedDay)?:week.days.first()).exercises.forEach { PrescriptionCard(it,prefs,entries.any { row -> loggedToday(row,it.exerciseId) },preferences,entries,onLog) }
    }
    if(showPlans) prefs.trainingPlans.forEach { week -> SectionCard("Week of ${week.startDateIso}") {
        Text(week.source,style=MaterialTheme.typography.labelMedium)
        week.days.forEach { day ->
            val date=LocalDate.parse(week.startDateIso).plusDays(day.weekday.toLong())
            val logged=day.exercises.count { p -> entries.any { e ->
                (e.details.trainerExerciseId==p.exerciseId || e.exerciseName==TrainerExerciseCatalog.byId(p.exerciseId)?.name) &&
                    Instant.ofEpochMilli(e.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()==date && (e.sets.any { it.completed } || e.details.holdSeconds!=null)
            } }
            Text(date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))+" · "+day.title+" · $logged/${day.exercises.size} logged")
        }
        OutlinedButton(onClick={weekJson=Json.encodeToString(week);selectedDay=0}){Text("Open plan")}
        TextButton(onClick={deleting=week.id}){Text("Remove saved plan")}
    } }
    deleting?.let { id -> AlertDialog(onDismissRequest={deleting=null},title={Text("Remove saved plan?")},text={Text("Removes this saved schedule. Actual workout logs are kept. Existing exported copies are separate.")},confirmButton={TextButton(onClick={scope.launch{preferences.deleteTrainingPlan(id);if(draftWeek?.id==id)weekJson=null;deleting=null}}){Text("Remove")}},dismissButton={TextButton(onClick={deleting=null}){Text("Keep")}}) }
}

private fun loggedToday(e:com.migraineguard.app.core.domain.WorkoutEntry,id:String)=
    (e.details.trainerExerciseId==id || e.exerciseName==TrainerExerciseCatalog.byId(id)?.name) && Instant.ofEpochMilli(e.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()==LocalDate.now() && (e.sets.any { it.completed } || e.details.holdSeconds!=null)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainerProfileEditor(initial:TrainerProfile,onSave:(TrainerProfile)->Unit) {
    var profileJson by rememberSaveable { mutableStateOf(Json.encodeToString(initial)) }
    val p=Json.decodeFromString<TrainerProfile>(profileJson)
    fun change(value:TrainerProfile) { profileJson=Json.encodeToString(value) }
    Text("Experience",style=MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { TrainingExperience.entries.forEach{v->FilterChip(p.experience==v,{change(p.copy(experience=v))},label={Text(v.label)})} }
    Text("Goal",style=MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { TrainingGoal.entries.forEach{v->FilterChip(p.goal==v,{change(p.copy(goal=v))},label={Text(v.label)})} }
    Text("Equipment you can use",style=MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { TrainingEquipment.entries.forEach{v->FilterChip(v==TrainingEquipment.BODYWEIGHT || v in p.equipment,{if(v!=TrainingEquipment.BODYWEIGHT) change(p.copy(equipment=if(v in p.equipment)p.equipment-v else p.equipment+v))},label={Text(v.label)})} }
    Text("Days each week",style=MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { (2..4).forEach{v->FilterChip(p.daysPerWeek==v,{change(p.copy(daysPerWeek=v))},label={Text("$v days")})} }
    Text("Time per session",style=MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf(30,45,60,75).forEach{v->FilterChip(p.minutesPerSession==v,{change(p.copy(minutesPerSession=v))},label={Text("$v min")})} }
    Text("Areas to skip (optional)",style=MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { Muscle.entries.forEach{v->FilterChip(v in p.avoid,{change(p.copy(avoid=if(v in p.avoid)p.avoid-v else p.avoid+v))},label={Text(v.label)})} }
    Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) { Checkbox(p.adultConfirmed,{change(p.copy(adultConfirmed=it))},modifier=Modifier.testTag("trainer-adult"));Text("I am 18 or older. This is general fitness planning, not rehabilitation.",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall) }
    Button(onClick={onSave(p.copy(ready=true))},enabled=p.adultConfirmed,modifier=Modifier.fillMaxWidth().testTag("trainer-save-preferences")){Text("Save training preferences")}
}

@Composable
private fun PrescriptionCard(p:ExercisePrescription,prefs:UserPreferences,logged:Boolean,preferences:UserPreferencesRepository,history:List<com.migraineguard.app.core.domain.WorkoutEntry>,onLog:(ExercisePrescription)->Unit) {
    val e=TrainerExerciseCatalog.byId(p.exerciseId)?:return
    val currentLoad=TrainerEngine.prescription(e,prefs.trainerProfile,prefs.bodyProfile,history)
    val effective=p.copy(loadKg=currentLoad.loadKg,loadReason=currentLoad.loadReason)
    val metric=prefs.bodyProfile.useMetric
    val scope=rememberCoroutineScope()
    var calibration by rememberSaveable(e.id) { mutableStateOf("") }
    var confirmed by remember { mutableStateOf(false) }
    var calibrationMetric by rememberSaveable(e.id) { mutableStateOf(metric) }
    LaunchedEffect(metric) {
        if(calibrationMetric!=metric) { calibration=calibration.toFloatOrNull()?.let { "%.2f".format(java.util.Locale.US,if(metric)it/2.2046226f else it*2.2046226f) }.orEmpty();confirmed=false;calibrationMetric=metric }
    }
    var info by remember { mutableStateOf("") }
    SectionCard(e.name,icon=Icons.Default.FitnessCenter) {
        Text(e.muscles.joinToString { it.label }+if(logged)" · Logged today ✓" else "",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.secondary)
        Text(if(p.holdSeconds!=null)"${p.sets} rounds · ${p.holdSeconds}s hold" else "${p.sets} sets · ${p.minReps}–${p.maxReps} reps"+(if(e.perSide)" each side" else ""),style=MaterialTheme.typography.titleLarge)
        Text("Rest ${p.restSeconds}s · finish with about ${p.targetRir} reps in reserve",style=MaterialTheme.typography.bodySmall)
        Text(if(effective.loadKg==null)"Choose a comfortable test load" else if(effective.loadKg==0f)"Bodyweight / self-resistance" else "Suggested load: %.1f %s".format(if(metric)effective.loadKg else effective.loadKg*2.2046226f,if(metric)"kg" else "lb"),style=MaterialTheme.typography.titleMedium)
        if(e.equipment==TrainingEquipment.DUMBBELLS) Text(if(e.id=="db-squat")"Load is the single dumbbell held at your chest." else "Load is per dumbbell / per hand.",style=MaterialTheme.typography.bodySmall)
        Text(effective.loadReason,style=MaterialTheme.typography.bodySmall)
        Text(e.cue)
        if(e.equipment!=TrainingEquipment.BODYWEIGHT) {
            OutlinedTextField(calibration,{calibration=it.replace(',','.').take(8)},label={Text(if(metric)"Comfortable test load (kg)" else "Comfortable test load (lb)")},singleLine=true,modifier=Modifier.fillMaxWidth().testTag("calibrate-"+e.id))
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {Checkbox(confirmed,{confirmed=it});Text("I tested this load with controlled form and about 3 reps left.",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall)}
            TextButton(onClick={val raw=calibration.toFloatOrNull();val kg=raw?.let{if(metric)it else it/2.2046226f};if(kg==null || !kg.isFinite() || kg !in .1f..500f)info="Enter a valid load." else scope.launch {preferences.saveTrainerProfile(prefs.trainerProfile.copy(testedLoadsKg=prefs.trainerProfile.testedLoadsKg+(e.id to kg),testedLoadsAt=prefs.trainerProfile.testedLoadsAt+(e.id to System.currentTimeMillis())));info="Test load saved. This load suggestion is updated."}},enabled=confirmed){Text("Remember my tested load")}
            if(info.isNotBlank())Text(info,style=MaterialTheme.typography.bodySmall)
        }
        val allowed=prefs.trainerProfile.ready && prefs.trainerProfile.adultConfirmed && (TrainerEngine.age(prefs.bodyProfile)?.let{it>=18}!=false) && TrainerExerciseCatalog.available(prefs.trainerProfile).any{it.id==e.id}
        Button(onClick={onLog(effective)},enabled=allowed,modifier=Modifier.fillMaxWidth().testTag("trainer-log-"+e.id)){Text("Use in exercise log")}
        if(!allowed)Text("Your current preferences exclude this movement. Rebuild your plan.",style=MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RestTimer() {
    var ends by rememberSaveable { mutableLongStateOf(0L) }
    var left by remember { mutableIntStateOf(0) }
    LaunchedEffect(ends) { do { left=if(ends==0L)0 else ((ends-System.currentTimeMillis()+999)/1000).toInt().coerceIn(0,180);if(left>0)delay(250) }while(left>0) }
    Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick={ends=System.currentTimeMillis()+90000}){Icon(Icons.Default.Timer,null);Spacer(Modifier.width(6.dp));Text(if(left>0)"Rest %02d:%02d".format(left/60,left%60) else "Start 90s rest")}
        if(left>0)TextButton(onClick={ends=0}){Text("Skip rest timer")}
    }
}
