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
            .takeIf { it > 0 }
            ?: intent.getIntExtra("chapter", -1).takeIf { it > 0 }
            ?: intent.getIntExtra("chapter_number", -1).takeIf { it > 0 }
            ?: intent.getIntExtra("surah_number", -1).takeIf { it > 0 }
        if (chapter != null && chapter > 0) {
            val ayah = intent.getIntExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, -1)
                .takeIf { it > 0 }
                ?: intent.getIntExtra("ayah", -1).takeIf { it > 0 }
                ?: intent.getIntExtra("ayah_number", -1).takeIf { it > 0 }
                ?: intent.getIntExtra("verse", -1).takeIf { it > 0 }
            return if (ayah != null && ayah > 0) "quran/reader/$chapter?ayah=$ayah" else "quran/reader/$chapter?ayah=-1"
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
                val ayah = uri.pathSegments.getOrNull(1)?.toIntOrNull()
                    ?: uri.getQueryParameter("ayah")?.toIntOrNull()
                    ?: uri.getQueryParameter("verse")?.toIntOrNull()
                if (ayah != null && ayah > 0) "quran/reader/$chapter?ayah=$ayah"
                else "quran/reader/$chapter?ayah=-1"
            }
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
