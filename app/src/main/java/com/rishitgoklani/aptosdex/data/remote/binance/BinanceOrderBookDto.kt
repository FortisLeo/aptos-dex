package com.rishitgoklani.aptosdex.data.remote.binance

import com.google.gson.annotations.SerializedName

/**
 * Binance Order Book Snapshot DTO
 * GET /api/v3/depth
 */
data class BinanceOrderBookSnapshotDto(
    @SerializedName("lastUpdateId") val lastUpdateId: Long,
    @SerializedName("bids") val bids: List<List<String>>,  // [price, quantity]
    @SerializedName("asks") val asks: List<List<String>>   // [price, quantity]
)
