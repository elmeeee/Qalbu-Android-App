package app.kamy.saatApp.domain.prayer

enum class PrayerMadhab(val rawValue: String, val displayNameRes: Int) {
    SHAFI("shafi", app.kamy.saatApp.R.string.madhab_shafi),
    MALIKI("maliki", app.kamy.saatApp.R.string.madhab_maliki),
    HANBALI("hanbali", app.kamy.saatApp.R.string.madhab_hanbali),
    HANAFI("hanafi", app.kamy.saatApp.R.string.madhab_hanafi);

    companion object {
        val defaultMadhab = SHAFI

        fun fromRawValue(raw: String?): PrayerMadhab =
            values().firstOrNull { it.rawValue == raw } ?: defaultMadhab
    }
}
