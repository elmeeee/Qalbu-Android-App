package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import org.json.JSONArray

enum class RadioCategory(val labelEn: String, val labelId: String, val labelMs: String) {
    ALL("All", "Semua", "Semua"),
    MALAYSIA("Malaysia", "Malaysia", "Malaysia"),
    INDONESIA("Indonesia", "Indonesia", "Indonesia"),
    SINGAPORE("Singapore", "Singapura", "Singapura"),
    BRUNEI("Brunei", "Brunei", "Brunei"),
    MUROTTAL_GLOBAL("Murottal 24/7", "Murottal 24/7", "Murottal 24/7");

    fun label(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> labelEn
        AppLanguage.MALAY -> labelMs
        AppLanguage.INDONESIAN -> labelId
    }

    companion object {
        fun fromName(name: String): RadioCategory = entries.firstOrNull { it.name == name } ?: ALL
    }
}

data class QuranRadioStation(
    val id: String,
    val name: String,
    val countryEn: String,
    val countryId: String,
    val countryMs: String,
    val countryFlag: String,
    val descriptionEn: String,
    val descriptionId: String,
    val descriptionMs: String,
    val category: RadioCategory,
    val streamUrl: String
) {
    fun country(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> countryEn
        AppLanguage.MALAY -> countryMs
        AppLanguage.INDONESIAN -> countryId
    }

    fun description(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> descriptionEn
        AppLanguage.MALAY -> descriptionMs
        AppLanguage.INDONESIAN -> descriptionId
    }
}

object LocalRadioCatalog {

    private var cachedStations: List<QuranRadioStation>? = null

    val stations: List<QuranRadioStation>
        get() = cachedStations ?: emptyList()

    fun getStations(context: Context? = null): List<QuranRadioStation> {
        cachedStations?.let { return it }

        if (context != null) {
            val loaded = loadFromAssets(context)
            if (loaded.isNotEmpty()) {
                cachedStations = loaded
                return loaded
            }
        }
        return emptyList()
    }

    private fun loadFromAssets(context: Context): List<QuranRadioStation> = runCatching {
        val jsonText = context.assets.open("radio/radio_stations.json")
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        val list = mutableListOf<QuranRadioStation>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                QuranRadioStation(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    countryEn = obj.getString("countryEn"),
                    countryId = obj.getString("countryId"),
                    countryMs = obj.getString("countryMs"),
                    countryFlag = obj.getString("countryFlag"),
                    descriptionEn = obj.getString("descriptionEn"),
                    descriptionId = obj.getString("descriptionId"),
                    descriptionMs = obj.getString("descriptionMs"),
                    category = RadioCategory.fromName(obj.optString("category", "ALL")),
                    streamUrl = obj.getString("streamUrl")
                )
            )
        }
        list
    }.getOrDefault(emptyList())
}
