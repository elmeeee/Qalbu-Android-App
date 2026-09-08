package app.kamy.saatApp.domain.prayer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class PrayerMadhab(
    val rawValue: String,
    @StringRes val displayNameRes: Int,
    @DrawableRes val iconRes: Int
) {
    SHAFI("shafi", R.string.madhab_shafi, R.drawable.imam_syafii),
    MALIKI("maliki", R.string.madhab_maliki, R.drawable.imam_maliki),
    HANBALI("hanbali", R.string.madhab_hanbali, R.drawable.imam_hambali),
    HANAFI("hanafi", R.string.madhab_hanafi, R.drawable.imam_hanafi);

    companion object {
        val defaultMadhab = SHAFI

        fun fromRawValue(raw: String?): PrayerMadhab =
            values().firstOrNull { it.rawValue == raw } ?: defaultMadhab
    }
}
