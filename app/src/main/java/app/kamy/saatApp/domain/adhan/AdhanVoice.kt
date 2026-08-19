package app.kamy.saatApp.domain.adhan

import androidx.annotation.RawRes
import app.kamy.saatApp.R

enum class AdhanVoice(
    val id: String,
    val displayName: String,
    @RawRes val rawRes: Int
) {
    IslamSobhi(
        id = "islam_sobhi",
        displayName = "Islam Sobhi",
        rawRes = R.raw.adhan_islam_sobhi
    ),
    OmarHishamAlArabi(
        id = "omar_hisham",
        displayName = "Omar Hisham Al Arabi",
        rawRes = R.raw.adhan_omar_hisham_al_arabi
    ),
    HamzaAlMajali(
        id = "hamza_al_majali",
        displayName = "Hamza Al Majali",
        rawRes = R.raw.adhan_hamza_al_majale
    ),
    SheikhAbdulKarimMalaysia(
        id = "sheikh_abdul_karim",
        displayName = "Sheikh Abdul Karim Umar Al-Makki",
        rawRes = R.raw.adhan_sheikh_abdul_karim_malaysia
    ),
    UstBilalAttaki(
        id = "ust_bilal_attaki",
        displayName = "Ust. Bilal Attaki",
        rawRes = R.raw.adhan_normal_ust_bilal_attaki
    ),
    UstDaengSyawal(
        id = "ust_daeng_syawal",
        displayName = "Ust. Daeng Syawal Mubarak",
        rawRes = R.raw.adhan_ust_daeng_syawal_indonesia
    ),
    HabibSyech(
        id = "habib_syech",
        displayName = "Habib Syech Bin Abdul Qadir Assegaf",
        rawRes = R.raw.adhan_habib_syech
    ),
    MFikriIzzulKamil(
        id = "m_fikri_izzul_kamil",
        displayName = "M. Fikri Izzul Kamil",
        rawRes = R.raw.adhan_m_fikri_izzul_kamil
    );

    companion object {
        val DEFAULT = IslamSobhi

        fun fromId(id: String?): AdhanVoice =
            entries.firstOrNull { it.id == id } ?: DEFAULT

        val selectable: List<AdhanVoice> = entries
    }
}

enum class FajrAdhanVoice(
    val id: String,
    val displayName: String,
    @RawRes val rawRes: Int
) {
    MisharyAlafasy(
        id = "fajr_mishary",
        displayName = "Mishary Rashid Alafasy",
        rawRes = R.raw.adhan_fajr_mishary_alafasy
    ),
    UstBilalAttaki(
        id = "fajr_bilal_attaki",
        displayName = "Ust. Bilal Attaki",
        rawRes = R.raw.adhan_fajr_ust_bilal_attaki
    ),
    MuhammadRohani(
        id = "fajr_muhammad_rohani",
        displayName = "Muhammad Rohani",
        rawRes = R.raw.adhan_fajr_muhammad_rohani
    );

    companion object {
        val DEFAULT = MisharyAlafasy

        fun fromId(id: String?): FajrAdhanVoice =
            entries.firstOrNull { it.id == id } ?: DEFAULT

        val selectable: List<FajrAdhanVoice> = entries
    }
}

object AdhanVoiceCatalog {
    @RawRes
    val fajrRawRes: Int = R.raw.adhan_fajr_mishary_alafasy

    const val fajrDisplayName: String = "Mishary Rashid Alafasy (Fajr)"

    @RawRes
    fun rawResForPrayer(
        prayerName: String,
        selected: AdhanVoice,
        selectedFajr: FajrAdhanVoice = FajrAdhanVoice.DEFAULT
    ): Int =
        if (prayerName.equals("Fajr", ignoreCase = true) || prayerName.equals("Subuh", ignoreCase = true)) {
            selectedFajr.rawRes
        } else {
            selected.rawRes
        }

    @RawRes
    fun rawResForPreview(voice: AdhanVoice): Int = voice.rawRes

    @RawRes
    fun rawResForPreview(voice: FajrAdhanVoice): Int = voice.rawRes
}

