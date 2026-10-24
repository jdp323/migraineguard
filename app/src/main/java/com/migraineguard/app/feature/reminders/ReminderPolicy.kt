package com.migraineguard.app.feature.reminders

import com.migraineguard.app.core.data.prefs.UserPreferences

data class ReminderPlan(val enabled: Boolean, val startHour: Int, val endHour: Int, val intervals: Map<String,Int>)
object ReminderPolicy {
    fun activeHour(hour: Int, start: Int, end: Int): Boolean = if(start<end) hour>=start&&hour<end else hour>=start||hour<end
    fun plan(p: UserPreferences) = ReminderPlan(p.notificationsEnabled,p.reminderStartHour,p.reminderEndHour,buildMap {
        if(p.waterReminders)put("water",p.waterReminderMinutes)
        if(p.movementReminders)put("movement",p.movementReminderMinutes)
        if(p.foodReminders)put("food",p.foodReminderMinutes)
        if(p.medicationReminders)put("medication",p.medicationReminderMinutes)
        if(p.migraineReminders)put("migraine",p.migraineReminderMinutes)
        if(p.updateChecksEnabled)put("update",60)
    })
    fun route(kind: String): String = when(kind){"water"->"water";"food"->"food";"movement"->com.migraineguard.app.navigation.Routes.Track;"medication"->"medications";"migraine"->"migraine";else->"settings"}
}
