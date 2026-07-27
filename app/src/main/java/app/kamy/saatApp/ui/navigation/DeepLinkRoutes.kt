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
        if (chapter > 0) {
            val ayah = intent.getIntExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, -1)
            return if (ayah > 0) "quran/reader/$chapter?ayah=$ayah" else "quran/reader/$chapter?ayah=-1"
        }
        val surahNumber = intent.getIntExtra("surah_number", -1)
        if (surahNumber > 0) {
            return "quran/reader/$surahNumber?ayah=-1"
        }
        return null
    }

    private fun uriRoute(uri: Uri?): String? {
        uri ?: return null
        if (uri.scheme != "saat") return null
        return when (uri.host) {
            "quran" -> {
                val chapter = uri.pathSegments.firstOrNull()?.toIntOrNull()
                    ?: uri.getQueryParameter("chapter")?.toIntOrNull()
                    ?: return RootTab.Quran.route
                val ayah = uri.getQueryParameter("ayah")?.toIntOrNull()
                    ?: uri.getQueryParameter("verse")?.toIntOrNull()
                if (ayah != null && ayah > 0) "quran/reader/$chapter?ayah=$ayah"
                else "quran/reader/$chapter?ayah=-1"
            }
            "mushaf" -> RootTab.Quran.route
            "today" -> RootTab.Today.route
            "bookmarks" -> "quran/bookmarks"
            "qibla" -> "tools/qibla"
            "dhikr" -> "tools/dhikr"
            "qiyam" -> "tools/qiyam"
            "zakat" -> "tools/zakat"
            else -> null
        }
    }
}
