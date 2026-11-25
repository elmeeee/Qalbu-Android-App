package app.kamy.saatApp.infrastructure.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FastingNotificationSchedulerTest {

    @Test
    fun catchUp_returnsFalse_whenDisabled() {
        assertFalse(
            RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = 10,
                minuteNow = 0,
                targetHour = 8,
                targetMinute = 30,
                hasShownToday = false,
                isEnabled = false
            )
        )
    }

    @Test
    fun catchUp_returnsFalse_whenAlreadyShownToday() {
        assertFalse(
            RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = 10,
                minuteNow = 0,
                targetHour = 8,
                targetMinute = 30,
                hasShownToday = true,
                isEnabled = true
            )
        )
    }

    @Test
    fun catchUp_returnsFalse_beforeTargetMorningTime() {
        assertFalse(
            RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = 8,
                minuteNow = 29,
                targetHour = 8,
                targetMinute = 30,
                hasShownToday = false,
                isEnabled = true
            )
        )
    }

    @Test
    fun catchUp_returnsTrue_atOrAfterTargetMorningTime() {
        assertTrue(
            RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = 8,
                minuteNow = 30,
                targetHour = 8,
                targetMinute = 30,
                hasShownToday = false,
                isEnabled = true
            )
        )
        assertTrue(
            RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = 14,
                minuteNow = 15,
                targetHour = 8,
                targetMinute = 30,
                hasShownToday = false,
                isEnabled = true
            )
        )
    }

    @Test
    fun nextWeekdayTime_schedulesSameDayEvening_whenBeforeTargetHour() {
        val wednesdayMorning = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 9, 50, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val fireAt = PrayerNotificationScheduler.nextWeekdayTime(
            weekday = Calendar.WEDNESDAY,
            hour = 20,
            minute = 0,
            from = wednesdayMorning
        )

        val resultCal = Calendar.getInstance().apply { timeInMillis = fireAt }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, resultCal.get(Calendar.MONTH))
        assertEquals(25, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun nextWeekdayTime_rollsToNextWeek_whenAfterTargetHour() {
        val wednesdayNight = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 20, 5, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val fireAt = PrayerNotificationScheduler.nextWeekdayTime(
            weekday = Calendar.WEDNESDAY,
            hour = 20,
            minute = 0,
            from = wednesdayNight
        )

        val resultCal = Calendar.getInstance().apply { timeInMillis = fireAt }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, resultCal.get(Calendar.MONTH))
        assertEquals(2, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun upcomingWeeklyOccurrences_returnsExpectedWeeklySpacing() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 25, 9, 50, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val firstSunday = PrayerNotificationScheduler.nextWeekdayTime(
            weekday = Calendar.SUNDAY,
            hour = 20,
            minute = 0,
            from = now
        )
        val occurrences = PrayerNotificationScheduler.upcomingWeeklyOccurrences(
            firstFireAt = firstSunday,
            now = now,
            count = 2
        )

        assertEquals(2, occurrences.size)
        val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L
        assertEquals(sevenDaysMillis, occurrences[1] - occurrences[0])
    }
}
