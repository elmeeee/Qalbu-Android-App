package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.domain.model.AsmaulHusnaItem
import org.json.JSONArray

object LocalAsmaulHusnaCatalog {

    private var cachedItems: List<AsmaulHusnaItem>? = null

    fun getItems(context: Context): List<AsmaulHusnaItem> {
        cachedItems?.let { return it }
        val items = loadFromAssets(context)
        if (items.isNotEmpty()) {
            cachedItems = items
        }
        return items
    }

    private fun loadFromAssets(context: Context): List<AsmaulHusnaItem> = runCatching {
        val jsonText = context.assets.open("asmaul_husna/asmaul_husna.json")
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        val list = mutableListOf<AsmaulHusnaItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                AsmaulHusnaItem(
                    number = obj.getInt("number"),
                    arabic = obj.getString("arabic"),
                    latin = obj.getString("latin"),
                    meaningEn = obj.getString("meaningEn"),
                    meaningId = obj.getString("meaningId"),
                    meaningMs = obj.getString("meaningMs"),
                    dalilEn = obj.getString("dalilEn"),
                    dalilId = obj.getString("dalilId"),
                    dalilMs = obj.getString("dalilMs"),
                    dalilReference = obj.getString("dalilReference"),
                    fadhilahEn = obj.getString("fadhilahEn"),
                    fadhilahId = obj.getString("fadhilahId"),
                    fadhilahMs = obj.getString("fadhilahMs"),
                    recommendedCount = obj.optInt("recommendedCount", 100)
                )
            )
        }
        list
    }.getOrDefault(emptyList())
}
