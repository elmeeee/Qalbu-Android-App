package app.kamy.saatApp.infrastructure.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import app.kamy.saatApp.infrastructure.local.LocalCityCatalog
import app.kamy.saatApp.infrastructure.local.OfflineCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class UserLocation(val latitude: Double, val longitude: Double)

data class ReverseGeocodeResult(
    val cityName: String? = null,
    val countryCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): UserLocation? {
        if (!hasAnyPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)

        // Fast path: try last location first (instant if cached).
        try {
            client.lastLocation.await()?.let {
                return UserLocation(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fresh install / no cache: request current location with timeout.
        // Use HIGH accuracy first for faster cold-start GPS, then BALANCED as fallback.
        val priorities = listOf(
            Priority.PRIORITY_HIGH_ACCURACY,
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        )
        for (priority in priorities) {
            try {
                kotlinx.coroutines.withTimeout(4500L) {
                    client.getCurrentLocation(priority, null).await()
                }?.let {
                    return UserLocation(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResult =
        withContext(Dispatchers.IO) {
            if (Geocoder.isPresent()) {
                try {
                    val address = kotlinx.coroutines.withTimeout(5000L) {
                        @Suppress("DEPRECATION")
                        Geocoder(context, Locale.getDefault())
                            .getFromLocation(latitude, longitude, 1)
                            ?.firstOrNull()
                    }
                    android.util.Log.d(
                        "LocationProvider",
                        "Address geocoded: subLocality=${address?.subLocality}, locality=${address?.locality}, subAdminArea=${address?.subAdminArea}, adminArea=${address?.adminArea}"
                    )
                    // Priority: subLocality (Kelurahan) first, then locality (Kecamatan/Kota), then subAdminArea (Kabupaten)
                    val name = address?.subLocality?.takeIf { it.isNotBlank() }
                        ?: address?.locality?.takeIf { it.isNotBlank() }
                        ?: address?.subAdminArea?.takeIf { it.isNotBlank() }
                        ?: address?.adminArea?.takeIf { it.isNotBlank() }
                    if (name != null) {
                        return@withContext ReverseGeocodeResult(
                            cityName = name,
                            countryCode = address?.countryCode?.takeIf { it.isNotBlank() }
                        )
                    }
                } catch (e: Throwable) {
                    android.util.Log.w("LocationProvider", "Android Geocoder failed: ${e.message}")
                }
            }

            // HTTP Fallback to guarantee accurate Kelurahan resolution even when Google Play Services Geocoder is unavailable
            fallbackReverseGeocodeHttp(latitude, longitude)?.let { httpResult ->
                android.util.Log.d("LocationProvider", "HTTP fallback geocoded: ${httpResult.cityName}")
                return@withContext httpResult
            }

            val nearest = LocalCityCatalog.findNearest(latitude, longitude)
            ReverseGeocodeResult(
                cityName = nearest?.name,
                countryCode = nearest?.countryCode
            )
        }

    private fun fallbackReverseGeocodeHttp(latitude: Double, longitude: Double): ReverseGeocodeResult? {
        return try {
            val urlString = String.format(
                Locale.US,
                "https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json&accept-language=id",
                latitude,
                longitude
            )
            val connection = (java.net.URL(urlString).openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "SaatApp/1.0 (Android)")
            }
            if (connection.responseCode == 200) {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val root = org.json.JSONObject(body)
                val address = root.optJSONObject("address")
                if (address != null) {
                    val kelurahan = address.optString("village").takeIf { it.isNotBlank() }
                        ?: address.optString("suburb").takeIf { it.isNotBlank() }
                        ?: address.optString("neighbourhood").takeIf { it.isNotBlank() }
                        ?: address.optString("municipality").takeIf { it.isNotBlank() }
                        ?: address.optString("city").takeIf { it.isNotBlank() }
                        ?: address.optString("town").takeIf { it.isNotBlank() }
                        ?: address.optString("county").takeIf { it.isNotBlank() }
                    val countryCode = address.optString("country_code").takeIf { it.isNotBlank() }?.uppercase(Locale.US)
                    if (kelurahan != null) {
                        return ReverseGeocodeResult(
                            cityName = kelurahan,
                            countryCode = countryCode
                        )
                    }
                }
            }
            null
        } catch (e: Throwable) {
            android.util.Log.w("LocationProvider", "HTTP fallback reverse geocode failed", e)
            null
        }
    }

    fun findNearestCity(latitude: Double, longitude: Double): OfflineCity? =
        LocalCityCatalog.findNearest(latitude, longitude)

    suspend fun reverseGeocodeCity(latitude: Double, longitude: Double): String? =
        reverseGeocode(latitude, longitude).cityName

    fun coordinateLabel(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.3f, %.3f", latitude, longitude)

    suspend fun forwardGeocode(query: String): ReverseGeocodeResult? =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return@withContext null
            LocalCityCatalog.findExact(trimmed)?.let { city ->
                return@withContext ReverseGeocodeResult(
                    cityName = city.displayLabel,
                    countryCode = city.countryCode,
                    latitude = city.latitude,
                    longitude = city.longitude
                )
            }
            LocalCityCatalog.search(trimmed, limit = 1).firstOrNull()?.let { city ->
                return@withContext ReverseGeocodeResult(
                    cityName = city.displayLabel,
                    countryCode = city.countryCode,
                    latitude = city.latitude,
                    longitude = city.longitude
                )
            }
            if (!Geocoder.isPresent()) return@withContext null
            try {
                @Suppress("DEPRECATION")
                val address = Geocoder(context, Locale.getDefault())
                    .getFromLocationName(trimmed, 1)
                    ?.firstOrNull()
                    ?: return@withContext null
                ReverseGeocodeResult(
                    cityName = address.locality?.takeIf { it.isNotBlank() }
                        ?: address.subAdminArea?.takeIf { it.isNotBlank() }
                        ?: address.adminArea?.takeIf { it.isNotBlank() }
                        ?: trimmed,
                    countryCode = address.countryCode?.takeIf { it.isNotBlank() },
                    latitude = address.latitude,
                    longitude = address.longitude
                )
            } catch (_: Throwable) {
                null
            }
        }

    fun hasAnyPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun searchOfflineCities(query: String, limit: Int = 12): List<OfflineCity> =
        LocalCityCatalog.search(query, limit)
}
