package app.kamy.saatApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetCoordinator {

    fun refreshAll(context: Context) {
        val appContext = context.applicationContext
        PrayerWidgetUpdater.updateAll(appContext)
        PrayerNextWidgetUpdater.updateAll(appContext)
        DailyVerseWidgetUpdater.updateAll(appContext)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                app.kamy.saatApp.infrastructure.widget.glance.PrayerNextGlanceWidget().updateAll(appContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hasAnyWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context.applicationContext)
        return widgetProviders().any { provider ->
            manager.getAppWidgetIds(ComponentName(context.applicationContext, provider)).isNotEmpty()
        }
    }

    fun onWidgetsEnabled(context: Context) {
        refreshAll(context)
        WidgetRefreshScheduler.schedule(context)
    }

    fun onWidgetsDisabled(context: Context) {
        if (!hasAnyWidgets(context)) {
            WidgetRefreshScheduler.cancel(context)
        }
    }

    private fun widgetProviders(): List<Class<*>> = listOf(
        PrayerTimesWidgetProvider::class.java,
        PrayerNextWidgetProvider::class.java,
        DailyVerseWidgetProvider::class.java
    )
}
