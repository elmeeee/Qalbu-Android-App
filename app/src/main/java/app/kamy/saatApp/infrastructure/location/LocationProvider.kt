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
            if (!Geocoder.isPresent()) return@withContext ReverseGeocodeResult()
            try {
                kotlinx.coroutines.withTimeout(3500L) {
                    @Suppress("DEPRECATION")
                    val address = Geocoder(context, Locale.getDefault())
                        .getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                    ReverseGeocodeResult(
                        cityName = address?.locality?.takeIf { it.isNotBlank() }
                            ?: address?.subAdminArea?.takeIf { it.isNotBlank() }
                            ?: address?.adminArea?.takeIf { it.isNotBlank() },
                        countryCode = address?.countryCode?.takeIf { it.isNotBlank() }
                    )
                }
            } catch (_: Throwable) {
                ReverseGeocodeResult()
            }
        }

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
