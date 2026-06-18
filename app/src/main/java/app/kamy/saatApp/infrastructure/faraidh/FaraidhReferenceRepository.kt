package app.kamy.saatApp.infrastructure.faraidh

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhProofKind
import app.kamy.saatApp.domain.faraidh.FaraidhReferenceBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class FaraidhReferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val assetJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private var cached: FaraidhReferenceBundle? = null

    suspend fun loadBundle(): FaraidhReferenceBundle = withContext(Dispatchers.IO) {
        cached ?: run {
            val text = context.assets.open("faraidh/references.json").bufferedReader().use { it.readText() }
            assetJson.decodeFromString(FaraidhReferenceBundle.serializer(), text).also { cached = it }
        }
    }

    suspend fun proofsForKeys(keys: List<String>, language: AppLanguage): List<FaraidhProofItem> {
        val bundle = loadBundle()
        val refIds = keys.flatMap { bundle.proofMappings[it].orEmpty() }.distinct()
        val items = mutableListOf<FaraidhProofItem>()
        keys.forEach { key ->
            if (key == "faraidh_awl_note" || key == "faraidh_radd_note") {
                items += FaraidhProofItem(
                    id = key,
                    kind = FaraidhProofKind.NOTE,
                    title = localizedNoteTitle(key, language),
                    body = localizedNoteBody(key, language),
                    arabic = null,
                    externalUrl = null
                )
            }
        }
        refIds.forEach { id ->
            bundle.verses.find { it.id == id }?.let { verse ->
                items += FaraidhProofItem(
                    id = verse.id,
                    kind = FaraidhProofKind.QURAN,
                    title = "Qur'an ${verse.surah}:${verse.ayah}",
                    body = localizedVerseText(verse, language),
                    arabic = verse.arabic,
                    externalUrl = null,
                    surah = verse.surah,
                    ayah = verse.ayah
                )
            }
            bundle.hadiths.find { it.id == id }?.let { hadith ->
                items += FaraidhProofItem(
                    id = hadith.id,
                    kind = FaraidhProofKind.HADITH,
                    title = "${hadith.collection} ${hadith.number}",
                    body = localizedHadithText(hadith, language),
                    arabic = hadith.arabic,
                    externalUrl = hadith.externalUrl
                )
            }
        }
        return items.distinctBy { it.id }
    }

    private fun localizedVerseText(verse: app.kamy.saatApp.domain.faraidh.FaraidhVerseRef, language: AppLanguage): String =
        when (language) {
            AppLanguage.INDONESIAN -> verse.textId
            AppLanguage.MALAY -> verse.textMs
            AppLanguage.ENGLISH -> verse.textEn
        }

    private fun localizedHadithText(hadith: app.kamy.saatApp.domain.faraidh.FaraidhHadithRef, language: AppLanguage): String =
        when (language) {
            AppLanguage.INDONESIAN -> hadith.textId
            AppLanguage.MALAY -> hadith.textMs
            AppLanguage.ENGLISH -> hadith.textEn
        }

    private fun localizedNoteTitle(key: String, language: AppLanguage): String = when (key) {
        "faraidh_awl_note" -> when (language) {
            AppLanguage.INDONESIAN -> "Aul (defisit pembagian)"
            AppLanguage.MALAY -> "Aul (kekurangan bahagian)"
            AppLanguage.ENGLISH -> "ʿAwl (deficit adjustment)"
        }
        else -> when (language) {
            AppLanguage.INDONESIAN -> "Radd (surplus pembagian)"
            AppLanguage.MALAY -> "Radd (lebihan bahagian)"
            AppLanguage.ENGLISH -> "Radd (surplus redistribution)"
        }
    }

    private fun localizedNoteBody(key: String, language: AppLanguage): String = when (key) {
        "faraidh_awl_note" -> when (language) {
            AppLanguage.INDONESIAN -> "Jika jumlah bagian tetap melebihi harta, semua porsi dikurangi proporsional dengan menaikkan penyebut (qismah musytarakah)."
            AppLanguage.MALAY -> "Jika jumlah bahagian tetap melebihi harta, semua bahagian dikurangkan secara proporsional dengan menaikkan penyebut."
            AppLanguage.ENGLISH -> "When fixed shares exceed the estate, all portions are reduced proportionally by increasing the common denominator."
        }
        else -> when (language) {
            AppLanguage.INDONESIAN -> "Jika sisa harta belum terdistribusi dan tidak ada asabah, kelebihan dikembalikan (radd) kepada ahli waris nasab secara proporsional—istri/suami dikecualikan menurut mazhab mayoritas."
            AppLanguage.MALAY -> "Jika baki harta belum diagihkan dan tiada asabah, lebihan dikembalikan (radd) kepada waris nasab secara proporsional—pasangan dikecualikan mengikut mazhab majoriti."
            AppLanguage.ENGLISH -> "When residue remains and no asabah exists, the surplus is redistributed (radd) proportionally among blood heirs—spouses excluded per majority opinion."
        }
    }
}
