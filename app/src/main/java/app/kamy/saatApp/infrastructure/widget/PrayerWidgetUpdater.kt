package app.kamy.saatApp.infrastructure.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R

object PrayerWidgetUpdater {

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, PrayerWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return
        val snapshot = PrayerWidgetRenderer.snapshot(appContext)
        ids.forEach { id ->
            manager.updateAppWidget(id, buildViews(appContext, snapshot))
        }
    }

    internal fun buildViews(context: Context, snapshot: PrayerWidgetSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.prayer_widget_large)
        views.setTextViewText(R.id.widget_title, snapshot.title)
        views.setTextViewText(R.id.widget_subtitle, snapshot.fullSubtitle)

        val slotContainers = listOf(
            R.id.widget_slot_1,
            R.id.widget_slot_2,
            R.id.widget_slot_3,
            R.id.widget_slot_4,
            R.id.widget_slot_5
        )
        val slotNames = listOf(
            R.id.widget_slot_1_name,
            R.id.widget_slot_2_name,
            R.id.widget_slot_3_name,
            R.id.widget_slot_4_name,
            R.id.widget_slot_5_name
        )
        val slotTimes = listOf(
            R.id.widget_slot_1_time,
            R.id.widget_slot_2_time,
            R.id.widget_slot_3_time,
            R.id.widget_slot_4_time,
            R.id.widget_slot_5_time
        )
        val slotStatuses = listOf(
            R.id.widget_slot_1_status,
            R.id.widget_slot_2_status,
            R.id.widget_slot_3_status,
            R.id.widget_slot_4_status,
            R.id.widget_slot_5_status
        )

        val activeBg = R.drawable.bg_widget_active_slot
        val inactiveBg = R.drawable.bg_widget_slots_container

        snapshot.slots.take(5).forEachIndexed { index, slot ->
            val containerId = slotContainers[index]
            val nameId = slotNames[index]
            val timeId = slotTimes[index]
            val statusId = slotStatuses[index]

            views.setTextViewText(nameId, slot.label)
            views.setTextViewText(timeId, slot.time)

            if (slot.isActive) {
                views.setInt(containerId, "setBackgroundResource", activeBg)
                views.setTextColor(nameId, Color.parseColor("#E0EDE7"))
                views.setTextColor(timeId, Color.WHITE)
                views.setTextViewTextSize(timeId, TypedValue.COMPLEX_UNIT_SP, 16.5f)
                views.setViewVisibility(statusId, View.VISIBLE)
                views.setTextViewText(statusId, slot.statusText ?: "Segera")
                views.setTextColor(statusId, Color.parseColor("#E0EDE7"))
            } else {
                views.setInt(containerId, "setBackgroundResource", inactiveBg)
                views.setTextColor(nameId, Color.parseColor("#234939"))
                views.setTextColor(timeId, Color.parseColor("#103B2B"))
                views.setTextViewTextSize(timeId, TypedValue.COMPLEX_UNIT_SP, 14.5f)
                views.setViewVisibility(statusId, View.GONE)
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.prayer_widget_root, pendingIntent)
        return views
    }
}
