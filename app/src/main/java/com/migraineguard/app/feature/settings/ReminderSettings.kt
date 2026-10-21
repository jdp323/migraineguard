package com.migraineguard.app.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.data.prefs.*
import com.migraineguard.app.core.ui.SectionCard
import com.migraineguard.app.feature.reminders.ReminderWorker
import kotlinx.coroutines.launch

@Composable
private fun ToggleRow(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text(label,Modifier.weight(1f));Switch(checked=checked,onCheckedChange=onChange)}}
@Composable
private fun IntervalPicker(label:String,value:Int,onChange:(Int)->Unit){
    var expanded by remember{mutableStateOf(false)}
    Box{OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(label+": every "+value+" min ▾")};DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){listOf(15,30,45,60,90,120,180,240,360,720,1440).forEach{n->DropdownMenuItem(text={Text(if(n==1440)"Every day" else "Every "+n+" minutes")},onClick={expanded=false;onChange(n)})}}}
}
@Composable
fun ReminderSettings(repository:UserPreferencesRepository){
    val prefs by repository.preferences.collectAsStateWithLifecycle(UserPreferences());val scope=rememberCoroutineScope();val context=LocalContext.current
    var permissionMessage by remember{mutableStateOf("")}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ok->permissionMessage=if(ok)"Notification permission granted." else "Notifications are blocked by Android. Your saved preferences are kept."}
    fun saveIntervals(water:Int=prefs.waterReminderMinutes,food:Int=prefs.foodReminderMinutes,med:Int=prefs.medicationReminderMinutes,migraine:Int=prefs.migraineReminderMinutes,foodOn:Boolean=prefs.foodReminders,start:Int=prefs.reminderStartHour,end:Int=prefs.reminderEndHour){scope.launch{repository.setReminderOptions(foodOn,water,food,med,migraine,start,end)}}
    SectionCard("Notification controls"){
        ToggleRow("Enable notifications",prefs.notificationsEnabled){scope.launch{repository.setNotificationsEnabled(it)};if(it&&Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS)}
        Text(if(ReminderWorker.canPostNotifications(context))"Android allows notifications." else "Android currently blocks notifications for this app.",style=MaterialTheme.typography.bodySmall)
        if(permissionMessage.isNotBlank())Text(permissionMessage)
        OutlinedButton(onClick={if(Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS)else permissionMessage="Check Android's app notification settings."}){Text("Check notification permission")}
        ToggleRow("Drinking water",prefs.waterReminders){scope.launch{repository.setWaterReminders(it)}}
        if(prefs.waterReminders)IntervalPicker("Water",prefs.waterReminderMinutes){saveIntervals(water=it)}
        ToggleRow("Log meals / food photos",prefs.foodReminders){saveIntervals(foodOn=it)}
        if(prefs.foodReminders)IntervalPicker("Food",prefs.foodReminderMinutes){saveIntervals(food=it)}
        ToggleRow("Medication check-in",prefs.medicationReminders){scope.launch{repository.setMedicationReminders(it)}}
        if(prefs.medicationReminders)IntervalPicker("Medication",prefs.medicationReminderMinutes){saveIntervals(med=it)}
        ToggleRow("Migraine journal check-in",prefs.migraineReminders){scope.launch{repository.setMigraineReminders(it)}}
        if(prefs.migraineReminders)IntervalPicker("Migraine",prefs.migraineReminderMinutes){saveIntervals(migraine=it)}
    }
    SectionCard("Active hours"){
        Text("Reminders are allowed from "+"%02d:00".format(prefs.reminderStartHour)+" to "+"%02d:00".format(prefs.reminderEndHour)+" in this phone's timezone.")
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            TextButton(onClick={saveIntervals(start=8,end=22)}){Text("08:00–22:00")}
            TextButton(onClick={saveIntervals(start=9,end=21)}){Text("09:00–21:00")}
        }
        var start by rememberSaveable{mutableStateOf("")};var end by rememberSaveable{mutableStateOf("")};var error by remember{mutableStateOf("")}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedTextField(value=start,onValueChange={start=it.filter(Char::isDigit).take(2)},label={Text("Start hour 0–23")},modifier=Modifier.weight(1f),singleLine=true)
            OutlinedTextField(value=end,onValueChange={end=it.filter(Char::isDigit).take(2)},label={Text("End hour 0–23")},modifier=Modifier.weight(1f),singleLine=true)
        }
        TextButton(onClick={val a=start.toIntOrNull();val b=end.toIntOrNull();if(a==null||b==null||a !in 0..23||b !in 0..23||a==b)error="Choose different hours from 0 to 23."else{saveIntervals(start=a,end=b);error="Hours saved."}}){Text("Save active hours")}
        if(error.isNotBlank())Text(error)
        Text("Android schedules periodic reminders approximately (minimum 15 minutes). Battery saving, force-stop and notification settings can delay them. These are check-ins, not exact medication alarms. Tapping Food opens the meal journal; no photo is taken or uploaded automatically.",style=MaterialTheme.typography.bodySmall)
    }
    SectionCard("New versions"){
        ToggleRow("Check GitHub daily for updates",prefs.updateChecksEnabled){scope.launch{repository.setUpdateChecks(it)}}
        Text("Checks public release metadata while connected. A new version can show a notification with a GitHub download link. The master notification switch applies; manual checks remain in App settings.",style=MaterialTheme.typography.bodySmall)
    }
}
