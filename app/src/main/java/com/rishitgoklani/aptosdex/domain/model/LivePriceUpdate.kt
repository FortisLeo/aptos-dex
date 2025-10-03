package com.rishitgoklani.aptosdex.domain.model

/**
 * Domain model for live price updates from WebSocket
 */
data class LivePriceUpdate(
    val symbol: String,
    val lastPrice: Double,
    val priceChange: Double,
    val priceChangePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val timestamp: Long
)

/**
 * Domain model for order book snapshot
 */
data class OrderBookSnapshot(
    val symbol: String,
    val lastUpdateId: Long,
    val bids: List<OrderBookLevel>,
    val asks: List<OrderBookLevel>
)

/**
 * Domain model for order book updates (WebSocket deltas)
 */
data class OrderBookUpdate(
    val symbol: String,
    val firstUpdateId: Long,
    val finalUpdateId: Long,
    val bids: List<OrderBookLevel>,
    val asks: List<OrderBookLevel>,
    val timestamp: Long
)

/**
 * Single price level in order book
 */
data class OrderBookLevel(
    val price: Double,
    val quantity: Double
)

/**
 * Result wrapper for order book operations
 */
sealed class OrderBookResult {
    data class Success(val snapshot: OrderBookSnapshot) : OrderBookResult()
    data class Failure(val error: OrderBookError) : OrderBookResult()
}

enum class OrderBookError {
    NETWORK_ERROR,
    INVALID_SYMBOL,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN
}
