package com.migraineguard.app.feature.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.migraineguard.app.R
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesRepository(applicationContext).preferences.first()
        if (!prefs.notificationsEnabled) return Result.success()
        val messages = mutableListOf<String>()
        if (prefs.waterReminders) messages += "Time for a glass of water — hydration helps migraines."
        if (prefs.medicationReminders) messages += "Medication check-in: mark doses taken in MigraineGuard."
        if (prefs.migraineReminders) messages += "Quick check: any migraine symptoms to log today?"
        if (messages.isEmpty()) return Result.success()
        ensureChannel(applicationContext)
        if (!canPostNotifications(applicationContext)) return Result.success()
        val nm = NotificationManagerCompat.from(applicationContext)
        messages.forEachIndexed { index, text ->
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            runCatching { nm.notify(1000 + index, notification) }
        }
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "migraineguard_reminders"
        private const val UNIQUE_WORK = "migraineguard_periodic_reminders"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Health reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
                val mgr = context.getSystemService(NotificationManager::class.java)
                mgr?.createNotificationChannel(channel)
            }
        }

        fun canPostNotifications(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < 33) return true
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun schedule(context: Context, enabled: Boolean) {
            val wm = WorkManager.getInstance(context)
            if (!enabled) {
                wm.cancelUniqueWork(UNIQUE_WORK)
                return
            }
            ensureChannel(context)
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
                .build()
            wm.enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
