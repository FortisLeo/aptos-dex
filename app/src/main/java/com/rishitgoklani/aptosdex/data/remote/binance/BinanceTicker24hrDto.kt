package com.rishitgoklani.aptosdex.data.remote.binance

import com.google.gson.annotations.SerializedName

/**
 * Binance 24hr Ticker Statistics Response
 * Endpoint: GET /api/v3/ticker/24hr
 *
 * Returns price change statistics for a 24-hour rolling window
 */
data class BinanceTicker24hrDto(
    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("priceChange")
    val priceChange: String,

    @SerializedName("priceChangePercent")
    val priceChangePercent: String,

    @SerializedName("lastPrice")
    val lastPrice: String,

    @SerializedName("volume")
    val volume: String,

    @SerializedName("quoteVolume")
    val quoteVolume: String,

    @SerializedName("openPrice")
    val openPrice: String,

    @SerializedName("highPrice")
    val highPrice: String,

    @SerializedName("lowPrice")
    val lowPrice: String
)
