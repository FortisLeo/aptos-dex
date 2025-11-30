package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.blockchain.MarketPairs
import com.rishitgoklani.aptosdex.domain.model.DexResult
import com.rishitgoklani.aptosdex.domain.model.MarketPrice
import com.rishitgoklani.aptosdex.domain.repository.DexRepository
import javax.inject.Inject

/**
 * Use case for fetching current market price from DEX
 */
class GetMarketPriceUseCase @Inject constructor(
    private val dexRepository: DexRepository
) {
    /**
     * Get current market price for a symbol
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @return Market price or error
     */
    suspend operator fun invoke(symbol: String): DexResult<MarketPrice> {
        val (baseToken, quoteToken) = MarketPairs.getTypeArguments(symbol)

        return dexRepository.getMarketPrice(
            baseToken = baseToken,
            quoteToken = quoteToken
        )
    }
}
