package com.rishitgoklani.aptosdex.domain.model

/**
 * Result types for DEX operations
 */
sealed class DexResult<out T> {
    data class Success<T>(val data: T) : DexResult<T>()
    data class Failure(val error: DexError) : DexResult<Nothing>()
}

/**
 * DEX error types
 */
enum class DexError {
    NOT_INITIALIZED,
    ALREADY_INITIALIZED,
    MARKET_NOT_ACTIVE,
    TOKEN_NOT_TRADEABLE,
    INSUFFICIENT_BALANCE,
    INVALID_ORDER,
    UNAUTHORIZED,
    DEX_PAUSED,
    NETWORK_ERROR,
    UNKNOWN
}

/**
 * User balance in DEX vault
 */
data class DexBalance(
    val availableBalance: Long,
    val lockedBalance: Long,
    val tokenType: String
)

/**
 * DEX statistics
 */
data class DexStats(
    val totalTrades: Long,
    val totalVolume: Double,
    val totalFees: Double,
    val activeUsers: Long
)

/**
 * Market price information
 */
data class MarketPrice(
    val baseToken: String,
    val quoteToken: String,
    val price: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Bid-ask spread information
 */
data class Spread(
    val bid: Double,
    val ask: Double,
    val spread: Double
)

/**
 * Order placement result
 */
data class OrderPlacementResult(
    val orderId: Long,
    val transactionHash: String
)

/**
 * Transaction result
 */
data class TransactionResult(
    val hash: String,
    val success: Boolean,
    val vmStatus: String? = null,
    val gasUsed: Long? = null
)
