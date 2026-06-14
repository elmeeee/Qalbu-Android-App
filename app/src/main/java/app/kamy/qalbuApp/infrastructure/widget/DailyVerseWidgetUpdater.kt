package app.kamy.qalbuApp.infrastructure.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler

object DailyVerseWidgetUpdater {

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, DailyVerseWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = DailyVerseWidgetRenderer.snapshot(appContext)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildViews(appContext, snapshot))
        }
    }

    internal fun buildViews(context: Context, snapshot: DailyVerseWidgetSnapshot?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.daily_verse_widget)
        if (snapshot == null) {
            views.setTextViewText(R.id.verse_widget_label, context.getString(R.string.verse_of_day))
            views.setTextViewText(R.id.verse_widget_reference, context.getString(R.string.app_name))
            views.setTextViewText(R.id.verse_widget_excerpt, context.getString(R.string.daily_verse_widget_empty))
        } else {
            views.setTextViewText(R.id.verse_widget_label, snapshot.label)
            views.setTextViewText(R.id.verse_widget_reference, snapshot.reference)
            views.setTextViewText(R.id.verse_widget_excerpt, snapshot.excerpt)
        }
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            snapshot?.let {
                putExtra(DailyVerseNotificationScheduler.EXTRA_CHAPTER, it.chapterNumber)
                putExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, it.ayahNumber)
            }
        }
        val pending = PendingIntent.getActivity(
            context,
            1,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.verse_widget_root, pending)
        return views
    }
}
