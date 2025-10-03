package com.rishitgoklani.aptosdex.domain.model

import java.util.UUID

/**
 * Represents a trading order in the orderbook
 */
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val symbol: String, // e.g., "APT"
    val side: OrderSide,
    val type: OrderType,
    val price: Double,
    val amount: Double,
    val total: Double = price * amount,
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val walletAddress: String
) {
    val isBuy: Boolean get() = side == OrderSide.BUY
    val isSell: Boolean get() = side == OrderSide.SELL
    val isLimit: Boolean get() = type == OrderType.LIMIT
    val isMarket: Boolean get() = type == OrderType.MARKET
}

enum class OrderSide {
    BUY, SELL
}

enum class OrderType {
    LIMIT, MARKET
}

enum class OrderStatus {
    PENDING,    // Order placed but not filled
    PARTIAL,    // Partially filled
    FILLED,     // Completely filled
    CANCELLED,  // Cancelled by user
    REJECTED    // Rejected by system
}

/**
 * Represents the orderbook data structure
 */
data class OrderBook(
    val symbol: String,
    val buyOrders: List<Order> = emptyList(),
    val sellOrders: List<Order> = emptyList(),
    val lastPrice: Double? = null,
    val priceChange24h: Double? = null
) {
    val bestBid: Double? get() = buyOrders.maxOfOrNull { it.price }
    val bestAsk: Double? get() = sellOrders.minOfOrNull { it.price }
    val spread: Double? get() {
        val bid = bestBid
        val ask = bestAsk
        return if (bid != null && ask != null) ask - bid else null
    }
}
