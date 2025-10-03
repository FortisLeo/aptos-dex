package com.rishitgoklani.aptosdex.data.repository

import android.util.Log
import com.rishitgoklani.aptosdex.data.remote.binance.BinanceApiService
import com.rishitgoklani.aptosdex.domain.model.TokenPrice
import com.rishitgoklani.aptosdex.domain.model.TokenPriceError
import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult
import com.rishitgoklani.aptosdex.domain.repository.TokenPriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of TokenPriceRepository using Binance API
 * Maps token symbols to Binance trading pairs (e.g., APT -> APTUSDT)
 */
class TokenPriceRepositoryImpl @Inject constructor(
    private val binanceApi: BinanceApiService
) : TokenPriceRepository {

    companion object {
        private const val TAG = "TokenPriceRepository"

        // Mapping of token symbols to Binance trading pairs
        private val SYMBOL_TO_PAIR = mapOf(
            "APT" to "APTUSDT",
            "USDT" to "USDTUSDT",
            "USDC" to "USDCUSDT",
            "BTC" to "BTCUSDT",
            "WBTC" to "WBTCUSDT",
            "ETH" to "ETHUSDT",
            "CAKE" to "CAKEUSDT"
        )
    }

    override suspend fun fetch24hrPrices(symbols: List<String>): TokenPriceResult =
        withContext(Dispatchers.IO) {
            try {
                // Map token symbols to Binance pairs
                val binancePairs = symbols.mapNotNull { SYMBOL_TO_PAIR[it] }

                if (binancePairs.isEmpty()) {
                    Log.w(TAG, "No valid Binance pairs found for symbols: $symbols")
                    return@withContext TokenPriceResult.Failure(TokenPriceError.INVALID_SYMBOL)
                }

                // Create JSON array format for Binance API: ["APTUSDT","BTCUSDT"]
                val symbolsParam = binancePairs.joinToString(
                    prefix = "[\"",
                    separator = "\",\"",
                    postfix = "\"]"
                )

                Log.d(TAG, "Fetching prices for symbols: $symbolsParam")

                // Fetch from Binance API
                val response = binanceApi.get24hrTickers(symbolsParam)

                // Map DTOs to domain models
                val tokenPrices = response.map { dto ->
                    TokenPrice(
                        symbol = getSymbolFromPair(dto.symbol),
                        currentPrice = dto.lastPrice.toDoubleOrNull() ?: 0.0,
                        priceChange24h = dto.priceChange.toDoubleOrNull() ?: 0.0,
                        priceChangePercent24h = dto.priceChangePercent.toDoubleOrNull() ?: 0.0,
                        volume24h = dto.quoteVolume.toDoubleOrNull() ?: 0.0,
                        high24h = dto.highPrice.toDoubleOrNull() ?: 0.0,
                        low24h = dto.lowPrice.toDoubleOrNull() ?: 0.0
                    )
                }

                Log.d(TAG, "Successfully fetched ${tokenPrices.size} token prices")
                TokenPriceResult.Success(tokenPrices)

            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    429 -> {
                        Log.e(TAG, "Rate limit exceeded", e)
                        TokenPriceResult.Failure(TokenPriceError.RATE_LIMIT_EXCEEDED)
                    }
                    else -> {
                        Log.e(TAG, "HTTP error fetching prices: ${e.code()}", e)
                        TokenPriceResult.Failure(TokenPriceError.NETWORK_ERROR)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching token prices", e)
                TokenPriceResult.Failure(TokenPriceError.UNKNOWN)
            }
        }

    /**
     * Extracts token symbol from Binance trading pair
     * e.g., "APTUSDT" -> "APT"
     */
    private fun getSymbolFromPair(pair: String): String {
        return SYMBOL_TO_PAIR.entries
            .firstOrNull { it.value == pair }
            ?.key
            ?: pair.removeSuffix("USDT").removeSuffix("USDC")
    }
}
