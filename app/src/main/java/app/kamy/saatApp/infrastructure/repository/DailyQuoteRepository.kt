package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class DailyQuoteItem(
    val id: Int,
    val dayOfYear: Int,
    val category: String,
    val theme: String,
    val quoteText: String,
    val referenceLabel: String,
    val mascot: String = "mascot_prayer"
) {
    @androidx.annotation.DrawableRes
    fun getMascotDrawable(): Int = when (mascot) {
        "mascot_prayer" -> app.kamy.saatApp.R.drawable.mascot_prayer
        "mascot_reading" -> app.kamy.saatApp.R.drawable.mascot_reading
        "mascot_lentera" -> app.kamy.saatApp.R.drawable.mascot_lentera
        "mascot_bawa_lentera" -> app.kamy.saatApp.R.drawable.mascot_bawa_lentera
        "mascot_ramadan" -> app.kamy.saatApp.R.drawable.mascot_ramadan
        "mascot_eidfitri" -> app.kamy.saatApp.R.drawable.mascot_eidfitri
        "mascot_eidadha" -> app.kamy.saatApp.R.drawable.mascot_eidadha
        "mascot_umrah" -> app.kamy.saatApp.R.drawable.mascot_umrah
        "mascot_ilmu" -> app.kamy.saatApp.R.drawable.mascot_ilmu
        "mascot_smile" -> app.kamy.saatApp.R.drawable.mascot_smile
        "mascot_happysad" -> app.kamy.saatApp.R.drawable.mascot_happysad
        "mascot_khilafah" -> app.kamy.saatApp.R.drawable.mascot_khilafah
        "mascot_toa" -> app.kamy.saatApp.R.drawable.mascot_toa
        else -> app.kamy.saatApp.R.drawable.mascot_prayer
    }
}

@Singleton
class DailyQuoteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val khgtCalendarRepository: KhgtCalendarRepository? = null
) {
    private var quotesList: List<DailyQuoteItemRaw> = emptyList()

    private data class DailyQuoteItemRaw(
        val id: Int,
        val dayOfYear: Int,
        val category: String,
        val theme: String,
        val referenceMap: Map<String, String>,
        val quoteMap: Map<String, String>,
        val mascot: String
    )

    init {
        loadQuotesFromAssets()
    }

    private fun loadQuotesFromAssets() {
        runCatching {
            val jsonString = context.assets.open("daily_quotes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<DailyQuoteItemRaw>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optInt("id", i + 1)
                val dayOfYear = obj.optInt("day_of_year", i + 1)
                val category = obj.optString("category", "QURAN")
                val theme = obj.optString("theme", "GENERAL")
                val mascot = obj.optString("mascot", "mascot_prayer")

                val refObj = obj.optJSONObject("reference")
                val refMap = mapOf(
                    "id" to (refObj?.optString("id") ?: "Qur'an"),
                    "ms" to (refObj?.optString("ms") ?: "Al-Quran"),
                    "en" to (refObj?.optString("en") ?: "Qur'an")
                )

                val qtObj = obj.optJSONObject("quote")
                val qtMap = mapOf(
                    "id" to (qtObj?.optString("id") ?: ""),
                    "ms" to (qtObj?.optString("ms") ?: ""),
                    "en" to (qtObj?.optString("en") ?: "")
                )

                list.add(DailyQuoteItemRaw(id, dayOfYear, category, theme, refMap, qtMap, mascot))
            }
            quotesList = list
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun getTodayQuote(
        language: AppLanguage,
        eventTitle: String? = null,
        hijriLabel: String? = null
    ): DailyQuoteItem {
        val date = Calendar.getInstance()
        val dayOfYear = date.get(Calendar.DAY_OF_YEAR)
        return getQuoteForDay(
            dayOfYear = dayOfYear,
            language = language,
            eventTitle = eventTitle,
            hijriLabel = hijriLabel,
            isFriday = date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        )
    }

    fun getQuoteForDay(
        dayOfYear: Int,
        language: AppLanguage,
        eventTitle: String? = null,
        hijriLabel: String? = null,
        isFriday: Boolean = false
    ): DailyQuoteItem {
        if (quotesList.isEmpty()) {
            loadQuotesFromAssets()
        }

        val event = eventTitle.orEmpty().lowercase()
        val hijri = hijriLabel.orEmpty().lowercase()

        // Match KHGT (Kalender Hijriah Global Tunggal) events or Friday
        val matchedTheme = when {
            event.contains("ramadan") || event.contains("ramadhan") || event.contains("nuzulul") || hijri.contains("ramadan") || hijri.contains("ramadhan") -> "RAMADAN"
            event.contains("fitri") || event.contains("eid al-fitr") || (hijri.contains("syawal") && (hijri.startsWith("1 ") || hijri.startsWith("2 "))) -> "EID_FITR"
            event.contains("adha") || event.contains("kurban") || event.contains("korban") || event.contains("tasyrik") || (hijri.contains("zulhijjah") && (hijri.contains("10") || hijri.contains("11") || hijri.contains("12") || hijri.contains("13"))) -> "EID_ADHA"
            event.contains("arafah") || event.contains("haji") || event.contains("hajj") || (hijri.contains("zulhijjah") && (hijri.contains("8") || hijri.contains("9"))) -> "HAJJ"
            event.contains("isra") || event.contains("mi'raj") || (hijri.contains("rajab") && hijri.contains("27")) -> "LIGHT"
            event.contains("maulid") || event.contains("nabi") || (hijri.contains("rabi") && hijri.contains("12")) -> "KNOWLEDGE"
            event.contains("asyura") || event.contains("muharram") || (hijri.contains("muharram") && (hijri.contains("9") || hijri.contains("10"))) -> "PATIENCE"
            isFriday -> "PRAYER"
            else -> null
        }

        val raw = (if (matchedTheme != null) quotesList.find { it.theme == matchedTheme } else null)
            ?: quotesList.find { it.dayOfYear == dayOfYear }
            ?: quotesList.getOrNull((dayOfYear - 1) % quotesList.size.coerceAtLeast(1))
            ?: DailyQuoteItemRaw(
                id = 1,
                dayOfYear = dayOfYear,
                category = "QURAN",
                theme = "GENERAL",
                referenceMap = mapOf("id" to "Qur'an 15:99", "ms" to "Al-Quran 15:99", "en" to "Qur'an 15:99"),
                quoteMap = mapOf(
                    "id" to "Dan sembahlah Tuhanmu sampai datang kepadamu keyakinan (ajal).",
                    "ms" to "Dan sembahlah Tuhanmu sehingga datang kepadamu keyakinan (ajal).",
                    "en" to "And worship your Lord until there comes to you the certainty (i.e. death)."
                ),
                mascot = "mascot_prayer"
            )

        val langKey = when (language) {
            AppLanguage.MALAY -> "ms"
            AppLanguage.ENGLISH -> "en"
            else -> "id"
        }

        val quoteText = raw.quoteMap[langKey] ?: raw.quoteMap["id"] ?: raw.quoteMap["en"].orEmpty()
        val refLabel = raw.referenceMap[langKey] ?: raw.referenceMap["id"] ?: raw.referenceMap["en"].orEmpty()

        return DailyQuoteItem(
            id = raw.id,
            dayOfYear = raw.dayOfYear,
            category = raw.category,
            theme = raw.theme,
            quoteText = "\"$quoteText\"",
            referenceLabel = refLabel,
            mascot = raw.mascot
        )
    }
}
