package com.rishitgoklani.aptosdex.domain.model

/**
 * Represents an order in the CLOB (Central Limit Order Book)
 * Matches the Move struct: Order { order_id, user, side, price, size, filled_size, timestamp }
 */
data class AptosOrder(
    val orderId: Long,
    val user: String,
    val side: Boolean, // true = buy, false = sell
    val price: Double,
    val size: Double,
    val filledSize: Double,
    val timestamp: Long
)

/**
 * Represents a price level in the order book
 * Matches the Move struct: PriceLevel { price, orders, total_size }
 */
data class AptosPriceLevel(
    val price: Double,
    val orders: List<AptosOrder>,
    val totalSize: Double
)

/**
 * Represents the order book snapshot from Aptos smart contract
 * Contains aggregated data from OrderBook<Base, Quote> struct
 */
data class AptosOrderBookSnapshot(
    val marketPair: String,
    val buyOrders: List<AptosPriceLevel>, // buy_orders from contract
    val sellOrders: List<AptosPriceLevel>, // sell_orders from contract
    val nextOrderId: Long,
    val lastUpdateSequence: Long = 0L
)

/**
 * Represents a trade event emitted from the Aptos smart contract
 * Matches the Move struct: TradeEvent { maker_order_id, taker_order_id, price, size, timestamp }
 */
data class AptosTradeEvent(
    val eventSequenceNumber: Long,
    val makerOrderId: Long,
    val takerOrderId: Long,
    val price: Double,
    val size: Double,
    val timestamp: Long
)

/**
 * Result wrapper for order book snapshot operations
 */
sealed class AptosOrderBookResult {
    data class Success(val snapshot: AptosOrderBookSnapshot) : AptosOrderBookResult()
    data class Failure(val error: AptosOrderBookError) : AptosOrderBookResult()
}

/**
 * Error types for Aptos order book operations
 */
enum class AptosOrderBookError {
    NETWORK_ERROR,
    CONTRACT_NOT_FOUND,
    INVALID_MARKET_PAIR,
    VIEW_FUNCTION_FAILED,
    STREAM_CONNECTION_FAILED,
    UNKNOWN
}
