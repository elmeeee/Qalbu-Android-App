package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persists the last fetched timetable so toggles can reschedule without a new location fetch. */
object PrayerScheduleCache {
    private const val PREFS = "qalbu_prayer_schedule_cache"
    private const val KEY_JSON = "bundle_json"

    fun save(context: Context, bundle: PrayerScheduleBundle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, bundle.toJson().toString())
            .apply()
    }

    fun load(context: Context): PrayerScheduleBundle? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_JSON, null)
            ?: return null
        return runCatching { parseBundle(JSONObject(raw)) }.getOrNull()
    }

    private fun parseBundle(o: JSONObject): PrayerScheduleBundle {
        val adzan = buildList {
            val arr = o.getJSONArray("adzan")
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                add(
                    PrayerNotificationItem(
                        name = item.getString("name"),
                        fireAtMillis = item.getLong("fireAt")
                    )
                )
            }
        }
        val imsak = if (o.has("imsak") && !o.isNull("imsak")) {
            val item = o.getJSONObject("imsak")
            PrayerNotificationItem(item.getString("name"), item.getLong("fireAt"))
        } else null
        val night = buildList {
            val arr = o.getJSONArray("night")
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                add(
                    NightDivisionItem(
                        kind = NightDivisionKind.valueOf(item.getString("kind")),
                        fireAtMillis = item.getLong("fireAt")
                    )
                )
            }
        }
        return PrayerScheduleBundle(
            adzanPrayers = adzan,
            imsak = imsak,
            nightDivisions = night,
            dayKey = o.getString("dayKey")
        )
    }

    private fun PrayerScheduleBundle.toJson(): JSONObject = JSONObject().apply {
        put("dayKey", dayKey)
        put("adzan", JSONArray().apply {
            adzanPrayers.forEach { put(it.toJson()) }
        })
        imsak?.let { put("imsak", it.toJson()) }
        put("night", JSONArray().apply {
            nightDivisions.forEach { put(it.toJson()) }
        })
    }

    private fun PrayerNotificationItem.toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("fireAt", fireAtMillis)

    private fun NightDivisionItem.toJson(): JSONObject = JSONObject()
        .put("kind", kind.name)
        .put("fireAt", fireAtMillis)
}
