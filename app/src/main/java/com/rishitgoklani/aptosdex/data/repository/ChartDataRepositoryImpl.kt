package com.rishitgoklani.aptosdex.data.repository

import android.util.Log
import com.rishitgoklani.aptosdex.data.remote.binance.BinanceApiService
import com.rishitgoklani.aptosdex.data.remote.binance.*
import com.rishitgoklani.aptosdex.domain.model.*
import com.rishitgoklani.aptosdex.domain.repository.ChartDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of ChartDataRepository using Binance Klines API
 */
class ChartDataRepositoryImpl @Inject constructor(
    private val binanceApi: BinanceApiService
) : ChartDataRepository {

    companion object {
        private const val TAG = "ChartDataRepository"

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

    override suspend fun fetchChartData(
        symbol: String,
        timePeriod: ChartTimePeriod
    ): ChartDataResult = withContext(Dispatchers.IO) {
        try {
            // Map token symbol to Binance pair
            val binancePair = SYMBOL_TO_PAIR[symbol.uppercase()]
            if (binancePair == null) {
                Log.w(TAG, "No Binance pair found for symbol: $symbol")
                return@withContext ChartDataResult.Failure(ChartDataError.INVALID_SYMBOL)
            }

            Log.d(TAG, "Fetching klines for $binancePair with interval ${timePeriod.interval}, limit ${timePeriod.limit}")

            // Fetch klines from Binance API
            val klines = binanceApi.getKlines(
                symbol = binancePair,
                interval = timePeriod.interval,
                limit = timePeriod.limit
            )

            // Map DTOs to domain models
            val candles = klines.map { kline ->
                Candle(
                    timestamp = kline.getOpenTime() / 1000,  // Convert to seconds
                    open = kline.getOpenPrice().toDoubleOrNull() ?: 0.0,
                    high = kline.getHighPrice().toDoubleOrNull() ?: 0.0,
                    low = kline.getLowPrice().toDoubleOrNull() ?: 0.0,
                    close = kline.getClosePrice().toDoubleOrNull() ?: 0.0,
                    volume = kline.getVolume().toDoubleOrNull() ?: 0.0
                )
            }

            Log.d(TAG, "Successfully fetched ${candles.size} candles for $symbol")
            ChartDataResult.Success(ChartData(candles))

        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                429 -> {
                    Log.e(TAG, "Rate limit exceeded", e)
                    ChartDataResult.Failure(ChartDataError.RATE_LIMIT_EXCEEDED)
                }
                else -> {
                    Log.e(TAG, "HTTP error fetching chart data: ${e.code()}", e)
                    ChartDataResult.Failure(ChartDataError.NETWORK_ERROR)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chart data for $symbol", e)
            ChartDataResult.Failure(ChartDataError.UNKNOWN)
        }
    }
}
