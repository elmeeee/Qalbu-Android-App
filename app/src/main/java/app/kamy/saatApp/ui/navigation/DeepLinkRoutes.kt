package app.kamy.saatApp.ui.navigation

import android.content.Intent
import android.net.Uri
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler

object DeepLinkRoutes {
    fun fromIntent(intent: Intent?): String? {
        intent ?: return null
        notificationRoute(intent)?.let { return it }
        return uriRoute(intent.data)
    }

    private fun notificationRoute(intent: Intent): String? {
        val chapter = intent.getIntExtra(DailyVerseNotificationScheduler.EXTRA_CHAPTER, -1)
        if (chapter <= 0) return null
        val ayah = intent.getIntExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, -1)
        return if (ayah > 0) "quran/reader/$chapter?ayah=$ayah" else "quran/reader/$chapter?ayah=-1"
    }

    private fun uriRoute(uri: Uri?): String? {
        uri ?: return null
        if (uri.scheme != "saat") return null
        return when (uri.host) {
            "quran" -> {
                val chapter = uri.pathSegments.firstOrNull()?.toIntOrNull()
                    ?: return RootTab.Quran.route
                val ayah = uri.getQueryParameter("ayah")?.toIntOrNull()
                if (ayah != null && ayah > 0) "quran/reader/$chapter?ayah=$ayah"
                else "quran/reader/$chapter?ayah=-1"
            }
            "mushaf" -> RootTab.Quran.route
            "today" -> RootTab.Today.route
            "reflect" -> RootTab.Reflect.route
            "bookmarks" -> "quran/bookmarks"
            "qibla" -> "tools/qibla"
            "dhikr" -> "tools/dhikr"
            "qiyam" -> "tools/qiyam"
            "zakat" -> "tools/zakat"
            else -> null
        }
    }
}
