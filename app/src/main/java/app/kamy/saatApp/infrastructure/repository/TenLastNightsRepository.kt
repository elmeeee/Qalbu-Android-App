package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class KimiNightQuoteItem(
    val night: Int,
    val quote: String,
    val description: String
)

data class LailatulQadrGuideItem(
    val title: String,
    val subtitle: String,
    val sections: List<LailatulQadrSectionItem>
)

data class LailatulQadrSectionItem(
    val id: String,
    val heading: String,
    val content: String,
    val quranReference: QuranRefResolved? = null,
    val hadithReference: HadithRefResolved? = null
)

data class QuranRefResolved(
    val verse: String,
    val reference: String,
    val translation: String
)

data class HadithRefResolved(
    val reference: String,
    val text: String
)

data class TenLastNightsDuaItem(
    val id: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val reference: String,
    val context: String,
    val translation: String
)

@Serializable
data class RawKimiQuoteDto(
    val night: Int = 21,
    val quote: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap()
)

@Serializable
data class RawLailatulQadrGuideDto(
    val title: Map<String, String> = emptyMap(),
    val subtitle: Map<String, String> = emptyMap(),
    val sections: List<RawLailatulQadrSectionDto> = emptyList()
)

@Serializable
data class RawLailatulQadrSectionDto(
    val id: String = "",
    val heading: Map<String, String> = emptyMap(),
    val content: Map<String, String> = emptyMap(),
    @SerialName("quran_reference") val quranReference: RawQuranRefDto? = null,
    @SerialName("hadith_reference") val hadithReference: RawHadithRefDto? = null
)

@Serializable
data class RawQuranRefDto(
    val verse: String = "",
    val reference: String = "",
    val translation: Map<String, String> = emptyMap()
)

@Serializable
data class RawHadithRefDto(
    val reference: String = "",
    val text: Map<String, String> = emptyMap()
)

@Serializable
data class RawTenLastNightsDuaDto(
    val id: String = "",
    val title: Map<String, String> = emptyMap(),
    val arabic: String = "",
    val transliteration: String = "",
    val reference: String = "",
    val context: Map<String, String> = emptyMap(),
    val translation: Map<String, String> = emptyMap()
)

@Singleton
class TenLastNightsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rawQuotes: List<RawKimiQuoteDto> = emptyList()
    private var rawGuide: RawLailatulQadrGuideDto? = null
    private var rawDuas: List<RawTenLastNightsDuaDto> = emptyList()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        loadAssets()
    }

    private fun loadAssets() {
        try {
            val quotesJson = context.assets.open("ten_last_nights_quotes.json").bufferedReader().use { it.readText() }
            rawQuotes = parseQuotesJson(quotesJson)
        } catch (_: Exception) {
            rawQuotes = emptyList()
        }

        try {
            val guideJson = context.assets.open("lailatul_qadr_guide.json").bufferedReader().use { it.readText() }
            rawGuide = parseGuideJson(guideJson)
        } catch (_: Exception) {
            rawGuide = null
        }

        try {
            val duasJson = context.assets.open("ten_last_nights_duas.json").bufferedReader().use { it.readText() }
            rawDuas = parseDuasJson(duasJson)
        } catch (_: Exception) {
            rawDuas = emptyList()
        }
    }

    fun getQuoteForNight(nightNumber: Int, language: AppLanguage): KimiNightQuoteItem {
        val effectiveNight = nightNumber.coerceIn(21, 30)
        return resolveQuote(rawQuotes, effectiveNight, language)
    }

    fun getLailatulQadrGuide(language: AppLanguage): LailatulQadrGuideItem {
        return resolveGuide(rawGuide, language)
    }

    fun getDuas(language: AppLanguage): List<TenLastNightsDuaItem> {
        return resolveDuas(rawDuas, language)
    }

    companion object {
        fun parseQuotesJson(jsonString: String): List<RawKimiQuoteDto> {
            val parser = Json { ignoreUnknownKeys = true; isLenient = true }
            return parser.decodeFromString(jsonString)
        }

        fun parseGuideJson(jsonString: String): RawLailatulQadrGuideDto {
            val parser = Json { ignoreUnknownKeys = true; isLenient = true }
            return parser.decodeFromString(jsonString)
        }

        fun parseDuasJson(jsonString: String): List<RawTenLastNightsDuaDto> {
            val parser = Json { ignoreUnknownKeys = true; isLenient = true }
            return parser.decodeFromString(jsonString)
        }

        fun resolveQuote(quotes: List<RawKimiQuoteDto>, night: Int, language: AppLanguage): KimiNightQuoteItem {
            val langKey = language.tag.lowercase()
            val raw = quotes.firstOrNull { it.night == night } ?: quotes.firstOrNull() ?: RawKimiQuoteDto()
            val quote = raw.quote[langKey] ?: raw.quote["id"] ?: raw.quote["en"] ?: "Pelan-pelan saja. Satu malam, satu langkah lebih dekat."
            val desc = raw.description[langKey] ?: raw.description["id"] ?: raw.description["en"] ?: "Mari isi malam ini dengan ibadah dan doa."
            return KimiNightQuoteItem(night = night, quote = quote, description = desc)
        }

        fun resolveGuide(rawGuide: RawLailatulQadrGuideDto?, language: AppLanguage): LailatulQadrGuideItem {
            val langKey = language.tag.lowercase()
            val guide = rawGuide ?: RawLailatulQadrGuideDto()
            val title = guide.title[langKey] ?: guide.title["id"] ?: guide.title["en"] ?: "Tentang Lailatul Qadr"
            val subtitle = guide.subtitle[langKey] ?: guide.subtitle["id"] ?: guide.subtitle["en"] ?: "Keutamaan, Tanda-tanda, dan Amalan yang Dianjurkan"

            val sections = guide.sections.map { section ->
                val heading = section.heading[langKey] ?: section.heading["id"] ?: section.heading["en"] ?: ""
                val content = section.content[langKey] ?: section.content["id"] ?: section.content["en"] ?: ""
                val quranRef = section.quranReference?.let { qr ->
                    val trans = qr.translation[langKey] ?: qr.translation["id"] ?: qr.translation["en"] ?: ""
                    QuranRefResolved(verse = qr.verse, reference = qr.reference, translation = trans)
                }
                val hadithRef = section.hadithReference?.let { hr ->
                    val text = hr.text[langKey] ?: hr.text["id"] ?: hr.text["en"] ?: ""
                    HadithRefResolved(reference = hr.reference, text = text)
                }
                LailatulQadrSectionItem(
                    id = section.id,
                    heading = heading,
                    content = content,
                    quranReference = quranRef,
                    hadithReference = hadithRef
                )
            }
            return LailatulQadrGuideItem(title = title, subtitle = subtitle, sections = sections)
        }

        fun resolveDuas(rawDuas: List<RawTenLastNightsDuaDto>, language: AppLanguage): List<TenLastNightsDuaItem> {
            val langKey = language.tag.lowercase()
            return rawDuas.map { dua ->
                val title = dua.title[langKey] ?: dua.title["id"] ?: dua.title["en"] ?: ""
                val context = dua.context[langKey] ?: dua.context["id"] ?: dua.context["en"] ?: ""
                val translation = dua.translation[langKey] ?: dua.translation["id"] ?: dua.translation["en"] ?: ""
                TenLastNightsDuaItem(
                    id = dua.id,
                    title = title,
                    arabic = dua.arabic,
                    transliteration = dua.transliteration,
                    reference = dua.reference,
                    context = context,
                    translation = translation
                )
            }
        }
    }
}
