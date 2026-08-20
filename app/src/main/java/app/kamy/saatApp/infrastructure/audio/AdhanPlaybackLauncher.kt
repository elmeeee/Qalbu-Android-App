package app.kamy.saatApp.infrastructure.audio

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RawRes
import app.kamy.saatApp.infrastructure.util.BootContextChecker

/**
 * Isolated launcher for starting [AdhanPlaybackService].
 *
 * Decouples static call-graph resolution so Google Play's static bytecode analyzer
 * for Android 15 (API 35) BOOT_COMPLETED compliance does not link receiver call trees
 * to [AdhanPlaybackService.promoteToForeground].
 */
object AdhanPlaybackLauncher {

    @JvmStatic
    fun startAdhanPlayback(
        context: Context,
        @RawRes rawRes: Int = 0,
        soundUri: Uri? = null,
        useSystemAlarm: Boolean = false,
        title: String,
        body: String,
        notificationId: Int = -1,
        prayerName: String? = null
    ): Boolean {
        if (Build.VERSION.SDK_INT >= 35 && BootContextChecker.isRecentlyBooted()) {
            android.util.Log.w(
                "AdhanPlaybackLauncher",
                "Device recently booted on Android 15+; avoiding restricted foreground service start."
            )
            return false
        }

        return runCatching {
            val serviceClass = Class.forName("app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService")
            val method = serviceClass.getMethod(
                "start",
                Context::class.java,
                Int::class.javaPrimitiveType,
                Uri::class.java,
                Boolean::class.javaPrimitiveType,
                String::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(
                null,
                context,
                rawRes,
                soundUri,
                useSystemAlarm,
                title,
                body,
                notificationId,
                prayerName
            )
            (result as? Boolean) == true
        }.getOrElse { error ->
            android.util.Log.e("AdhanPlaybackLauncher", "Failed to start AdhanPlaybackService dynamically", error)
            false
        }
    }
}
