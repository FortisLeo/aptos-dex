package com.rishitgoklani.aptosdex.data.repository

import android.util.Log
import com.rishitgoklani.aptosdex.data.remote.binance.BinanceApiService
import com.rishitgoklani.aptosdex.data.remote.binance.BinanceWebSocketService
import com.rishitgoklani.aptosdex.domain.model.*
import com.rishitgoklani.aptosdex.domain.repository.LivePriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of LivePriceRepository using Binance API and WebSocket
 */
class LivePriceRepositoryImpl @Inject constructor(
    private val binanceApi: BinanceApiService,
    private val webSocketService: BinanceWebSocketService
) : LivePriceRepository {

    companion object {
        private const val TAG = "LivePriceRepository"

        // Mapping of token symbols to Binance trading pairs
        private val SYMBOL_TO_PAIR = mapOf(
            "APT" to "APTUSDT",
            "USDC" to "USDCUSDT",
            "BTC" to "BTCUSDT",
            "WBTC" to "BTCUSDT",  // Use BTC price for WBTC
            "ETH" to "ETHUSDT",
            "CAKE" to "CAKEUSDT"
        )
    }

    override fun subscribeToPriceUpdates(symbol: String): Flow<LivePriceUpdate> {
        val binancePair = SYMBOL_TO_PAIR[symbol.uppercase()] ?: "${symbol.uppercase()}USDT"

        Log.d(TAG, "Subscribing to price updates for $symbol -> $binancePair")

        return webSocketService.subscribeToTicker(binancePair).map { dto ->
            val priceUpdate = LivePriceUpdate(
                symbol = symbol.uppercase(),
                lastPrice = dto.lastPrice.toDoubleOrNull() ?: 0.0,
                priceChange = dto.priceChange.toDoubleOrNull() ?: 0.0,
                priceChangePercent = dto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                highPrice = dto.highPrice.toDoubleOrNull() ?: 0.0,
                lowPrice = dto.lowPrice.toDoubleOrNull() ?: 0.0,
                volume = dto.totalTradedQuoteVolume.toDoubleOrNull() ?: 0.0,
                timestamp = dto.eventTime
            )
            Log.d(TAG, "Mapped Price: ${priceUpdate.symbol} = $${priceUpdate.lastPrice} (${priceUpdate.priceChangePercent}%)")
            priceUpdate
        }
    }

    override suspend fun fetchOrderBookSnapshot(symbol: String, limit: Int): OrderBookResult =
        withContext(Dispatchers.IO) {
            try {
                val binancePair = SYMBOL_TO_PAIR[symbol.uppercase()] ?: "${symbol.uppercase()}USDT"

                Log.d(TAG, "Fetching order book snapshot for $symbol -> $binancePair (limit=$limit)")

                val snapshot = binanceApi.getOrderBookSnapshot(binancePair, limit)

                val orderBookSnapshot = OrderBookSnapshot(
                    symbol = symbol.uppercase(),
                    lastUpdateId = snapshot.lastUpdateId,
                    bids = snapshot.bids.map { level ->
                        OrderBookLevel(
                            price = level[0].toDoubleOrNull() ?: 0.0,
                            quantity = level[1].toDoubleOrNull() ?: 0.0
                        )
                    },
                    asks = snapshot.asks.map { level ->
                        OrderBookLevel(
                            price = level[0].toDoubleOrNull() ?: 0.0,
                            quantity = level[1].toDoubleOrNull() ?: 0.0
                        )
                    }
                )

                Log.d(TAG, "Order book snapshot fetched: lastUpdateId=${snapshot.lastUpdateId}, bids=${orderBookSnapshot.bids.size}, asks=${orderBookSnapshot.asks.size}")
                OrderBookResult.Success(orderBookSnapshot)

            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    429 -> {
                        Log.e(TAG, "Rate limit exceeded", e)
                        OrderBookResult.Failure(OrderBookError.RATE_LIMIT_EXCEEDED)
                    }
                    else -> {
                        Log.e(TAG, "HTTP error fetching order book: ${e.code()}", e)
                        OrderBookResult.Failure(OrderBookError.NETWORK_ERROR)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching order book snapshot", e)
                OrderBookResult.Failure(OrderBookError.UNKNOWN)
            }
        }

    override fun subscribeToOrderBook(symbol: String): Flow<OrderBookUpdate> {
        val binancePair = SYMBOL_TO_PAIR[symbol.uppercase()] ?: "${symbol.uppercase()}USDT"

        Log.d(TAG, "Subscribing to order book for $symbol -> $binancePair")

        return webSocketService.subscribeToDepth(binancePair).map { dto ->
            val orderBookUpdate = OrderBookUpdate(
                symbol = symbol.uppercase(),
                firstUpdateId = dto.firstUpdateId,
                finalUpdateId = dto.finalUpdateId,
                bids = dto.bids.map { level ->
                    OrderBookLevel(
                        price = level[0].toDoubleOrNull() ?: 0.0,
                        quantity = level[1].toDoubleOrNull() ?: 0.0
                    )
                },
                asks = dto.asks.map { level ->
                    OrderBookLevel(
                        price = level[0].toDoubleOrNull() ?: 0.0,
                        quantity = level[1].toDoubleOrNull() ?: 0.0
                    )
                },
                timestamp = dto.eventTime
            )
            Log.d(TAG, "OrderBook Update Mapped: ${orderBookUpdate.symbol} - firstUpdateId=${orderBookUpdate.firstUpdateId}, finalUpdateId=${orderBookUpdate.finalUpdateId}")
            orderBookUpdate
        }
    }
}
