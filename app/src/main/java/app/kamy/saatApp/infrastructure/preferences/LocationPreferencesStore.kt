package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class LocationMode {
    GPS,
    MANUAL
}

data class SavedManualLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val countryCode: String? = null
)

@Singleton
class LocationPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun mode(): LocationMode =
        if (prefs.getString(KEY_MODE, LocationMode.GPS.name) == LocationMode.MANUAL.name) {
            LocationMode.MANUAL
        } else {
            LocationMode.GPS
        }

    fun setMode(mode: LocationMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun manualLocation(): SavedManualLocation? {
        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LON)) return null
        val lat = prefs.getFloat(KEY_LAT, Float.NaN).toDouble()
        val lon = prefs.getFloat(KEY_LON, Float.NaN).toDouble()
        if (lat.isNaN() || lon.isNaN()) return null
        val label = prefs.getString(KEY_LABEL, null)?.takeIf { it.isNotBlank() } ?: return null
        return SavedManualLocation(
            latitude = lat,
            longitude = lon,
            label = label,
            countryCode = prefs.getString(KEY_COUNTRY, null)?.takeIf { it.isNotBlank() }
        )
    }

    fun saveManual(location: SavedManualLocation) {
        prefs.edit()
            .putString(KEY_MODE, LocationMode.MANUAL.name)
            .putFloat(KEY_LAT, location.latitude.toFloat())
            .putFloat(KEY_LON, location.longitude.toFloat())
            .putString(KEY_LABEL, location.label)
            .putString(KEY_COUNTRY, location.countryCode)
            .apply()
    }

    fun saveActiveLabel(label: String) {
        prefs.edit().putString(KEY_ACTIVE_LABEL, label).apply()
    }

    fun displayLabel(): String? =
        prefs.getString(KEY_ACTIVE_LABEL, null)?.takeIf { it.isNotBlank() }
            ?: manualLocation()?.label

    companion object {
        fun from(context: Context): LocationPreferencesStore =
            LocationPreferencesStore(context.applicationContext)

        private const val PREFS = "saat_location_prefs"
        private const val KEY_MODE = "mode"
        private const val KEY_LAT = "latitude"
        private const val KEY_LON = "longitude"
        private const val KEY_LABEL = "label"
        private const val KEY_COUNTRY = "country_code"
        private const val KEY_ACTIVE_LABEL = "active_label"
    }
}
