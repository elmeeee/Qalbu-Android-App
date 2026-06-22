package app.kamy.saatApp.domain.tools

import app.kamy.saatApp.R

/**
 * Canonical Manzil reading order (23 sections) per the compilation of Shah Waliullah
 * and Maulana Muhammad Zakariya — matching standard ruqyah references.
 */
data class ManzilSectionDef(
    val titleRes: Int,
    val descriptionRes: Int,
    val surah: Int,
    val startAyah: Int,
    val endAyah: Int
) {
    val key: String get() = "${surah}_${startAyah}_$endAyah"
}

fun manzilSections(): List<ManzilSectionDef> = listOf(
    ManzilSectionDef(R.string.manzil_item_fatihah, R.string.manzil_desc_fatihah, 1, 1, 7),
    ManzilSectionDef(R.string.manzil_item_baqarah_1, R.string.manzil_desc_baqarah_1, 2, 1, 5),
    ManzilSectionDef(R.string.manzil_item_baqarah_163, R.string.manzil_desc_baqarah_163, 2, 163, 163),
    ManzilSectionDef(R.string.manzil_item_ayat_kursi, R.string.manzil_desc_ayat_kursi, 2, 255, 255),
    ManzilSectionDef(R.string.manzil_item_baqarah_256, R.string.manzil_desc_baqarah_256, 2, 256, 256),
    ManzilSectionDef(R.string.manzil_item_baqarah_257, R.string.manzil_desc_baqarah_257, 2, 257, 257),
    ManzilSectionDef(R.string.manzil_item_baqarah_284, R.string.manzil_desc_baqarah_284, 2, 284, 284),
    ManzilSectionDef(R.string.manzil_item_baqarah_285, R.string.manzil_desc_baqarah_285, 2, 285, 285),
    ManzilSectionDef(R.string.manzil_item_baqarah_286, R.string.manzil_desc_baqarah_286, 2, 286, 286),
    ManzilSectionDef(R.string.manzil_item_ali_imran_18, R.string.manzil_desc_ali_imran_18, 3, 18, 18),
    ManzilSectionDef(R.string.manzil_item_ali_imran_26, R.string.manzil_desc_ali_imran_26, 3, 26, 26),
    ManzilSectionDef(R.string.manzil_item_ali_imran_27, R.string.manzil_desc_ali_imran_27, 3, 27, 27),
    ManzilSectionDef(R.string.manzil_item_al_araf, R.string.manzil_desc_al_araf, 7, 54, 56),
    ManzilSectionDef(R.string.manzil_item_al_isra, R.string.manzil_desc_al_isra, 17, 110, 111),
    ManzilSectionDef(R.string.manzil_item_al_muminun, R.string.manzil_desc_al_muminun, 23, 115, 118),
    ManzilSectionDef(R.string.manzil_item_ash_shaffat, R.string.manzil_desc_ash_shaffat, 37, 1, 11),
    ManzilSectionDef(R.string.manzil_item_ar_rahman, R.string.manzil_desc_ar_rahman, 55, 33, 40),
    ManzilSectionDef(R.string.manzil_item_al_hasyr, R.string.manzil_desc_al_hasyr, 59, 21, 24),
    ManzilSectionDef(R.string.manzil_item_al_jinn, R.string.manzil_desc_al_jinn, 72, 1, 4),
    ManzilSectionDef(R.string.manzil_item_al_kafirun, R.string.manzil_desc_al_kafirun, 109, 1, 6),
    ManzilSectionDef(R.string.manzil_item_al_ikhlas, R.string.manzil_desc_al_ikhlas, 112, 1, 4),
    ManzilSectionDef(R.string.manzil_item_al_falaq, R.string.manzil_desc_al_falaq, 113, 1, 5),
    ManzilSectionDef(R.string.manzil_item_an_nas, R.string.manzil_desc_an_nas, 114, 1, 6)
)
