package app.kamy.saatApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class DailyVerseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val snapshot = DailyVerseWidgetRenderer.snapshot(context)
        appWidgetIds.forEach { id ->
            runCatching {
                appWidgetManager.updateAppWidget(
                    id,
                    DailyVerseWidgetUpdater.buildViews(context, snapshot)
                )
            }
        }
    }

    override fun onEnabled(context: Context) {
        WidgetCoordinator.onWidgetsEnabled(context)
    }

    override fun onDisabled(context: Context) {
        WidgetCoordinator.onWidgetsDisabled(context)
    }
}
