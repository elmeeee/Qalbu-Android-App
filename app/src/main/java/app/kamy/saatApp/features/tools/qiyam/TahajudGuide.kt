package app.kamy.saatApp.features.tools.qiyam

import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class TahajudGuideCategory {
    PREPARATION,
    PRAYER,
    WITR,
    CLOSING
}

data class TahajudReading(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @StringRes val arabicRes: Int? = null,
    @StringRes val transliterationRes: Int? = null,
    val category: TahajudGuideCategory
)

object TahajudGuide {
    val readings: List<TahajudReading> = listOf(
        TahajudReading(
            id = "when",
            titleRes = R.string.tahajud_when_title,
            bodyRes = R.string.tahajud_when_body,
            category = TahajudGuideCategory.PREPARATION
        ),
        TahajudReading(
            id = "niat",
            titleRes = R.string.tahajud_niat_title,
            bodyRes = R.string.tahajud_niat_body,
            arabicRes = R.string.tahajud_niat_arabic,
            transliterationRes = R.string.tahajud_niat_translit,
            category = TahajudGuideCategory.PREPARATION
        ),
        TahajudReading(
            id = "takbir",
            titleRes = R.string.tahajud_takbir_title,
            bodyRes = R.string.tahajud_takbir_body,
            arabicRes = R.string.tahajud_takbir_arabic,
            transliterationRes = R.string.tahajud_takbir_translit,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "iftitah",
            titleRes = R.string.tahajud_iftitah_title,
            bodyRes = R.string.tahajud_iftitah_body,
            arabicRes = R.string.tahajud_iftitah_arabic,
            transliterationRes = R.string.tahajud_iftitah_translit,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "fatihah",
            titleRes = R.string.tahajud_fatihah_title,
            bodyRes = R.string.tahajud_fatihah_body,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "surah",
            titleRes = R.string.tahajud_surah_title,
            bodyRes = R.string.tahajud_surah_body,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "ruku",
            titleRes = R.string.tahajud_ruku_title,
            bodyRes = R.string.tahajud_ruku_body,
            arabicRes = R.string.tahajud_ruku_arabic,
            transliterationRes = R.string.tahajud_ruku_translit,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "sujud",
            titleRes = R.string.tahajud_sujud_title,
            bodyRes = R.string.tahajud_sujud_body,
            arabicRes = R.string.tahajud_sujud_arabic,
            transliterationRes = R.string.tahajud_sujud_translit,
            category = TahajudGuideCategory.PRAYER
        ),
        TahajudReading(
            id = "witr",
            titleRes = R.string.tahajud_witr_title,
            bodyRes = R.string.tahajud_witr_body,
            category = TahajudGuideCategory.WITR
        ),
        TahajudReading(
            id = "qunut",
            titleRes = R.string.tahajud_qunut_title,
            bodyRes = R.string.tahajud_qunut_body,
            arabicRes = R.string.tahajud_qunut_arabic,
            transliterationRes = R.string.tahajud_qunut_translit,
            category = TahajudGuideCategory.WITR
        ),
        TahajudReading(
            id = "dhikr_after",
            titleRes = R.string.tahajud_dhikr_title,
            bodyRes = R.string.tahajud_dhikr_body,
            category = TahajudGuideCategory.CLOSING
        ),
        TahajudReading(
            id = "dua",
            titleRes = R.string.tahajud_dua_title,
            bodyRes = R.string.tahajud_dua_body,
            category = TahajudGuideCategory.CLOSING
        )
    )

    fun byCategory(category: TahajudGuideCategory): List<TahajudReading> =
        readings.filter { it.category == category }
}
