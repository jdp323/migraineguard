package com.migraineguard.app.feature.fitness

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Movie
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.luminance
import com.migraineguard.app.core.design.GlassBackdrop
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.migraineguard.app.core.ui.AppTopBar
import com.migraineguard.app.core.ui.SectionCard
import org.json.JSONArray

/** Native GIF animation, independent of Compose's animation clock. One opened demo is decoded at a time. */
@Suppress("DEPRECATION")
internal class ExerciseGifDrawable(private val movie:Movie):Drawable(),Runnable {
    var playing=false; private set
    private var offset=0L
    private var started=0L
    val durationMillis get()=movie.duration().coerceAtLeast(1)
    fun positionMillis():Int=((offset+if(playing)SystemClock.uptimeMillis()-started else 0L)%durationMillis).toInt()
    fun play(){if(!playing){started=SystemClock.uptimeMillis();playing=true;scheduleSelf(this,SystemClock.uptimeMillis());invalidateSelf()}}
    fun pause(){if(playing){offset=positionMillis().toLong();playing=false;unscheduleSelf(this);invalidateSelf()}}
    fun restart(){offset=0;started=SystemClock.uptimeMillis();invalidateSelf()}
    override fun run(){if(playing){invalidateSelf();scheduleSelf(this,SystemClock.uptimeMillis()+100)}}
    override fun draw(canvas:Canvas){
        movie.setTime(positionMillis())
        val scale=minOf(bounds.width().toFloat()/movie.width(),bounds.height().toFloat()/movie.height())
        val save=canvas.save()
        canvas.translate(bounds.left+(bounds.width()-movie.width()*scale)/2,bounds.top+(bounds.height()-movie.height()*scale)/2)
        canvas.scale(scale,scale);movie.draw(canvas,0f,0f);canvas.restoreToCount(save)
    }
    override fun getIntrinsicWidth()=movie.width()
    override fun getIntrinsicHeight()=movie.height()
    override fun setAlpha(alpha:Int){}
    override fun setColorFilter(filter:ColorFilter?){}
    @Deprecated("Deprecated in Java") override fun getOpacity()=PixelFormat.TRANSLUCENT
}

