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
            val goldUsdOz = fetchGoldUsdPerTroyOz() ?: return@runCatching null
            val usdRate = fetchUsdRate(currency) ?: return@runCatching null
            val goldPerGramLocal = ZakatCalculator.goldUsdPerGram(goldUsdOz) * usdRate
            val silverPerGramLocal = goldPerGramLocal / 80.0
            GoldPriceQuote(
                goldPerGramIdr = goldPerGramLocal,
                silverPerGramIdr = silverPerGramLocal,
                sourceLabel = "metals.live + open.er-api.com",
                fetchedAtMillis = System.currentTimeMillis()
            )
        }.getOrNull()
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
