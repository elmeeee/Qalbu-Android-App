package app.kamy.qalbuApp.infrastructure.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val countryCode: String? = null
)

/**
 * Thin FusedLocation wrapper. Returns null when the user hasn't granted
 * either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION.
 *
 * Compose layer (Accompanist `rememberMultiplePermissionsState`) is responsible
 * for prompting the user — we just read the current grant state here.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): UserLocation? {
        if (!hasAnyPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        // GPS can be null right after permission grant — retry briefly like iOS location updates.
        repeat(4) { attempt ->
            val location = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                ?: client.lastLocation.await()
            if (location != null) {
                return UserLocation(location.latitude, location.longitude)
            }
            if (attempt < 3) delay(750L)
        }
        return null
    }

    /** Mirrors iOS PrayerTimesViewModel reverse geocode for city + country auto-detect. */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResult =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext ReverseGeocodeResult()
            try {
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
            } catch (_: Throwable) {
                ReverseGeocodeResult()
            }
        }

    suspend fun reverseGeocodeCity(latitude: Double, longitude: Double): String? =
        reverseGeocode(latitude, longitude).cityName

    fun coordinateLabel(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.3f, %.3f", latitude, longitude)

    fun hasAnyPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
