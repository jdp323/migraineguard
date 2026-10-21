package com.migraineguard.app.feature.reminders
import com.migraineguard.app.core.data.prefs.UserPreferences
import org.junit.Assert.*
import org.junit.Test
class ReminderPolicyTest {
    @Test fun activeHoursHandleOvernight(){assertTrue(ReminderPolicy.activeHour(23,22,6));assertTrue(ReminderPolicy.activeHour(2,22,6));assertFalse(ReminderPolicy.activeHour(12,22,6));assertFalse(ReminderPolicy.activeHour(22,8,22))}
    @Test fun eachKindKeepsItsOwnIntervalAndToggle(){val p=ReminderPolicy.plan(UserPreferences(foodReminders=true,waterReminderMinutes=30,foodReminderMinutes=240,updateChecksEnabled=false));assertEquals(30,p.intervals["water"]);assertEquals(240,p.intervals["food"]);assertFalse(p.intervals.containsKey("medication"));assertFalse(p.intervals.containsKey("update"));assertFalse(ReminderPolicy.plan(UserPreferences(notificationsEnabled=false)).enabled)}
    @Test fun tapsOpenRelevantDestinations(){assertEquals("food",ReminderPolicy.route("food"));assertEquals("water",ReminderPolicy.route("water"));assertEquals("settings",ReminderPolicy.route("update"))}
}
