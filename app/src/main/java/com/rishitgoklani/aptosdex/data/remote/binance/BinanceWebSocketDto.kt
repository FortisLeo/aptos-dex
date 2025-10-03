package com.rishitgoklani.aptosdex.data.remote.binance

import com.google.gson.annotations.SerializedName

/**
 * Binance WebSocket Ticker DTO
 * Individual Symbol Ticker Stream (@ticker)
 * Provides rolling 24hr window market summary
 */
data class BinanceTickerStreamDto(
    @SerializedName("e") val eventType: String,           // Event type (24hrTicker)
    @SerializedName("E") val eventTime: Long,              // Event time
    @SerializedName("s") val symbol: String,               // Symbol
    @SerializedName("p") val priceChange: String,          // Price change
    @SerializedName("P") val priceChangePercent: String,   // Price change percent
    @SerializedName("w") val weightedAvgPrice: String,     // Weighted average price
    @SerializedName("c") val lastPrice: String,            // Last price
    @SerializedName("Q") val lastQuantity: String,         // Last quantity
    @SerializedName("o") val openPrice: String,            // Open price
    @SerializedName("h") val highPrice: String,            // High price
    @SerializedName("l") val lowPrice: String,             // Low price
    @SerializedName("v") val totalTradedBaseVolume: String, // Total traded base asset volume
    @SerializedName("q") val totalTradedQuoteVolume: String, // Total traded quote asset volume
    @SerializedName("n") val numberOfTrades: Long           // Number of trades
)

/**
 * Binance WebSocket Depth Stream DTO
 * Diff. Depth Stream (@depth)
 * Provides order book updates
 */
data class BinanceDepthStreamDto(
    @SerializedName("e") val eventType: String,     // Event type (depthUpdate)
    @SerializedName("E") val eventTime: Long,        // Event time
    @SerializedName("s") val symbol: String,         // Symbol
    @SerializedName("U") val firstUpdateId: Long,    // First update ID in event
    @SerializedName("u") val finalUpdateId: Long,    // Final update ID in event
    @SerializedName("b") val bids: List<List<String>>, // Bids to be updated [price, quantity]
    @SerializedName("a") val asks: List<List<String>>  // Asks to be updated [price, quantity]
)
