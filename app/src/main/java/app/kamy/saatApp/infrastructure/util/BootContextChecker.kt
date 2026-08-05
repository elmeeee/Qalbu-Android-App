package app.kamy.saatApp.infrastructure.util

import android.os.SystemClock

object BootContextChecker {

    private const val BOOT_GRACE_PERIOD_MS = 60_000L

    fun isRecentlyBooted(): Boolean {
        val elapsed = SystemClock.elapsedRealtime()
        return elapsed < BOOT_GRACE_PERIOD_MS
    }
}
