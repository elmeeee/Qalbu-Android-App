package app.kamy.qalbuApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R

object PrayerWidgetUpdater {

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, PrayerTimesWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(appContext)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildViews(appContext, snapshot))
        }
    }

    internal fun buildViews(context: Context, snapshot: PrayerWidgetSnapshot?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_times_widget)
        if (snapshot == null) {
            views.setTextViewText(R.id.widget_city, context.getString(R.string.app_name))
            views.setTextViewText(R.id.widget_countdown, "--:--:--")
            views.setTextViewText(R.id.widget_subtitle, context.getString(R.string.prayer_widget_empty))
        } else {
            views.setTextViewText(R.id.widget_city, snapshot.cityLabel)
            views.setTextViewText(R.id.widget_countdown, snapshot.countdown)
            views.setTextViewText(R.id.widget_subtitle, snapshot.subtitle)
        }
        val launchIntent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        return views
    }
}
