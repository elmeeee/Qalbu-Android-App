package app.kamy.qalbuApp.infrastructure.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.domain.model.PrayerType

object PrayerWidgetUpdater {

    private const val COLOR_ACTIVE = 0xFFD4AF37.toInt()
    private const val COLOR_ACTIVE_LABEL = 0xFFE8D5A3.toInt()
    private const val COLOR_INACTIVE = 0xFFFFFFFF.toInt()
    private const val COLOR_INACTIVE_LABEL = 0xC7FFFFFF.toInt()

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
            views.setTextViewText(R.id.widget_hijri, "")
            views.setTextViewText(R.id.widget_next_line, context.getString(R.string.prayer_widget_empty))
            views.setTextViewText(R.id.widget_countdown, "--:--")
            bindSlot(views, PrayerType.FAJR, context.getString(R.string.prayer_fajr), "--:--", false)
            bindSlot(views, PrayerType.DHUHR, context.getString(R.string.prayer_dhuhr), "--:--", false)
            bindSlot(views, PrayerType.ASR, context.getString(R.string.prayer_asr), "--:--", false)
            bindSlot(views, PrayerType.MAGHRIB, context.getString(R.string.prayer_maghrib), "--:--", false)
            bindSlot(views, PrayerType.ISHA, context.getString(R.string.prayer_isha), "--:--", false)
        } else {
            views.setTextViewText(R.id.widget_city, snapshot.cityLabel)
            views.setTextViewText(
                R.id.widget_hijri,
                listOfNotNull(snapshot.hijriLabel, snapshot.gregorianLabel)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            )
            val subtitle = snapshot.khgtEventTitle?.takeIf { it.isNotBlank() }
                ?: snapshot.nextPrayerLabel
            views.setTextViewText(R.id.widget_next_line, subtitle)
            views.setTextViewText(R.id.widget_countdown, snapshot.countdownCompact)
            snapshot.slots.forEach { slot ->
                bindSlot(views, slot.type, slot.label, slot.time, slot.isActive)
            }
        }
        val launchIntent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
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

    private fun bindSlot(
        views: RemoteViews,
        type: PrayerType,
        label: String,
        time: String,
        active: Boolean
    ) {
        val (slotId, labelId, timeId) = slotIds(type)
        views.setInt(
            slotId,
            "setBackgroundResource",
            if (active) R.drawable.widget_slot_active_background else R.drawable.widget_slot_background
        )
        views.setTextViewText(labelId, label)
        views.setTextViewText(timeId, time)
        views.setTextColor(labelId, if (active) COLOR_ACTIVE_LABEL else COLOR_INACTIVE_LABEL)
        views.setTextColor(timeId, if (active) COLOR_ACTIVE else COLOR_INACTIVE)
    }

    private fun slotIds(type: PrayerType): Triple<Int, Int, Int> = when (type) {
        PrayerType.FAJR -> Triple(R.id.widget_slot_fajr, R.id.widget_fajr_label, R.id.widget_fajr_time)
        PrayerType.DHUHR -> Triple(R.id.widget_slot_dhuhr, R.id.widget_dhuhr_label, R.id.widget_dhuhr_time)
        PrayerType.ASR -> Triple(R.id.widget_slot_asr, R.id.widget_asr_label, R.id.widget_asr_time)
        PrayerType.MAGHRIB -> Triple(R.id.widget_slot_maghrib, R.id.widget_maghrib_label, R.id.widget_maghrib_time)
        PrayerType.ISHA -> Triple(R.id.widget_slot_isha, R.id.widget_isha_label, R.id.widget_isha_time)
        PrayerType.SUNRISE -> Triple(R.id.widget_slot_fajr, R.id.widget_fajr_label, R.id.widget_fajr_time)
    }
}
