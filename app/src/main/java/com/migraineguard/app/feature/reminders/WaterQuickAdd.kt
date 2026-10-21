package com.migraineguard.app.feature.reminders

import android.app.PendingIntent
import android.content.*
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.migraineguard.app.core.data.db.AppDatabase
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

object WaterQuickAdd {
    const val ACTION = "com.migraineguard.app.ADD_WATER"
    const val NOTIFICATION_ID = 2000
    fun addActions(context: Context, builder: NotificationCompat.Builder, account: String): NotificationCompat.Builder {
        val token = java.util.UUID.randomUUID().toString()
        context.getSharedPreferences("reminder_owner",Context.MODE_PRIVATE).edit()
            .putString("water_token",token).putLong("water_issued",System.currentTimeMillis()).commit()
        for (amount in 1..3) {
            val intent = Intent(context,WaterActionReceiver::class.java).setAction(ACTION)
                .setData(Uri.parse("migraineguard://water/"+token+"/"+amount))
                .putExtra("account",account).putExtra("token",token).putExtra("amount",amount)
            val pending = PendingIntent.getBroadcast(context,amount,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(NotificationCompat.Action.Builder(0,if(amount==1) "1 glass" else "$amount glasses",pending)
                .setAuthenticationRequired(true).build())
        }
        return builder
    }
    suspend fun apply(context: Context, account: String, token: String, amount: Int, now: Long = System.currentTimeMillis()): Boolean {
        if (amount !in 1..3 || token.length !in 1..100) return false
        val owner = context.getSharedPreferences("reminder_owner",Context.MODE_PRIVATE)
        fun valid() = owner.getString("active_account",null)==account && owner.getString("water_token",null)==token && now-owner.getLong("water_issued",0) in 0..86_400_000L
        if (!valid()) return false
        val prefs = UserPreferencesRepository(context,account).preferences.first()
        if (!prefs.notificationsEnabled || !prefs.waterReminders || !valid()) return false
        val added = AppDatabase.get(context,account).waterActionDao().addOnce(token,LocalDate.now().toString(),amount,now)
        if (owner.getString("water_token",null)==token) {
            owner.edit().remove("water_token").apply()
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
        return added
    }
}

/** Private receiver only enqueues durable local work; no activity, AI, or health permission is needed. */
class WaterActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WaterQuickAdd.ACTION) return
        val account = intent.getStringExtra("account") ?: return
        val token = intent.getStringExtra("token") ?: return
        val amount = intent.getIntExtra("amount",0)
        if (amount !in 1..3 || token.length !in 1..100) return
        val work = OneTimeWorkRequestBuilder<WaterQuickAddWorker>()
            .setInputData(workDataOf("account" to account,"token" to token,"amount" to amount))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()
        WorkManager.getInstance(context).enqueueUniqueWork("water-action-"+token,ExistingWorkPolicy.KEEP,work)
    }
}
class WaterQuickAddWorker(context: Context, params: WorkerParameters): CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        val account = inputData.getString("account") ?: return Result.success()
        val token = inputData.getString("token") ?: return Result.success()
        return try {
            WaterQuickAdd.apply(applicationContext,account,token,inputData.getInt("amount",0)); Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (_: Exception) { if(runAttemptCount < 2) Result.retry() else Result.failure() }
    }
}
