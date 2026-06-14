package app.kamy.qalbuApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class DailyVerseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        DailyVerseWidgetUpdater.updateAll(context)
    }

    override fun onEnabled(context: Context) {
        DailyVerseWidgetUpdater.updateAll(context)
    }
}
