package com.migraineguard.app.feature.reminders

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.migraineguard.app.*
import com.migraineguard.app.feature.settings.AppUpdates
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext:Context,params:WorkerParameters):CoroutineWorker(appContext,params){
    override suspend fun doWork():Result {
        val app=applicationContext as MigraineGuardApp
        val account=inputData.getString("account")?:return Result.success()
        val kind=inputData.getString("kind")?:return Result.success()
        val owners=applicationContext.getSharedPreferences("reminder_owner",Context.MODE_PRIVATE)
        if(owners.getString("active_account",null)!=account)return Result.success()
        val prefs=com.migraineguard.app.core.data.prefs.UserPreferencesRepository(applicationContext,account).preferences.first()
        val plan=ReminderPolicy.plan(prefs)
        if(kind !in plan.intervals || !plan.enabled)return Result.success()
        if(kind!="update"&&!ReminderPolicy.activeHour(java.time.LocalTime.now().hour,plan.startHour,plan.endHour))return Result.success()
        val movementSteps = if(kind=="movement" && prefs.deviceHealthEnabled) MovementCheck.recentSteps(applicationContext,prefs.movementReminderMinutes) else null
        if(kind=="movement" && !MovementPolicy.shouldRemind(movementSteps)) return Result.success()
        var title=if(kind=="movement") "Time to move" else if(kind=="water") "Time for water" else "Your MigraineGuard reminder"
        var text=when(kind){"water"->"Log what you drank: 1 glass = 250 ml. Choose 1, 2 or 3 below.";"movement"->MovementPolicy.message(movementSteps,prefs.movementReminderMinutes);"food"->"Had a meal? Add a food photo or description to your journal.";"medication"->"Review your medication journal and mark what you took.";"migraine"->"Record any symptoms or triggers you noticed.";else->""}
        var downloadUrl:String?=null
        if(kind=="update"){
            try{
                val cache=applicationContext.getSharedPreferences("public_release_cache",Context.MODE_PRIVATE)
                val cached=AppUpdates.cached(applicationContext)
                val age=System.currentTimeMillis()-cache.getLong("checked",0)
                val release=if(cached!=null&&age in 0..86_399_999L)cached else AppUpdates.check(applicationContext)
                if(!AppUpdates.newer(release.version,BuildConfig.VERSION_NAME))return Result.success()
                if(!ReminderPolicy.activeHour(java.time.LocalTime.now().hour,plan.startHour,plan.endHour))return Result.success()
                if(cache.getString("notified",null)==release.version)return Result.success()
                if(!prefs.notificationsEnabled||!canPostNotifications(applicationContext))return Result.success()
                title="MigraineGuard update available";text=release.version+" is ready. Tap to view the GitHub download page.";downloadUrl=release.url
            }catch(e:CancellationException){throw e}catch(_:Exception){return Result.retry()}
        }
        if(owners.getString("active_account",null)!=account||!canPostNotifications(applicationContext))return Result.success()
        ensureChannel(applicationContext)
        val intent=if(downloadUrl!=null)Intent(Intent.ACTION_VIEW,android.net.Uri.parse(downloadUrl)) else Intent(applicationContext,MainActivity::class.java).putExtra("open_route",ReminderPolicy.route(kind)).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val id=2000+listOf("water","food","medication","migraine","update","movement").indexOf(kind)
        val pending=PendingIntent.getActivity(applicationContext,id,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try{
            val builder = NotificationCompat.Builder(applicationContext,CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(pending).setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            if(kind=="water") WaterQuickAdd.addActions(applicationContext,builder,account)
            NotificationManagerCompat.from(applicationContext).notify(id,builder.build())
            if(kind=="update")applicationContext.getSharedPreferences("public_release_cache",Context.MODE_PRIVATE).edit().putString("notified",AppUpdates.cached(applicationContext)?.version).apply()
        }catch(_:SecurityException){return Result.success()}
        return Result.success()
    }
    companion object {
        const val CHANNEL_ID="migraineguard_reminders"
        private val kinds=listOf("water","food","medication","migraine","update","movement")
        fun ensureChannel(context:Context){if(Build.VERSION.SDK_INT>=26)context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(NotificationChannel(CHANNEL_ID,"Health & app reminders",NotificationManager.IMPORTANCE_DEFAULT))}
        fun canPostNotifications(context:Context):Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled() && (Build.VERSION.SDK_INT<33||ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
        fun schedule(context:Context,enabled:Boolean){if(!enabled){val wm=WorkManager.getInstance(context);wm.cancelUniqueWork("migraineguard_periodic_reminders");kinds.forEach{wm.cancelUniqueWork("migraineguard_reminder_"+it)}}}
        fun schedule(context:Context,plan:ReminderPlan,account:String){
            val wm=WorkManager.getInstance(context);wm.cancelUniqueWork("migraineguard_periodic_reminders")
            kinds.forEach{kind->
                val interval=plan.intervals[kind]
                if(!plan.enabled||interval==null){wm.cancelUniqueWork("migraineguard_reminder_"+kind)}else{
                    val builder=PeriodicWorkRequestBuilder<ReminderWorker>(interval.coerceIn(15,1440).toLong(),TimeUnit.MINUTES).setInputData(workDataOf("kind" to kind,"account" to account)).setInitialDelay((if(kind=="update")15 else interval).toLong(),TimeUnit.MINUTES)
                    if(kind=="update")builder.setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    wm.enqueueUniquePeriodicWork("migraineguard_reminder_"+kind,ExistingPeriodicWorkPolicy.UPDATE,builder.build())
                }
            }
        }
    }
}
