package app.kamy.qalbuApp.infrastructure.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserLocation(val latitude: Double, val longitude: Double)

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
        // CurrentLocation is preferred over LastLocation — it triggers a fresh sample
        // if the cached one is stale or absent (e.g., right after permission grant).
        val location = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            ?: client.lastLocation.await()
            ?: return null
        return UserLocation(location.latitude, location.longitude)
    }

    fun hasAnyPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
