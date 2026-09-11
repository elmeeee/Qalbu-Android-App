package app.kamy.saatApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class PrayerWidgetTimesProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PrayerWidgetUpdater.updateTimes(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetCoordinator.onWidgetsEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetCoordinator.onWidgetsDisabled(context)
    }
}
