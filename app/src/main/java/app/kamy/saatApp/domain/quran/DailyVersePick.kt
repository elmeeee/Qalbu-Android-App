package app.kamy.saatApp.domain.quran

import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class DailyVerseOccasion(val key: String, @StringRes val labelRes: Int) {
    Ramadan("ramadan", R.string.daily_verse_occasion_ramadan),
    EidFitr("eid_fitr", R.string.daily_verse_occasion_eid_fitr),
    EidAdha("eid_adha", R.string.daily_verse_occasion_eid_adha),
    Hajj("hajj", R.string.daily_verse_occasion_hajj),
    Muharram("muharram", R.string.daily_verse_occasion_muharram),
    Ashura("ashura", R.string.daily_verse_occasion_ashura),
    IsraMiraj("isra_miraj", R.string.daily_verse_occasion_isra_miraj),
    LailatulQadr("lailatul_qadr", R.string.daily_verse_occasion_lailatul_qadr),
    Maulid("maulid", R.string.daily_verse_occasion_maulid),
    Tasyrik("tasyrik", R.string.daily_verse_occasion_tasyrik),
    AyyamulBidh("ayyamul_bidh", R.string.daily_verse_occasion_ayyamul_bidh),
    FastDaud("fast_daud", R.string.daily_verse_occasion_fasting_daud),
    Jumuah("jumuah", R.string.daily_verse_occasion_jumuah),
    Monday("monday", R.string.daily_verse_occasion_monday),
    Tuesday("tuesday", R.string.daily_verse_occasion_tuesday),
    Wednesday("wednesday", R.string.daily_verse_occasion_wednesday),
    Thursday("thursday", R.string.daily_verse_occasion_thursday),
    Saturday("saturday", R.string.daily_verse_occasion_saturday),
    Sunday("sunday", R.string.daily_verse_occasion_sunday),
    Daily("daily", R.string.daily_verse_occasion_daily);

    companion object {
        fun fromKey(key: String?): DailyVerseOccasion? =
            entries.firstOrNull { it.key == key }
    }
}

data class DailyVersePick(
    val verseKey: String,
    val occasion: DailyVerseOccasion
)

data class DailyVerseContext(
    val dayKey: String,
    val dayOfYear: Int,
    val dayOfWeek: Int,
    val hijriLabel: String? = null,
    val eventTitle: String? = null,
    val isRamadanSeason: Boolean = false
)
