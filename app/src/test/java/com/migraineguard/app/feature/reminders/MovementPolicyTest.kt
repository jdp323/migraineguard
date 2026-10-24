package com.migraineguard.app.feature.reminders
import com.migraineguard.app.core.data.prefs.UserPreferences
import org.junit.Assert.*
import org.junit.Test
class MovementPolicyTest {
    @Test fun activeStepsSuppressButMissingDataDoesNotPretendSitting() {
        assertFalse(MovementPolicy.shouldRemind(20)); assertFalse(MovementPolicy.shouldRemind(300))
        assertTrue(MovementPolicy.shouldRemind(0)); assertTrue(MovementPolicy.shouldRemind(null))
        assertFalse(MovementPolicy.message(null,60).contains("steps"))
        assertTrue(MovementPolicy.message(0,90).contains("90 minutes"))
    }
    @Test fun toggleIntervalMasterAndOvernightHoursAreRespected() {
        val prefs = UserPreferences(movementReminders=true,movementReminderMinutes=90,notificationsEnabled=false)
        val plan = ReminderPolicy.plan(prefs)
        assertFalse(plan.enabled); assertEquals(90,plan.intervals["movement"])
        assertFalse(ReminderPolicy.plan(prefs.copy(movementReminders=false)).intervals.containsKey("movement"))
        assertTrue(ReminderPolicy.activeHour(23,22,8)); assertFalse(ReminderPolicy.activeHour(12,22,8))
        assertEquals(com.migraineguard.app.navigation.Routes.Track,ReminderPolicy.route("movement"))
    }
}
