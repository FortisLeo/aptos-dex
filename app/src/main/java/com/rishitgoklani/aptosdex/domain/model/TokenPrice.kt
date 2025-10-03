package com.rishitgoklani.aptosdex.domain.model

/**
 * Domain model representing token price information
 * Pure Kotlin model with no platform dependencies
 */
data class TokenPrice(
    val symbol: String,
    val currentPrice: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val volume24h: Double,
    val high24h: Double,
    val low24h: Double
)

/**
 * Result wrapper for token price fetch operations
 */
sealed class TokenPriceResult {
    data class Success(val prices: List<TokenPrice>) : TokenPriceResult()
    data class Failure(val error: TokenPriceError) : TokenPriceResult()
}

/**
 * Token price error types
 */
enum class TokenPriceError {
    NETWORK_ERROR,
    INVALID_SYMBOL,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN
}
