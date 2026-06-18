package app.kamy.qalbuApp.infrastructure.local

import java.util.Locale

data class OfflineCity(
    val name: String,
    val region: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayLabel: String
        get() = if (region.isBlank()) name else "$name, $region"
}

/**
 * Bundled cities for offline prayer location (no Geocoder / GPS required).
 * Indonesia + major international hubs.
 */
object LocalCityCatalog {
    private val cities: List<OfflineCity> = listOf(
        // Indonesia
        OfflineCity("Jakarta", "DKI Jakarta", "ID", -6.2088, 106.8456),
        OfflineCity("Surabaya", "Jawa Timur", "ID", -7.2575, 112.7521),
        OfflineCity("Bandung", "Jawa Barat", "ID", -6.9175, 107.6191),
        OfflineCity("Medan", "Sumatera Utara", "ID", 3.5952, 98.6722),
        OfflineCity("Semarang", "Jawa Tengah", "ID", -6.9667, 110.4167),
        OfflineCity("Makassar", "Sulawesi Selatan", "ID", -5.1477, 119.4327),
        OfflineCity("Palembang", "Sumatera Selatan", "ID", -2.9761, 104.7754),
        OfflineCity("Tangerang", "Banten", "ID", -6.1783, 106.6319),
        OfflineCity("Depok", "Jawa Barat", "ID", -6.4025, 106.7942),
        OfflineCity("Bekasi", "Jawa Barat", "ID", -6.2383, 106.9756),
        OfflineCity("Yogyakarta", "DI Yogyakarta", "ID", -7.7956, 110.3695),
        OfflineCity("Malang", "Jawa Timur", "ID", -7.9666, 112.6326),
        OfflineCity("Denpasar", "Bali", "ID", -8.6705, 115.2126),
        OfflineCity("Banjarmasin", "Kalimantan Selatan", "ID", -3.3186, 114.5944),
        OfflineCity("Pontianak", "Kalimantan Barat", "ID", -0.0263, 109.3425),
        OfflineCity("Balikpapan", "Kalimantan Timur", "ID", -1.2379, 116.8529),
        OfflineCity("Pekanbaru", "Riau", "ID", 0.5071, 101.4478),
        OfflineCity("Padang", "Sumatera Barat", "ID", -0.9471, 100.4172),
        OfflineCity("Manado", "Sulawesi Utara", "ID", 1.4748, 124.8421),
        OfflineCity("Mataram", "Nusa Tenggara Barat", "ID", -8.5833, 116.1167),
        OfflineCity("Kupang", "Nusa Tenggara Timur", "ID", -10.1772, 123.6070),
        OfflineCity("Jayapura", "Papua", "ID", -2.5337, 140.7181),
        OfflineCity("Aceh", "Aceh", "ID", 5.5483, 95.3238),
        OfflineCity("Banda Aceh", "Aceh", "ID", 5.5483, 95.3238),
        OfflineCity("Lhokseumawe", "Aceh", "ID", 5.1801, 97.1507),
        OfflineCity("Batam", "Kepulauan Riau", "ID", 1.0456, 104.0305),
        OfflineCity("Cirebon", "Jawa Barat", "ID", -6.7320, 108.5523),
        OfflineCity("Solo", "Jawa Tengah", "ID", -7.5755, 110.8243),
        OfflineCity("Samarinda", "Kalimantan Timur", "ID", -0.5022, 117.1536),
        OfflineCity("Tasikmalaya", "Jawa Barat", "ID", -7.3274, 108.2207),
        // Malaysia, Singapore, Brunei
        OfflineCity("Kuala Lumpur", "Wilayah Persekutuan", "MY", 3.1390, 101.6869),
        OfflineCity("Johor Bahru", "Johor", "MY", 1.4927, 103.7414),
        OfflineCity("Penang", "Pulau Pinang", "MY", 5.4141, 100.3288),
        OfflineCity("Kota Kinabalu", "Sabah", "MY", 5.9804, 116.0735),
        OfflineCity("Kuching", "Sarawak", "MY", 1.5535, 110.3593),
        OfflineCity("Singapore", "", "SG", 1.3521, 103.8198),
        OfflineCity("Bandar Seri Begawan", "Brunei", "BN", 4.9031, 114.9398),
        // Middle East & South Asia
        OfflineCity("Makkah", "Makkah", "SA", 21.4225, 39.8262),
        OfflineCity("Madinah", "Madinah", "SA", 24.4672, 39.6111),
        OfflineCity("Riyadh", "Riyadh", "SA", 24.7136, 46.6753),
        OfflineCity("Jeddah", "Makkah", "SA", 21.4858, 39.1925),
        OfflineCity("Dubai", "Dubai", "AE", 25.2048, 55.2708),
        OfflineCity("Doha", "Qatar", "QA", 25.2854, 51.5310),
        OfflineCity("Kuwait City", "Kuwait", "KW", 29.3759, 47.9774),
        OfflineCity("Istanbul", "Türkiye", "TR", 41.0082, 28.9784),
        OfflineCity("Cairo", "Egypt", "EG", 30.0444, 31.2357),
        OfflineCity("Karachi", "Sindh", "PK", 24.8607, 67.0011),
        OfflineCity("Lahore", "Punjab", "PK", 31.5204, 74.3587),
        OfflineCity("Dhaka", "Bangladesh", "BD", 23.8103, 90.4125),
        OfflineCity("Tehran", "Iran", "IR", 35.6892, 51.3890),
        // Southeast Asia & East Asia
        OfflineCity("Bangkok", "Thailand", "TH", 13.7563, 100.5018),
        OfflineCity("Manila", "Philippines", "PH", 14.5995, 120.9842),
        OfflineCity("Hong Kong", "Hong Kong", "HK", 22.3193, 114.1694),
        OfflineCity("Tokyo", "Japan", "JP", 35.6762, 139.6503),
        // Western
        OfflineCity("London", "United Kingdom", "GB", 51.5074, -0.1278),
        OfflineCity("Paris", "France", "FR", 48.8566, 2.3522),
        OfflineCity("New York", "New York", "US", 40.7128, -74.0060),
        OfflineCity("Los Angeles", "California", "US", 34.0522, -118.2437),
        OfflineCity("Toronto", "Ontario", "CA", 43.6532, -79.3832),
        OfflineCity("Sydney", "New South Wales", "AU", -33.8688, 151.2093)
    )

    fun search(query: String, limit: Int = 12): List<OfflineCity> {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.length < 2) return emptyList()
        return cities
            .asSequence()
            .filter { city ->
                city.name.lowercase(Locale.getDefault()).contains(q) ||
                    city.region.lowercase(Locale.getDefault()).contains(q) ||
                    city.countryCode.lowercase(Locale.getDefault()) == q
            }
            .sortedBy { it.name }
            .take(limit)
            .toList()
    }

    fun findExact(query: String): OfflineCity? {
        val q = query.trim().lowercase(Locale.getDefault())
        return cities.firstOrNull {
            it.name.lowercase(Locale.getDefault()) == q ||
                it.displayLabel.lowercase(Locale.getDefault()) == q
        }
    }
}
