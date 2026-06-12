package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerScheduleCache {
    private const val PREFS = "qalbu_prayer_schedule_cache"
    private const val KEY_JSON = "bundle_json"
    private const val KEY_LAT = "latitude"
    private const val KEY_LON = "longitude"
    private const val KEY_META = "widget_meta_json"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    data class WidgetMeta(
        val cityLabel: String,
        val hijriLabel: String?,
        val gregorianLabel: String?,
        val timings: Map<String, String>
    )

    fun save(
        context: Context,
        bundle: PrayerScheduleBundle,
        latitude: Double,
        longitude: Double,
        meta: WidgetMeta? = null
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, bundle.toJson().toString())
            .putFloat(KEY_LAT, latitude.toFloat())
            .putFloat(KEY_LON, longitude.toFloat())
            .putString(KEY_META, meta?.toJson()?.toString())
            .apply()
    }

    fun loadMeta(context: Context): WidgetMeta? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_META, null)
            ?: return null
        return runCatching { parseMeta(JSONObject(raw)) }.getOrNull()
    }

    fun load(context: Context): PrayerScheduleBundle? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_JSON, null)
            ?: return null
        return runCatching { parseBundle(JSONObject(raw)) }.getOrNull()
    }

    fun loadCoordinates(context: Context): Pair<Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LON)) return null
        val lat = prefs.getFloat(KEY_LAT, Float.NaN).toDouble()
        val lon = prefs.getFloat(KEY_LON, Float.NaN).toDouble()
        if (lat.isNaN() || lon.isNaN()) return null
        return lat to lon
    }

    fun isStale(context: Context): Boolean {
        val bundle = load(context) ?: return true
        return bundle.dayKey != dayKeyFormat.format(Date())
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
                val kind = runCatching {
                    NightDivisionKind.valueOf(item.getString("kind"))
                }.getOrNull() ?: continue
                add(NightDivisionItem(kind = kind, fireAtMillis = item.getLong("fireAt")))
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

    private fun WidgetMeta.toJson(): JSONObject = JSONObject().apply {
        put("cityLabel", cityLabel)
        put("hijriLabel", hijriLabel)
        put("gregorianLabel", gregorianLabel)
        put("timings", JSONObject().apply {
            timings.forEach { (key, value) -> put(key, value) }
        })
    }

    private fun parseMeta(o: JSONObject): WidgetMeta {
        val timings = buildMap {
            val obj = o.optJSONObject("timings") ?: return@buildMap
            obj.keys().forEach { key -> put(key, obj.getString(key)) }
        }
        return WidgetMeta(
            cityLabel = o.getString("cityLabel"),
            hijriLabel = o.optString("hijriLabel").takeIf { it.isNotBlank() },
            gregorianLabel = o.optString("gregorianLabel").takeIf { it.isNotBlank() },
            timings = timings
        )
    }
}
