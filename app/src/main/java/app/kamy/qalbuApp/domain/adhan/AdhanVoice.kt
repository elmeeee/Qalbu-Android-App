package app.kamy.qalbuApp.domain.adhan

import androidx.annotation.RawRes
import app.kamy.qalbuApp.R

/** User-selectable adhan for Dhuhr, Asr, Maghrib, and Isha. Fajr always uses [FAJR_FIXED]. */
enum class AdhanVoice(
    val id: String,
    val displayName: String,
    @RawRes val rawRes: Int
) {
    OmarHishamAlArabi(
        id = "omar_hisham",
        displayName = "Omar Hisham Al Arabi",
        rawRes = R.raw.adhan_omar_hisham_al_arabi
    ),
    UstDaengSyawal(
        id = "ust_daeng_syawal",
        displayName = "Ust. Daeng Syawal Mubarak",
        rawRes = R.raw.adhan_ust_daeng_syawal_indonesia
    ),
    UstazSadidSingapore(
        id = "ustaz_sadid_singapore",
        displayName = "Ustaz Sadid Latiff & Ustaz Ahmad Dahri",
        rawRes = R.raw.adhan_ustaz_sadid_ahmad_dahri_singapore
    ),
    SheikhAbdulKarimMalaysia(
        id = "sheikh_abdul_karim",
        displayName = "Sheikh Abdul Karim Umar Al-Makki",
        rawRes = R.raw.adhan_sheikh_abdul_karim_malaysia
    );

    companion object {
        val DEFAULT = OmarHishamAlArabi

        fun fromId(id: String?): AdhanVoice =
            entries.firstOrNull { it.id == id } ?: DEFAULT

        val selectable: List<AdhanVoice> = entries
    }
}

object AdhanVoiceCatalog {
    @RawRes
    val fajrRawRes: Int = R.raw.adhan_fajr_mishary_alafasy

    const val fajrDisplayName: String = "Mishary Rashid Alafasy (Fajr)"

    @RawRes
    fun rawResForPrayer(prayerName: String, selected: AdhanVoice): Int =
        if (prayerName.equals("Fajr", ignoreCase = true)) fajrRawRes else selected.rawRes

    @RawRes
    fun rawResForPreview(voice: AdhanVoice): Int = voice.rawRes
}
