package app.kamy.saatApp.infrastructure.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R

object PrayerNextWidgetUpdater {

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, PrayerNextWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(appContext)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildViews(appContext, snapshot))
        }
    }

    internal fun buildViews(context: Context, snapshot: PrayerWidgetSnapshot?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_next_widget)
        if (snapshot == null) {
            val displayCity = app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore.from(context).displayLabel()
                ?: context.getString(R.string.app_name)
            views.setTextViewText(R.id.prayer_next_city, displayCity)
            views.setTextViewText(R.id.prayer_next_hijri, context.getString(R.string.prayer_schedule))
            views.setTextViewText(R.id.prayer_next_label, context.getString(R.string.prayer_widget_next_title))
            views.setTextViewText(R.id.prayer_next_name, context.getString(R.string.prayer_widget_empty))
            views.setTextViewText(R.id.prayer_next_countdown, "--:--")
            views.setTextViewText(R.id.prayer_next_subtitle, context.getString(R.string.prayer_widget_empty))
        } else {
            views.setTextViewText(R.id.prayer_next_city, snapshot.cityLabel)
            views.setTextViewText(
                R.id.prayer_next_hijri,
                listOfNotNull(snapshot.hijriLabel, snapshot.gregorianLabel)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            )
            views.setTextViewText(R.id.prayer_next_label, snapshot.nextPrayerLabel)
            views.setTextViewText(
                R.id.prayer_next_name,
                "${snapshot.nextPrayerName} · ${snapshot.nextPrayerTime}"
            )
            views.setTextViewText(R.id.prayer_next_countdown, snapshot.countdown)
            views.setTextViewText(
                R.id.prayer_next_subtitle,
                snapshot.khgtEventTitle?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.prayer_widget_at_time, snapshot.nextPrayerTime)
            )
        }
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.prayer_next_root, pendingIntent)
        return views
    }
}
