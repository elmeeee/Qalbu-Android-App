package app.kamy.saatApp.infrastructure.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R

object PrayerWidgetUpdater {

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        updateLarge(appContext)
        updateSmall(appContext)
        updateMedium(appContext)
        updateTimes(appContext)
    }

    fun updateSmall(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PrayerWidgetSmallProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(context)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildSmallViews(context, snapshot))
        }
    }

    fun updateMedium(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PrayerWidgetMediumProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(context)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildMediumViews(context, snapshot))
        }
    }

    fun updateTimes(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PrayerWidgetTimesProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(context)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildTimesViews(context, snapshot))
        }
    }

    fun updateLarge(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PrayerWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(context)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildLargeViews(context, snapshot))
        }
    }

    internal fun buildSmallViews(context: Context, snapshot: PrayerWidgetSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_small)
        when {
            snapshot.isRamadan -> {
                views.setInt(R.id.widget_small_ring_container, "setBackgroundResource", R.drawable.bg_widget_ring_gold)
                views.setImageViewResource(R.id.widget_small_icon, R.drawable.ic_live_ramadan)
                views.setViewVisibility(R.id.widget_small_streak_text, View.GONE)
            }
            snapshot.streakCount > 0 -> {
                views.setInt(R.id.widget_small_ring_container, "setBackgroundResource", R.drawable.bg_widget_ring_streak)
                views.setImageViewResource(R.id.widget_small_icon, R.drawable.ic_widget_water_drop)
                views.setViewVisibility(R.id.widget_small_streak_text, View.VISIBLE)
                views.setTextViewText(R.id.widget_small_streak_text, snapshot.streakCount.toString())
            }
            else -> {
                views.setInt(R.id.widget_small_ring_container, "setBackgroundResource", R.drawable.bg_widget_ring_emerald)
                views.setImageViewResource(R.id.widget_small_icon, R.drawable.ic_live_mosque)
                views.setViewVisibility(R.id.widget_small_streak_text, View.GONE)
            }
        }
        views.setOnClickPendingIntent(R.id.prayer_widget_small_root, openAppPendingIntent(context))
        return views
    }

    internal fun buildMediumViews(context: Context, snapshot: PrayerWidgetSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_medium)

        when {
            // During prayer time
            snapshot.isArrived -> {
                views.setImageViewResource(R.id.widget_med_icon, R.drawable.ic_live_mosque)
                views.setViewVisibility(R.id.widget_med_state_countdown, View.GONE)
                views.setViewVisibility(R.id.widget_med_state_arrived, View.VISIBLE)
                views.setViewVisibility(R.id.widget_med_state_finished, View.GONE)
                views.setViewVisibility(R.id.widget_med_arrived_bell, View.VISIBLE)

                views.setTextViewText(R.id.widget_med_arrived_name, snapshot.statePrayerName)
                views.setTextViewText(R.id.widget_med_arrived_time, snapshot.stateTimeStr)
            }
            // After prayer time
            snapshot.isFinished -> {
                views.setImageViewResource(R.id.widget_med_icon, if (snapshot.isRamadan) R.drawable.ic_live_ramadan else R.drawable.ic_live_check)
                views.setViewVisibility(R.id.widget_med_state_countdown, View.GONE)
                views.setViewVisibility(R.id.widget_med_state_arrived, View.GONE)
                views.setViewVisibility(R.id.widget_med_state_finished, View.VISIBLE)
                views.setViewVisibility(R.id.widget_med_arrived_bell, View.GONE)

                views.setTextViewText(R.id.widget_med_finished_title, snapshot.stateTitle)
                views.setTextViewText(R.id.widget_med_finished_next, "${snapshot.nextPrayerName} • ${snapshot.nextPrayerTimeStr}")
            }
            // Ramadan Iftar Countdown
            snapshot.isRamadan && snapshot.nextPrayerName.contains("Maghrib", ignoreCase = true) -> {
                views.setImageViewResource(R.id.widget_med_icon, R.drawable.ic_live_ramadan)
                views.setViewVisibility(R.id.widget_med_state_countdown, View.VISIBLE)
                views.setViewVisibility(R.id.widget_med_state_arrived, View.GONE)
                views.setViewVisibility(R.id.widget_med_state_finished, View.GONE)
                views.setViewVisibility(R.id.widget_med_arrived_bell, View.GONE)

                views.setTextViewText(R.id.widget_med_countdown_title, snapshot.stateTitle)
                views.setChronometer(R.id.widget_med_countdown_time, snapshot.chronometerBase, null, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    views.setChronometerCountDown(R.id.widget_med_countdown_time, true)
                }
                views.setTextViewText(R.id.widget_med_countdown_sub, "Iftar • ${snapshot.iftarTimeStr}\nImsak • ${snapshot.imsakTimeStr}")
            }
            // Default Next Prayer Countdown
            else -> {
                views.setImageViewResource(R.id.widget_med_icon, R.drawable.ic_live_mosque)
                views.setViewVisibility(R.id.widget_med_state_countdown, View.VISIBLE)
                views.setViewVisibility(R.id.widget_med_state_arrived, View.GONE)
                views.setViewVisibility(R.id.widget_med_state_finished, View.GONE)
                views.setViewVisibility(R.id.widget_med_arrived_bell, View.GONE)

                views.setTextViewText(R.id.widget_med_countdown_title, snapshot.nextPrayerName)
                views.setChronometer(R.id.widget_med_countdown_time, snapshot.chronometerBase, null, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    views.setChronometerCountDown(R.id.widget_med_countdown_time, true)
                }
                views.setTextViewText(R.id.widget_med_countdown_sub, snapshot.nextPrayerTimeStr)
            }
        }

        views.setOnClickPendingIntent(R.id.prayer_widget_medium_root, openAppPendingIntent(context))
        return views
    }

    internal fun buildTimesViews(context: Context, snapshot: PrayerWidgetSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_times)

        val iconIds = listOf(
            R.id.widget_times_icon_1, R.id.widget_times_icon_2, R.id.widget_times_icon_3,
            R.id.widget_times_icon_4, R.id.widget_times_icon_5, R.id.widget_times_icon_6
        )
        val nameIds = listOf(
            R.id.widget_times_name_1, R.id.widget_times_name_2, R.id.widget_times_name_3,
            R.id.widget_times_name_4, R.id.widget_times_name_5, R.id.widget_times_name_6
        )
        val timeIds = listOf(
            R.id.widget_times_time_1, R.id.widget_times_time_2, R.id.widget_times_time_3,
            R.id.widget_times_time_4, R.id.widget_times_time_5, R.id.widget_times_time_6
        )

        val activeColor = Color.parseColor("#15AA7C")
        val inactiveNameColor = Color.parseColor("#D1D5DB")
        val inactiveTimeColor = Color.WHITE

        snapshot.slots.forEachIndexed { i, slot ->
            if (i < 6) {
                views.setImageViewResource(iconIds[i], slot.iconRes)
                views.setTextViewText(nameIds[i], slot.label)
                views.setTextViewText(timeIds[i], slot.time)

                if (slot.isActive) {
                    views.setTextColor(nameIds[i], activeColor)
                    views.setTextColor(timeIds[i], activeColor)
                } else {
                    views.setTextColor(nameIds[i], inactiveNameColor)
                    views.setTextColor(timeIds[i], inactiveTimeColor)
                }
            }
        }

        views.setOnClickPendingIntent(R.id.prayer_widget_times_root, openAppPendingIntent(context))
        return views
    }

    internal fun buildLargeViews(context: Context, snapshot: PrayerWidgetSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_large)

        // Top Row
        val iconRes = if (snapshot.isRamadan) R.drawable.ic_live_ramadan else R.drawable.ic_live_mosque
        val ringRes = if (snapshot.isRamadan) R.drawable.bg_widget_ring_gold else R.drawable.bg_widget_ring_emerald

        views.setInt(R.id.widget_large_ring_container, "setBackgroundResource", ringRes)
        views.setImageViewResource(R.id.widget_large_icon, iconRes)

        val topTitle = if (snapshot.isRamadan && snapshot.nextPrayerName.contains("Maghrib", ignoreCase = true)) {
            snapshot.stateTitle
        } else {
            "Menuju ${snapshot.nextPrayerName}"
        }

        views.setTextViewText(R.id.widget_large_countdown_title, topTitle)
        views.setChronometer(R.id.widget_large_countdown_time, snapshot.chronometerBase, null, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            views.setChronometerCountDown(R.id.widget_large_countdown_time, true)
        }
        views.setTextViewText(R.id.widget_large_countdown_sub, snapshot.nextPrayerTimeStr)

        // Bottom 6 slots
        val iconIds = listOf(
            R.id.widget_large_icon_1, R.id.widget_large_icon_2, R.id.widget_large_icon_3,
            R.id.widget_large_icon_4, R.id.widget_large_icon_5, R.id.widget_large_icon_6
        )
        val nameIds = listOf(
            R.id.widget_large_name_1, R.id.widget_large_name_2, R.id.widget_large_name_3,
            R.id.widget_large_name_4, R.id.widget_large_name_5, R.id.widget_large_name_6
        )
        val timeIds = listOf(
            R.id.widget_large_time_1, R.id.widget_large_time_2, R.id.widget_large_time_3,
            R.id.widget_large_time_4, R.id.widget_large_time_5, R.id.widget_large_time_6
        )

        val activeColor = Color.parseColor("#15AA7C")
        val inactiveNameColor = Color.parseColor("#D1D5DB")
        val inactiveTimeColor = Color.WHITE

        snapshot.slots.forEachIndexed { i, slot ->
            if (i < 6) {
                views.setImageViewResource(iconIds[i], slot.iconRes)
                views.setTextViewText(nameIds[i], slot.label)
                views.setTextViewText(timeIds[i], slot.time)

                if (slot.isActive) {
                    views.setTextColor(nameIds[i], activeColor)
                    views.setTextColor(timeIds[i], activeColor)
                } else {
                    views.setTextColor(nameIds[i], inactiveNameColor)
                    views.setTextColor(timeIds[i], inactiveTimeColor)
                }
            }
        }

        views.setOnClickPendingIntent(R.id.prayer_widget_root, openAppPendingIntent(context))
        return views
    }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
