package app.kamy.saatApp.domain.model

import androidx.annotation.DrawableRes
import app.kamy.saatApp.R

/**
 * Brand avatars the user can be given. A random one is assigned on first sign-in and stored
 * in Firestore (`users/{uid}.avatarKey`), so the same avatar shows across devices.
 * Seasonal mascots (Ramadan, Eid, Umrah, Khilafah) are excluded so identity stays stable.
 */
enum class BrandAvatar(val key: String, @DrawableRes val drawableRes: Int) {
    QURAN_QUOTE("quran_quote", R.drawable.mascot_quran_qoute),
    LENTERA("lentera", R.drawable.mascot_lentera),
    BAWA_LENTERA("bawa_lentera", R.drawable.mascot_bawa_lentera),
    READING("reading", R.drawable.mascot_reading),
    ILMU("ilmu", R.drawable.mascot_ilmu),
    PRAYER("prayer", R.drawable.mascot_prayer),
    SMILE("smile", R.drawable.mascot_smile),
    TOA("toa", R.drawable.mascot_toa);

    companion object {
        fun fromKey(key: String?): BrandAvatar? = entries.firstOrNull { it.key == key }
        fun random(): BrandAvatar = entries.random()
    }
}
