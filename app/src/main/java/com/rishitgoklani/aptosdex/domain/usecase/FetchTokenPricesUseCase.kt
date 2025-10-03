package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult
import com.rishitgoklani.aptosdex.domain.repository.TokenPriceRepository
import javax.inject.Inject

/**
 * Use case for fetching token prices from Binance API
 * Follows Clean Architecture - pure business logic
 */
class FetchTokenPricesUseCase @Inject constructor(
    private val tokenPriceRepository: TokenPriceRepository
) {
    /**
     * Fetches 24hr price statistics for given token symbols
     *
     * @param symbols List of token symbols to fetch prices for
     * @return TokenPriceResult with success or failure
     */
    suspend operator fun invoke(symbols: List<String>): TokenPriceResult {
        require(symbols.isNotEmpty()) { "Symbols list cannot be empty" }
        return tokenPriceRepository.fetch24hrPrices(symbols)
    }
}
