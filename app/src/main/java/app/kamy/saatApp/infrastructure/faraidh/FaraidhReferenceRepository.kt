package app.kamy.saatApp.infrastructure.faraidh

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryBundle
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryItem
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryTerm
import app.kamy.saatApp.domain.faraidh.FaraidhDictionaryBundle
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
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
    private var glossaryCached: FaraidhGlossaryBundle? = null
    private var dictionaryCached: FaraidhDictionaryBundle? = null

    suspend fun loadBundle(): FaraidhReferenceBundle = withContext(Dispatchers.IO) {
        cached ?: run {
            val text = context.assets.open("faraidh/references.json").bufferedReader().use { it.readText() }
            assetJson.decodeFromString(FaraidhReferenceBundle.serializer(), text).also { cached = it }
        }
    }

    suspend fun loadGlossary(): FaraidhGlossaryBundle = withContext(Dispatchers.IO) {
        glossaryCached ?: run {
            val text = context.assets.open("faraidh/glossary.json").bufferedReader().use { it.readText() }
            assetJson.decodeFromString(FaraidhGlossaryBundle.serializer(), text).also { glossaryCached = it }
        }
    }

    suspend fun loadDictionary(): FaraidhDictionaryBundle = withContext(Dispatchers.IO) {
        dictionaryCached ?: run {
            val text = context.assets.open("faraidh/faraidh_terms_dictionary.json").bufferedReader().use { it.readText() }
            assetJson.decodeFromString(FaraidhDictionaryBundle.serializer(), text).also { dictionaryCached = it }
        }
    }

    suspend fun glossaryItems(language: AppLanguage): List<FaraidhGlossaryItem> = withContext(Dispatchers.IO) {
        val baseItems = loadGlossary().terms.map { it.toItem(language) }
        val dict = loadDictionary()
        val dictItems = mutableListOf<FaraidhGlossaryItem>()

        fun addDictItems(map: Map<String, app.kamy.saatApp.domain.faraidh.FaraidhDictionaryItem>) {
            map.forEach { (key, item) ->
                val title = item.display_name[language.tag] ?: item.display_name["en"] ?: key
                val definition = item.glossary_definition[language.tag] ?: item.glossary_definition["en"].orEmpty()
                
                val dalilText = if (item.dalil.isNotEmpty()) {
                    val label = when (language) {
                        AppLanguage.INDONESIAN -> "\nRujukan Syariah (Dalil):"
                        AppLanguage.MALAY -> "\nRujukan Syariah (Dalil):"
                        AppLanguage.ENGLISH -> "\nScriptural Proofs (Dalil):"
                    }
                    val list = item.dalil.map { d ->
                        val citation = d.reference_citation[language.tag] ?: d.reference_citation["en"].orEmpty()
                        val trans = d.translation?.get(language.tag) ?: d.translation?.get("en").orEmpty()
                        val arab = d.arabic_text?.let { "\n   \"$it\"" }.orEmpty()
                        val citationSuffix = if (citation.isNotBlank()) " [$citation]" else ""
                        "- ${d.source}$citationSuffix: $trans$arab"
                    }.joinToString("\n")
                    "$label\n$list"
                } else ""

                dictItems += FaraidhGlossaryItem(
                    id = "dict_${item.id}",
                    title = title,
                    arabic = item.arabic_term,
                    body = if (dalilText.isNotBlank()) "$definition\n$dalilText" else definition
                )
            }
        }

        addDictItems(dict.standard_heirs)
        addDictItems(dict.classical_cases)
        addDictItems(dict.anomalies_and_legal_rules)
        addDictItems(dict.disqualifications)

        baseItems + dictItems
    }

    private fun FaraidhGlossaryTerm.toItem(language: AppLanguage): FaraidhGlossaryItem =
        FaraidhGlossaryItem(
            id = id,
            title = when (language) {
                AppLanguage.INDONESIAN -> titleId
                AppLanguage.MALAY -> titleMs
                AppLanguage.ENGLISH -> titleEn
            },
            body = when (language) {
                AppLanguage.INDONESIAN -> bodyId
                AppLanguage.MALAY -> bodyMs
                AppLanguage.ENGLISH -> bodyEn
            },
            arabic = arabic
        )

    fun madhhabLabel(madhhab: FaraidhMadhhab, language: AppLanguage): String = when (madhhab) {
        FaraidhMadhhab.HANAFI -> when (language) {
            AppLanguage.INDONESIAN -> "Hanafi"
            AppLanguage.MALAY -> "Hanafi"
            AppLanguage.ENGLISH -> "Hanafi"
        }
        FaraidhMadhhab.MALIKI -> when (language) {
            AppLanguage.INDONESIAN -> "Maliki"
            AppLanguage.MALAY -> "Maliki"
            AppLanguage.ENGLISH -> "Maliki"
        }
        FaraidhMadhhab.SHAFII -> when (language) {
            AppLanguage.INDONESIAN -> "Syafi'i"
            AppLanguage.MALAY -> "Syafi'i"
            AppLanguage.ENGLISH -> "Shafi'i"
        }
        FaraidhMadhhab.HANBALI -> when (language) {
            AppLanguage.INDONESIAN -> "Hanbali"
            AppLanguage.MALAY -> "Hanbali"
            AppLanguage.ENGLISH -> "Hanbali"
        }
    }

    fun madhhabNote(madhhab: FaraidhMadhhab, language: AppLanguage): String = when (madhhab) {
        FaraidhMadhhab.HANAFI -> when (language) {
            AppLanguage.INDONESIAN -> "Mazhab Hanafi: radd mencakup pasangan (suami/istri); saudara perempuan dapat asabah bil-ghayr bersama anak perempuan; beberapa kasus kakek dan saudara berbeda dari jumhur."
            AppLanguage.MALAY -> "Mazhab Hanafi: radd merangkumi pasangan; saudara perempuan boleh asabah bil-ghayr bersama anak perempuan; beberapa kes datuk dan saudara berbeza daripada jumhur."
            AppLanguage.ENGLISH -> "Hanafi madhhab: radd includes spouses; full sister may be asabah bil-ghayr with daughter; grandfather/sibling cases differ from jumhur in some scenarios."
        }
        FaraidhMadhhab.MALIKI -> when (language) {
            AppLanguage.INDONESIAN -> "Mazhab Maliki: radd mengecualikan pasangan (seperti Syafi'i/Hanbali); beberapa aturan residu dan kalalah mengikuti riwayat Malik."
            AppLanguage.MALAY -> "Mazhab Maliki: radd mengecualikan pasangan; beberapa peraturan residu dan kalalah mengikut riwayat Malik."
            AppLanguage.ENGLISH -> "Maliki madhhab: radd excludes spouses; certain residue and kalalah rules follow Malik's transmission."
        }
        FaraidhMadhhab.SHAFII -> when (language) {
            AppLanguage.INDONESIAN -> "Mazhab Syafi'i: radd mengecualikan pasangan; pembagian furud dan asabah mengikuti kitab Syafi'i yang digunakan di Nusantara."
            AppLanguage.MALAY -> "Mazhab Syafi'i: radd mengecualikan pasangan; pembahagian furud dan asabah mengikut kitab Syafi'i yang digunakan di rantau ini."
            AppLanguage.ENGLISH -> "Shafi'i madhhab: radd excludes spouses; furud and asabah follow the Shafi'i manuals widely used in Southeast Asia."
        }
        FaraidhMadhhab.HANBALI -> when (language) {
            AppLanguage.INDONESIAN -> "Mazhab Hanbali: radd mengecualikan pasangan; aturan hajb dan asabah mengikuti pandangan Ahmad bin Hanbal dan murid-muridnya."
            AppLanguage.MALAY -> "Mazhab Hanbali: radd mengecualikan pasangan; peraturan hajb dan asabah mengikut pandangan Ahmad bin Hanbal."
            AppLanguage.ENGLISH -> "Hanbali madhhab: radd excludes spouses; blocking and asabah rules follow Ahmad ibn Hanbal's school."
        }
    }

    suspend fun proofsForKeys(keys: List<String>, language: AppLanguage): List<FaraidhProofItem> {
        val bundle = loadBundle()
        val refIds = keys.flatMap { bundle.proofMappings[it].orEmpty() }.distinct()
        val items = mutableListOf<FaraidhProofItem>()
        keys.forEach { key ->
            if (key.startsWith("madhhab_")) {
                val madhhab = when (key) {
                    "madhhab_hanafi" -> FaraidhMadhhab.HANAFI
                    "madhhab_maliki" -> FaraidhMadhhab.MALIKI
                    "madhhab_shafii" -> FaraidhMadhhab.SHAFII
                    else -> FaraidhMadhhab.HANBALI
                }
                items += FaraidhProofItem(
                    id = key,
                    kind = FaraidhProofKind.NOTE,
                    title = when (language) {
                        AppLanguage.INDONESIAN -> "Catatan mazhab ${madhhabLabel(madhhab, language)}"
                        AppLanguage.MALAY -> "Nota mazhab ${madhhabLabel(madhhab, language)}"
                        AppLanguage.ENGLISH -> "${madhhabLabel(madhhab, language)} madhhab note"
                    },
                    body = madhhabNote(madhhab, language),
                    arabic = null,
                    externalUrl = null
                )
            }
            if (key == "faraidh_awl_note" || key == "faraidh_radd_note" || key == "faraidh_radd_note_hanafi" || key == "proof_out_of_wedlock_note") {
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
        "proof_out_of_wedlock_note" -> when (language) {
            AppLanguage.INDONESIAN -> "Nasab & Waris Anak luar nikah / Anak li'an"
            AppLanguage.MALAY -> "Nasab & Waris Anak luar nikah / Anak li'an"
            AppLanguage.ENGLISH -> "Lineage & Inheritance of Illegitimate/Li'an Children"
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
        "proof_out_of_wedlock_note" -> when (language) {
            AppLanguage.INDONESIAN -> "Berdasarkan syariat Islam, anak yang lahir di luar pernikahan yang sah atau anak li'an hanya dinasabkan kepada ibu kandungnya. Oleh karena itu, ia tidak mewarisi dari ayah biologis atau kerabat ayahnya, dan sebaliknya. Hubungan waris hanya berlaku dengan ibu dan kerabat ibunya."
            AppLanguage.MALAY -> "Berdasarkan syariat Islam, anak yang lahir di luar pernikahan yang sah atau anak li'an hanya dinasabkan kepada ibu kandungnya. Oleh itu, dia tidak mewarisi daripada bapa biologi atau kerabat bapanya, dan sebaliknya. Hubungan waris hanya terpakai dengan ibu dan kerabat ibunya."
            AppLanguage.ENGLISH -> "Under Islamic Shariah, a child born out of wedlock or a child of li'an is legally attributed only to the biological mother. Therefore, there is no mutual inheritance between the child and the biological father or his paternal relatives. Inheritance relations exist solely with the mother and her relatives."
        }
        "faraidh_radd_note_hanafi" -> when (language) {
            AppLanguage.INDONESIAN -> "Kelebihan harta dikembalikan (radd) kepada semua ahli waris yang berhak secara proporsional—termasuk suami/istri menurut mazhab Hanafi."
            AppLanguage.MALAY -> "Lebihan harta dikembalikan (radd) kepada semua waris yang layak secara proporsional—termasuk pasangan mengikut mazhab Hanafi."
            AppLanguage.ENGLISH -> "Surplus is redistributed (radd) proportionally among all eligible heirs—including spouses per the Hanafi madhhab."
        }
        else -> when (language) {
            AppLanguage.INDONESIAN -> "Kelebihan harta dikembalikan (radd) kepada ahli waris nasab secara proporsional—suami/istri dikecualikan menurut Maliki, Syafi'i, dan Hanbali."
            AppLanguage.MALAY -> "Lebihan harta dikembalikan (radd) kepada waris nasab secara proporsional—pasangan dikecualikan mengikut Maliki, Syafi'i, dan Hanbali."
            AppLanguage.ENGLISH -> "Surplus is redistributed (radd) proportionally among blood heirs—spouses excluded per Maliki, Shafi'i, and Hanbali."
        }
    }
}
