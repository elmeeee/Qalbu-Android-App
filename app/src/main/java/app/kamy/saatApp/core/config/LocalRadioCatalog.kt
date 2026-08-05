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
        get() = cachedStations ?: fallbackStations

    fun getStations(context: Context? = null): List<QuranRadioStation> {
        cachedStations?.let { return it }

        if (context != null) {
            val loaded = loadFromAssets(context)
            if (loaded.isNotEmpty()) {
                cachedStations = loaded
                return loaded
            }
        }
        return fallbackStations
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

    private val fallbackStations: List<QuranRadioStation> = listOf(
        QuranRadioStation(
            id = "suara_muslim_id",
            name = "Suara Muslim Surabaya",
            countryEn = "Indonesia",
            countryId = "Indonesia",
            countryMs = "Indonesia",
            countryFlag = "🇮🇩",
            descriptionEn = "Islamic Broadcast & Quran Recitation",
            descriptionId = "Radio Dakwah Islamiyyah & Murottal",
            descriptionMs = "Radio Dakwah Islamiyyah & Murottal",
            category = RadioCategory.INDONESIA,
            streamUrl = "https://pu.klikhost.com/proxy/suaramuslim/stream"
        ),
        QuranRadioStation(
            id = "ikim_my",
            name = "IKIM FM",
            countryEn = "Malaysia",
            countryId = "Malaysia",
            countryMs = "Malaysia",
            countryFlag = "🇲🇾",
            descriptionEn = "Islamic Understanding Institute Malaysia",
            descriptionId = "Institut Kefahaman Islam Malaysia",
            descriptionMs = "Institut Kefahaman Islam Malaysia",
            category = RadioCategory.MALAYSIA,
            streamUrl = "https://ais-sa8.cdnstream1.com/5035/playlist.m3u8"
        ),
        QuranRadioStation(
            id = "warna_sg",
            name = "Warna 94.2 FM",
            countryEn = "Singapore",
            countryId = "Singapura",
            countryMs = "Singapura",
            countryFlag = "🇸🇬",
            descriptionEn = "Mediacorp Singapore (Islamic & Malay Broadcast)",
            descriptionId = "Mediacorp Singapore (Siaran Islami & Kebudayaan Melayu)",
            descriptionMs = "Mediacorp Singapore (Siaran Islami & Kebudayaan Melayu)",
            category = RadioCategory.SINGAPORE,
            streamUrl = "https://22393.live.streamtheworld.com:443/WARNA942FM_PREM.aac"
        ),
        QuranRadioStation(
            id = "asyik_bn",
            name = "Asyik FM",
            countryEn = "Brunei / Regional",
            countryId = "Brunei / Regional",
            countryMs = "Brunei / Regional",
            countryFlag = "🇧🇳",
            descriptionEn = "Islamic & Malay Regional Broadcast",
            descriptionId = "Siaran Islamiyyah & Kebudayaan Melayu Nusantara",
            descriptionMs = "Siaran Islamiyyah & Kebudayaan Melayu Nusantara",
            category = RadioCategory.BRUNEI,
            streamUrl = "https://28163.live.streamtheworld.com:443/ASYIK_FMAAC_SC"
        ),
        QuranRadioStation(
            id = "cairo_quran",
            name = "Cairo Quran Live",
            countryEn = "Egypt / Global",
            countryId = "Mesir / Global",
            countryMs = "Mesir / Global",
            countryFlag = "🇪🇬",
            descriptionEn = "Quran Recitation from Cairo",
            descriptionId = "Lantunan Al-Qur'an dari Kairo",
            descriptionMs = "Lantunan Al-Qur'an dari Kairo",
            category = RadioCategory.MUROTTAL_GLOBAL,
            streamUrl = "https://stream.radiojar.com/8s5u5tpdtwzuv"
        )
    )
}
