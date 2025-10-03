package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.LivePriceUpdate
import com.rishitgoklani.aptosdex.domain.model.OrderBookResult
import com.rishitgoklani.aptosdex.domain.model.OrderBookUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for live price and order book data via WebSocket
 */
interface LivePriceRepository {
    /**
     * Subscribe to live price updates for a symbol
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @return Flow of live price updates
     */
    fun subscribeToPriceUpdates(symbol: String): Flow<LivePriceUpdate>

    /**
     * Fetch order book snapshot (initial state)
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @param limit Number of price levels (default 20)
     * @return OrderBookResult with snapshot or error
     */
    suspend fun fetchOrderBookSnapshot(symbol: String, limit: Int = 20): OrderBookResult

    /**
     * Subscribe to order book updates for a symbol
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @return Flow of order book updates
     */
    fun subscribeToOrderBook(symbol: String): Flow<OrderBookUpdate>
}
