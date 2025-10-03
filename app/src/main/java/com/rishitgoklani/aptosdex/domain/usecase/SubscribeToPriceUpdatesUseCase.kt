package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.LivePriceUpdate
import com.rishitgoklani.aptosdex.domain.repository.LivePriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for subscribing to live price updates via WebSocket
 */
class SubscribeToPriceUpdatesUseCase @Inject constructor(
    private val livePriceRepository: LivePriceRepository
) {
    /**
     * Subscribe to real-time price updates for a symbol
     *
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @return Flow of live price updates
     */
    operator fun invoke(symbol: String): Flow<LivePriceUpdate> {
        return livePriceRepository.subscribeToPriceUpdates(symbol)
    }
}
