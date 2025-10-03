package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult

/**
 * Repository interface for fetching token price data
 * Domain layer - no platform dependencies
 */
interface TokenPriceRepository {
    /**
     * Fetches 24hr price statistics for multiple token symbols
     *
     * @param symbols List of token symbols (e.g., ["APT", "BTC", "USDT"])
     * @return TokenPriceResult containing list of token prices or error
     */
    suspend fun fetch24hrPrices(symbols: List<String>): TokenPriceResult
}
