package com.rishitgoklani.aptosdex.data.repository

import android.util.Log
import com.rishitgoklani.aptosdex.blockchain.AptosClientWrapper
import com.rishitgoklani.aptosdex.blockchain.AptosConfig
import com.rishitgoklani.aptosdex.blockchain.AptosEventStream
import com.rishitgoklani.aptosdex.blockchain.MarketPairs
import com.rishitgoklani.aptosdex.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for Aptos order book operations
 */
interface AptosOrderBookRepository {
    suspend fun fetchOrderBookSnapshot(symbol: String): AptosOrderBookResult
    fun streamTradeEvents(symbol: String): Flow<AptosTradeEvent>
}

/**
 * Implementation of Aptos order book repository
 * Handles fetching order book snapshots and streaming trade events
 */
@Singleton
class AptosOrderBookRepositoryImpl @Inject constructor(
    private val aptosClient: AptosClientWrapper,
    private val eventStream: AptosEventStream
) : AptosOrderBookRepository {

    companion object {
        private const val TAG = "AptosOrderBookRepo"
    }

    /**
     * Fetch current order book snapshot from smart contract view function
     * Calls get_book_depth on the CLOB contract
     */
    override suspend fun fetchOrderBookSnapshot(symbol: String): AptosOrderBookResult =
        withContext(Dispatchers.IO) {
            try {
                val marketPair = MarketPairs.getMarketPair(symbol)
                val typeArgs = MarketPairs.getTypeArguments(symbol)
                Log.d(TAG, "Fetching order book snapshot for $symbol -> $marketPair")
                Log.d(TAG, "Type arguments: Base=${typeArgs.first}, Quote=${typeArgs.second}")

                // Call the view function get_book_depth<Base, Quote>(book_addr: address, levels: u64)
                // Returns: (vector<u64> bid_prices, vector<u64> bid_quantities, vector<u64> ask_prices, vector<u64> ask_quantities)
                val result = aptosClient.callViewFunction(
                    functionName = AptosConfig.VIEW_FUNCTION_GET_BOOK_DEPTH,
                    typeArguments = listOf(typeArgs.first, typeArgs.second),
                    arguments = listOf(AptosConfig.CLOB_CONTRACT_ADDRESS, 20) // book_addr, levels
                )

                result.fold(
                    onSuccess = { data ->
                        // Parse the view function response
                        // Expected format from get_book_depth: (bid_prices, bid_quantities, ask_prices, ask_quantities)
                        // data[0] = vector<u64> bid_prices
                        // data[1] = vector<u64> bid_quantities
                        // data[2] = vector<u64> ask_prices
                        // data[3] = vector<u64> ask_quantities

                        Log.d(TAG, "View function returned data: $data")

                        // Only show real data from contract, no mock fallback
                        if (data.size < 4) {
                            Log.w(TAG, "Invalid response from contract - expected 4 vectors, got ${data.size}")
                            AptosOrderBookResult.Failure(AptosOrderBookError.VIEW_FUNCTION_FAILED)
                        } else {
                            val snapshot = try {
                                parseOrderBookDataFromVectors(data, marketPair)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing order book data", e)
                                return@withContext AptosOrderBookResult.Failure(AptosOrderBookError.VIEW_FUNCTION_FAILED)
                            }

                            Log.d(TAG, "Order book snapshot created: ${snapshot.buyOrders.size} buy levels, ${snapshot.sellOrders.size} sell levels")
                            AptosOrderBookResult.Success(snapshot)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to fetch order book snapshot", error)
                        AptosOrderBookResult.Failure(
                            when {
                                error.message?.contains("network", ignoreCase = true) == true ->
                                    AptosOrderBookError.NETWORK_ERROR
                                error.message?.contains("not found", ignoreCase = true) == true ->
                                    AptosOrderBookError.CONTRACT_NOT_FOUND
                                else -> AptosOrderBookError.VIEW_FUNCTION_FAILED
                            }
                        )
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching order book snapshot for $symbol", e)
                AptosOrderBookResult.Failure(AptosOrderBookError.UNKNOWN)
            }
        }

    /**
     * Stream trade events for a specific symbol
     * Returns a Flow of TradeEvent updates
     */
    override fun streamTradeEvents(symbol: String): Flow<AptosTradeEvent> {
        val marketPair = MarketPairs.getMarketPair(symbol)
        Log.d(TAG, "Starting trade event stream for $symbol -> $marketPair")
        return eventStream.streamTradeEvents(marketPair, startSequenceNumber = 0L)
    }

    /**
     * Parse order book data from view function response with new format
     * Parses get_book_depth return: (bid_prices, bid_quantities, ask_prices, ask_quantities)
     */
    private fun parseOrderBookDataFromVectors(
        data: List<Any>,
        marketPair: String
    ): AptosOrderBookSnapshot {
        // Expected structure from updated contract:
        // data[0] = vector<u64> bid_prices
        // data[1] = vector<u64> bid_quantities
        // data[2] = vector<u64> ask_prices
        // data[3] = vector<u64> ask_quantities

        @Suppress("UNCHECKED_CAST")
        val bidPrices = (data.getOrNull(0) as? List<Any>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val bidQuantities = (data.getOrNull(1) as? List<Any>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val askPrices = (data.getOrNull(2) as? List<Any>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val askQuantities = (data.getOrNull(3) as? List<Any>) ?: emptyList()

        // Combine prices and quantities into PriceLevels
        val buyOrders = bidPrices.mapIndexedNotNull { index, priceAny ->
            val price = (priceAny as? String)?.toDoubleOrNull() ?: return@mapIndexedNotNull null
            val quantity = (bidQuantities.getOrNull(index) as? String)?.toDoubleOrNull() ?: return@mapIndexedNotNull null

            AptosPriceLevel(
                price = price,
                orders = emptyList(), // Individual orders not returned in new format
                totalSize = quantity
            )
        }

        val sellOrders = askPrices.mapIndexedNotNull { index, priceAny ->
            val price = (priceAny as? String)?.toDoubleOrNull() ?: return@mapIndexedNotNull null
            val quantity = (askQuantities.getOrNull(index) as? String)?.toDoubleOrNull() ?: return@mapIndexedNotNull null

            AptosPriceLevel(
                price = price,
                orders = emptyList(), // Individual orders not returned in new format
                totalSize = quantity
            )
        }

        return AptosOrderBookSnapshot(
            marketPair = marketPair,
            buyOrders = buyOrders,
            sellOrders = sellOrders,
            nextOrderId = 0L,
            lastUpdateSequence = 0L
        )
    }
}
