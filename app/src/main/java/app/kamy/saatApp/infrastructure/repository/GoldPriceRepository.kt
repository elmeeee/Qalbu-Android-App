package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.domain.tools.GoldPriceQuote
import app.kamy.saatApp.domain.tools.ZakatCalculator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Singleton
class GoldPriceRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchQuote(currency: String = "IDR"): GoldPriceQuote? = withContext(Dispatchers.IO) {
        runCatching {
            fetchQuoteFromLogamMulia() ?: fetchQuoteFallback(currency)
        }.getOrNull()
    }

    private fun fetchQuoteFromLogamMulia(): GoldPriceQuote? {
        val request = Request.Builder()
            .url("https://logam-mulia-api.iamutaki.workers.dev/api/prices/hargaemas-net")
            .get()
            .build()

        val body = client.newCall(request).execute().body?.string() ?: return null
        val json = JSONObject(body)
        if (!json.optBoolean("success", false)) return null
        val data = json.optJSONArray("data") ?: return null
        val goldPerGramLocal = parseGramPrice(data, "gold") ?: return null
        val silverPerGramLocal = parseGramPrice(data, "silver") ?: ZakatCalculator.silverPriceFromGold(goldPerGramLocal)

        return GoldPriceQuote(
            goldPerGramIdr = goldPerGramLocal,
            silverPerGramIdr = silverPerGramLocal,
            sourceLabel = "Logam Mulia / Harga Emas.net",
            fetchedAtMillis = System.currentTimeMillis()
        )
    }

    private fun parseGramPrice(data: JSONArray, material: String): Double? {
        var fallbackWeight: Double? = null
        var fallbackPrice: Double? = null

        for (i in 0 until data.length()) {
            val item = data.optJSONObject(i) ?: continue
            if (item.optString("material") != material) continue
            if (item.optString("weightUnit") != "gr") continue
            val weight = item.optDouble("weight", -1.0)
            val sellPrice = item.optDouble("sellPrice", -1.0)
            if (weight <= 0.0 || sellPrice <= 0.0) continue
            if (weight == 1.0) {
                return sellPrice
            }
            if (fallbackWeight == null || weight < fallbackWeight) {
                fallbackWeight = weight
                fallbackPrice = sellPrice
            }
        }

        return if (fallbackWeight != null && fallbackPrice != null) {
            fallbackPrice / fallbackWeight
        } else {
            null
        }
    }

    private fun fetchQuoteFallback(currency: String): GoldPriceQuote? {
        val goldUsdOz = fetchGoldUsdPerTroyOz() ?: return null
        val usdRate = fetchUsdRate(currency) ?: return null
        val goldPerGramLocal = ZakatCalculator.goldUsdPerGram(goldUsdOz) * usdRate
        val silverPerGramLocal = goldPerGramLocal / 80.0
        return GoldPriceQuote(
            goldPerGramIdr = goldPerGramLocal,
            silverPerGramIdr = silverPerGramLocal,
            sourceLabel = "metals.live + open.er-api.com",
            fetchedAtMillis = System.currentTimeMillis()
        )
    }

    private fun fetchGoldUsdPerTroyOz(): Double? {
        val request = Request.Builder()
            .url("https://api.metals.live/v1/spot/gold")
            .get()
            .build()
        val body = client.newCall(request).execute().body?.string() ?: return null
        val array = JSONArray(body)
        if (array.length() == 0) return null
        val row = array.getJSONArray(0)
        return row.optDouble(1).takeIf { it > 0.0 }
    }

    private fun fetchUsdRate(targetCurrency: String): Double? {
        val request = Request.Builder()
            .url("https://open.er-api.com/v6/latest/USD")
            .get()
            .build()
        val body = client.newCall(request).execute().body?.string() ?: return null
        val json = JSONObject(body)
        if (json.optString("result") != "success") return null
        val rates = json.optJSONObject("rates") ?: return null
        return rates.optDouble(targetCurrency).takeIf { it > 0.0 }
    }
}
