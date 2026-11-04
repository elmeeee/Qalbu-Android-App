package app.kamy.saatApp.shared.domain.tools

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaCalculator {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    private fun toRadians(deg: Double): Double = deg * (PI / 180.0)
    private fun toDegrees(rad: Double): Double = rad * (180.0 / PI)

    /** Bearing from user location to Kaaba in degrees (0–360, clockwise from north). */
    fun bearingToKaaba(latitude: Double, longitude: Double): Float {
        val lat1 = toRadians(latitude)
        val lat2 = toRadians(KAABA_LAT)
        val deltaLng = toRadians(KAABA_LNG - longitude)
        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)
        val bearing = toDegrees(atan2(y, x))
        return (((bearing + 360) % 360)).toFloat()
    }

    fun distanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val earthRadius = 6371.0
        val dLat = toRadians(KAABA_LAT - latitude)
        val dLng = toRadians(KAABA_LNG - longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(toRadians(latitude)) * cos(toRadians(KAABA_LAT)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
