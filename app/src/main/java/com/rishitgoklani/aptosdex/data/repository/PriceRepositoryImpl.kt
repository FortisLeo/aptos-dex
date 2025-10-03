package com.rishitgoklani.aptosdex.data.repository

import com.rishitgoklani.aptosdex.domain.repository.PriceRepository
import com.rishitgoklani.aptosdex.presentation.swap.PythFeeds
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : PriceRepository {

    override suspend fun getUsdPrices(fromSymbol: String, toSymbol: String): Pair<Double, Double> {
        val fromId = PythFeeds.idFor(fromSymbol) ?: return 0.0 to 0.0
        val toId = PythFeeds.idFor(toSymbol) ?: return 0.0 to 0.0
        val url = "https://hermes.pyth.network/v2/updates/price/latest?ids%5B%5D=$fromId&ids%5B%5D=$toId"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .build()
        okHttpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return 0.0 to 0.0
            val body = resp.body?.string().orEmpty()
            val parsed = JSONObject(body).optJSONArray("parsed") ?: return 0.0 to 0.0
            var fromPrice = 0.0
            var toPrice = 0.0
            for (i in 0 until parsed.length()) {
                val obj = parsed.getJSONObject(i)
                val id = obj.getString("id")
                val priceObj = obj.getJSONObject("price")
                val price = priceObj.getString("price").toLong()
                val expo = priceObj.getInt("expo")
                val value = price * Math.pow(10.0, expo.toDouble())
                if (id == fromId) fromPrice = value
                if (id == toId) toPrice = value
            }
            if (toPrice == 0.0) toPrice = 1.0
            return fromPrice to toPrice
        }
    }
}


