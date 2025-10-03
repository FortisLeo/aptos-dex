package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.OrderBookResult
import com.rishitgoklani.aptosdex.domain.repository.LivePriceRepository
import javax.inject.Inject

/**
 * Use case for fetching initial order book snapshot
 */
class FetchOrderBookSnapshotUseCase @Inject constructor(
    private val livePriceRepository: LivePriceRepository
) {
    /**
     * Fetches order book snapshot for a symbol
     *
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @param limit Number of price levels (default 20)
     * @return OrderBookResult with snapshot or error
     */
    suspend operator fun invoke(symbol: String, limit: Int = 20): OrderBookResult {
        return livePriceRepository.fetchOrderBookSnapshot(symbol, limit)
    }
}
