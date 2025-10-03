package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.OrderBookUpdate
import com.rishitgoklani.aptosdex.domain.repository.LivePriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for subscribing to order book updates via WebSocket
 */
class SubscribeToOrderBookUseCase @Inject constructor(
    private val livePriceRepository: LivePriceRepository
) {
    /**
     * Subscribe to real-time order book updates for a symbol
     *
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @return Flow of order book updates
     */
    operator fun invoke(symbol: String): Flow<OrderBookUpdate> {
        return livePriceRepository.subscribeToOrderBook(symbol)
    }
}
