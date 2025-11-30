package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.blockchain.CoinTypes
import com.rishitgoklani.aptosdex.blockchain.MarketPairs
import com.rishitgoklani.aptosdex.domain.model.DexResult
import com.rishitgoklani.aptosdex.domain.model.OrderPlacementResult
import com.rishitgoklani.aptosdex.domain.repository.DexRepository
import javax.inject.Inject

/**
 * Use case for placing limit orders on the DEX
 * Follows clean architecture principles
 */
class PlaceLimitOrderUseCase @Inject constructor(
    private val dexRepository: DexRepository
) {
    /**
     * Place a limit order
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @param isBuy true for buy order, false for sell order
     * @param price Price in quote token (USDC)
     * @param size Amount in base token
     * @return Result with order ID or error
     */
    suspend operator fun invoke(
        symbol: String,
        isBuy: Boolean,
        price: Double,
        size: Double
    ): DexResult<OrderPlacementResult> {
        // Get type arguments for the market pair
        val (baseToken, quoteToken) = MarketPairs.getTypeArguments(symbol)

        // Convert to microunits (assuming 6 decimals)
        val priceInMicrounits = (price * 1_000_000).toLong()
        val sizeInMicrounits = (size * 1_000_000).toLong()

        // Market ID - in a real app, this should be looked up from configuration
        val marketId = 0L

        // Side: false = buy, true = sell
        val side = !isBuy

        return dexRepository.placeLimitOrder(
            baseToken = baseToken,
            quoteToken = quoteToken,
            marketId = marketId,
            side = side,
            price = priceInMicrounits,
            size = sizeInMicrounits
        )
    }
}
