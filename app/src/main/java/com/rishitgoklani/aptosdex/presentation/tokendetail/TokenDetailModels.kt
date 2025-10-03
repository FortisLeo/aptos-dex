package com.rishitgoklani.aptosdex.presentation.tokendetail

/**
 * Candlestick chart data model
 * Represents OHLC (Open, High, Low, Close) data for a specific time period
 */
data class CandlestickData(
    val time: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float
)

/**
 * Order book data containing bids and asks
 */
data class OrderBookData(
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>
)

/**
 * Single order book entry
 * Represents a buy or sell order at a specific price
 */
data class OrderBookEntry(
    val price: String,
    val amount: String,
    val total: String
)
