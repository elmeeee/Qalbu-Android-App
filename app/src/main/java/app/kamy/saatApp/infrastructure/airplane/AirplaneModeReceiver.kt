package app.kamy.saatApp.infrastructure.airplane

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings

/**
 * Tracks airplane mode state in real-time via [Intent.ACTION_AIRPLANE_MODE_CHANGED].
 *
 * Registered once in [app.kamy.saatApp.SaatApplication] for the app process lifetime.
 * Call [isAirplaneModeOn] before any audio playback to silently skip sound when
 * the device is in airplane mode — visual notifications are unaffected.
 */
class AirplaneModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            cachedAirplaneModeOn = readSystemSetting(context)
        }
    }

    companion object {

        @Volatile
        private var cachedAirplaneModeOn: Boolean = false

        @Volatile
        private var registered: Boolean = false

        private val receiver = AirplaneModeReceiver()

        /**
         * Registers the broadcast receiver and performs an initial state check.
         * Idempotent — safe to call multiple times.
         */
        fun register(context: Context) {
            refresh(context)
            if (registered) return
            val appContext = context.applicationContext
            val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(receiver, filter)
            }
            registered = true
        }

        /**
         * Refreshes the cached state from the system setting.
         */
        fun refresh(context: Context) {
            cachedAirplaneModeOn = readSystemSetting(context)
        }

        /**
         * Returns the current airplane mode status.
         * Uses the real-time cached value maintained by the broadcast receiver,
         * falling back to a live system query if the receiver was never registered.
         */
        fun isAirplaneModeOn(context: Context): Boolean {
            if (!registered) refresh(context)
            return cachedAirplaneModeOn
        }

        private fun readSystemSetting(context: Context): Boolean =
            runCatching {
                Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    0
                ) != 0
            }.getOrDefault(false)
    }
}
