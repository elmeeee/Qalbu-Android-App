package app.kamy.saatApp.infrastructure.notifications

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactAlarmSchedulerTest {
    @Test
    fun usesExactSchedulingWhenPermissionGrantedOnAndroid12AndAbove() {
        assertTrue(ExactAlarmScheduler.shouldUseExactScheduling(Build.VERSION_CODES.S, true))
    }

    @Test
    fun fallsBackToInexactWhenPermissionMissingOnAndroid12AndAbove() {
        assertFalse(ExactAlarmScheduler.shouldUseExactScheduling(Build.VERSION_CODES.S, false))
    }

    @Test
    fun usesExactSchedulingOnPreAndroid12() {
        assertTrue(ExactAlarmScheduler.shouldUseExactScheduling(Build.VERSION_CODES.R, true))
        assertTrue(ExactAlarmScheduler.shouldUseExactScheduling(Build.VERSION_CODES.R, false))
    }
}
