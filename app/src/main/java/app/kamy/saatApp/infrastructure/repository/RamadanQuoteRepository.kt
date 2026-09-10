package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class RamadanQuoteItem(
    val day: Int,
    val sourceType: String,
    val reference: String,
    val quote: String
)

@Serializable
internal data class RawRamadanQuoteDto(
    val day: Int = 1,
    @SerialName("source_type") val sourceType: String = "QURAN",
    val reference: Map<String, String> = emptyMap(),
    val quote: Map<String, String> = emptyMap()
)

@Singleton
class RamadanQuoteRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var quotes: List<RawRamadanQuoteDto> = emptyList()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        loadQuotes()
    }

    private fun loadQuotes() {
        runCatching {
            val jsonString = context.assets.open("ramadan_quotes.json").bufferedReader().use { it.readText() }
            quotes = parseQuotesJson(jsonString, jsonParser)
        }.onFailure {
            it.printStackTrace()
        }
    }

    companion object {
        private val defaultJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        internal fun parseQuotesJson(jsonString: String, parser: Json = defaultJson): List<RawRamadanQuoteDto> {
            return try {
                parser.decodeFromString<List<RawRamadanQuoteDto>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }

        internal fun resolveQuote(quotes: List<RawRamadanQuoteDto>, dayNumber: Int, language: AppLanguage): RamadanQuoteItem {
            val targetDay = dayNumber.coerceIn(1, 30)
            val raw = quotes.firstOrNull { it.day == targetDay } ?: quotes.firstOrNull()
            val langKey = when (language) {
                AppLanguage.INDONESIAN -> "id"
                AppLanguage.MALAY -> "ms"
                AppLanguage.ENGLISH -> "en"
            }
            return RamadanQuoteItem(
                day = raw?.day ?: targetDay,
                sourceType = raw?.sourceType ?: "QURAN",
                reference = raw?.reference?.get(langKey)?.ifBlank { null }
                    ?: raw?.reference?.get("en")
                    ?: "Al-Baqarah 2:184",
                quote = raw?.quote?.get(langKey)?.ifBlank { null }
                    ?: raw?.quote?.get("en")
                    ?: "And fast, it is better for you if you only knew."
            )
        }
    }

    fun getQuoteForDay(dayNumber: Int, language: AppLanguage): RamadanQuoteItem {
        return resolveQuote(quotes, dayNumber, language)
    }
}