private fun loadDemo(context:android.content.Context,id:String):ExerciseDemo? {
    if(!ExerciseGuideCatalog.hasGif(id))return null
    val json=context.assets.open("exercise_demos/index.json").bufferedReader().use{JSONArray(it.readText())}
    return (0 until json.length()).map{json.getJSONObject(it)}.firstOrNull{it.getString("exerciseId")==id}?.let {
        ExerciseDemo(id,it.getString("file"),it.getString("author"),it.getString("source"))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGuideDialog(exercise:TrainerExercise,onDismiss:()->Unit) {
    val context=LocalContext.current
    val owner=LocalLifecycleOwner.current
    val demo=remember(exercise.id){loadDemo(context,exercise.id)}
    @Suppress("DEPRECATION")
    val drawable=remember(demo?.asset){demo?.let{d->context.assets.open("exercise_demos/"+d.asset).use{Movie.decodeStream(it)}?.let(::ExerciseGifDrawable)}}
    var playing by remember{mutableStateOf(false)}
    var credits by remember{mutableStateOf(false)}
    var sourceError by remember{mutableStateOf("")}
    DisposableEffect(drawable,owner) {
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_STOP){drawable?.pause();playing=false}}
        owner.lifecycle.addObserver(observer)
        onDispose{drawable?.pause();owner.lifecycle.removeObserver(observer)}
    }
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        GlassBackdrop(MaterialTheme.colorScheme.onSurface.luminance() > .5f) {
        Scaffold(topBar={AppTopBar(title={Text("Exercise guide")},navigationIcon={TextButton(onClick=onDismiss,modifier=Modifier.testTag("demo-close")){Text("Close")}})}){padding->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp).testTag("exercise-guide-list"),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)) {
                item { Text(exercise.name,style=MaterialTheme.typography.headlineSmall);Text(exercise.muscles.joinToString{it.label},style=MaterialTheme.typography.labelLarge) }
                if(drawable!=null) {
                    item { AndroidView(factory={ImageView(it).apply{setImageDrawable(drawable);contentDescription=exercise.name+" exercise demonstration"}},modifier=Modifier.fillMaxWidth().height(300.dp).testTag("exercise-gif")) }
                    item {
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            Button(onClick={if(playing)drawable.pause() else drawable.play();playing=!playing},modifier=Modifier.testTag("demo-play")){Text(if(playing)"Pause GIF" else "Play GIF")}
                            OutlinedButton(onClick={drawable.restart()},modifier=Modifier.testTag("demo-restart")){Text("Restart")}
                        }
                        Text(if(playing)"Playing · loops automatically" else "Paused · press Play to watch",modifier=Modifier.testTag("demo-status"),style=MaterialTheme.typography.bodySmall)
                    }
                } else item { Text("Step-by-step guide. A GIF is not yet available for this variation.",style=MaterialTheme.typography.bodySmall) }
                item { SectionCard("Equipment & setup") { Text(ExerciseGuideCatalog.equipment(exercise)) } }
                item { SectionCard("How to do it") { ExerciseGuideCatalog.setup(exercise).forEachIndexed{i,s->Text((i+1).toString()+". "+s)} } }
                item { Text("The demonstration shows movement and equipment, not a load recommendation. Follow your own controlled test load and current workout targets.",style=MaterialTheme.typography.bodySmall) }
                if(demo!=null)item {
                    TextButton(onClick={credits=!credits}){Text(if(credits)"Hide credits" else "Source & license")}
                    if(credits) {
                        Text("Video: "+demo.author+" / wger · CC BY-SA 4.0. Trimmed, resized and converted to GIF. GIF adaptations use the same license.",style=MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(demo.source)))}.onFailure{sourceError="No browser is available. The offline demo and guide still work."}}){Text("Original video")}
                        if(sourceError.isNotBlank())Text(sourceError)
                        TextButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://creativecommons.org/licenses/by-sa/4.0/")))}.onFailure{sourceError="No browser is available. The full license is bundled with the app."}}){Text("View license")}
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable
fun ExerciseLibraryDialog(targets:Set<Muscle>,onUse:(TrainerExercise)->Unit,onDismiss:()->Unit) {
    val keyboard=LocalSoftwareKeyboardController.current
    val focus=LocalFocusManager.current
    var search by rememberSaveable{mutableStateOf("")}
    var equipment by rememberSaveable{mutableStateOf("ALL")}
    var onlyGifs by rememberSaveable{mutableStateOf(false)}
    var selectedOnly by rememberSaveable{mutableStateOf(targets.isNotEmpty())}
    var guide by rememberSaveable{mutableStateOf<String?>(null)}
    val choices=TrainerExerciseCatalog.all.filter{e->
        (search.isBlank()||e.name.contains(search,true)||e.muscles.any{it.label.contains(search,true)})&&
        (equipment=="ALL"||e.equipment.name==equipment)&&(!onlyGifs||ExerciseGuideCatalog.hasGif(e.id))&&
        (!selectedOnly||e.muscles.any{it in targets})
    }
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        GlassBackdrop(MaterialTheme.colorScheme.onSurface.luminance() > .5f) {
        Scaffold(topBar={AppTopBar(title={Text("Exercises & machines")},navigationIcon={TextButton(onClick=onDismiss,modifier=Modifier.testTag("library-close")){Text("Close")}})}){padding->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=18.dp).testTag("exercise-library-list"),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)) {
                item {
                    OutlinedTextField(search,{search=it},label={Text("Search exercise or muscle")},singleLine=true,modifier=Modifier.fillMaxWidth().testTag("exercise-library-search"))
                    FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                        FilterChip(equipment=="ALL",{equipment="ALL"},label={Text("All equipment")})
                        TrainingEquipment.entries.forEach{e->FilterChip(equipment==e.name,{equipment=e.name},label={Text(e.label)})}
                    }
                    Row{Checkbox(onlyGifs,{onlyGifs=it},modifier=Modifier.testTag("library-only-gifs"));Text("Show exercises with GIFs")}
                    if(targets.isNotEmpty())Row{Checkbox(selectedOnly,{selectedOnly=it},modifier=Modifier.testTag("library-selected-only"));Text("Match my selected muscles")}
                    Text(choices.size.toString()+" exercises",style=MaterialTheme.typography.labelLarge)
                }
                if(choices.isEmpty())item{Text("No matching exercises. Try another search or filter.")}
                items(choices,key={it.id}){e->
                    SectionCard(e.name) {
                        Text(e.equipment.label+" · "+e.muscles.joinToString{it.label})
                        Text(e.cue,style=MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick={focus.clearFocus();keyboard?.hide();guide=e.id},modifier=Modifier.testTag("library-guide-"+e.id)){Text(if(ExerciseGuideCatalog.hasGif(e.id))"Steps & GIF" else "View steps")}
                        Button(onClick={focus.clearFocus();keyboard?.hide();onUse(e)},modifier=Modifier.testTag("library-use-"+e.id)){Text("Use in exercise log")}
                    }
                }
            }
        }
        }
    }
    guide?.let{id->TrainerExerciseCatalog.byId(id)?.let{ExerciseGuideDialog(it){guide=null}}}
}
