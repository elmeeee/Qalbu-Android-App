package app.kamy.qalbuApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class PrayerTimesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val snapshot = PrayerWidgetRenderer.snapshot(context)
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(
                id,
                PrayerWidgetUpdater.buildViews(context, snapshot)
            )
        }
    }

    override fun onEnabled(context: Context) {
        PrayerWidgetUpdater.updateAll(context)
    }
}
