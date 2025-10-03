package com.rishitgoklani.aptosdex.blockchain

import android.util.Log
import com.rishitgoklani.aptosdex.domain.model.AptosTradeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for streaming Aptos blockchain events
 * Provides real-time updates for TradeEvent emissions from the CLOB contract
 */
@Singleton
class AptosEventStream @Inject constructor(
    private val aptosClient: AptosClientWrapper
) {

    companion object {
        private const val TAG = "AptosEventStream"
        private const val POLL_INTERVAL_MS = 2000L // Poll every 2 seconds
    }

    /**
     * Stream trade events for a specific market pair
     * This provides real-time updates as trades are executed on-chain
     *
     * Implementation approach:
     * 1. For now: Polling-based approach (query events periodically)
     * 2. Future: WebSocket-based streaming via Aptos Indexer GraphQL subscription
     *
     * @param marketPair The market pair to stream events for (e.g., "APT_USDC")
     * @param startSequenceNumber The event sequence number to start from (0 for all events)
     * @return Flow of TradeEvent objects
     */
    fun streamTradeEvents(
        marketPair: String,
        startSequenceNumber: Long = 0L
    ): Flow<AptosTradeEvent> = flow {
        Log.d(TAG, "Starting event stream for market: $marketPair")
        Log.d(TAG, "Start sequence: $startSequenceNumber")

        var currentSequence = startSequenceNumber

        try {
            // TODO: Implement actual event streaming
            // Option 1: Polling approach (current implementation)
            while (true) {
                val events = fetchEventsSinceSequence(marketPair, currentSequence)

                events.forEach { event ->
                    emit(event)
                    currentSequence = event.eventSequenceNumber + 1
                    Log.d(TAG, "Emitted TradeEvent: seq=${event.eventSequenceNumber}, price=${event.price}, size=${event.size}")
                }

                delay(POLL_INTERVAL_MS)
            }

            // Option 2: GraphQL Subscription (future implementation)
            // This would use Aptos Indexer's GraphQL WebSocket for real-time updates
            // subscription {
            //   events(
            //     where: {
            //       account_address: {_eq: $CLOB_CONTRACT_ADDRESS},
            //       type: {_eq: $TRADE_EVENT_TYPE},
            //       data: {_contains: {market_pair: $marketPair}}
            //     }
            //   ) {
            //     sequence_number
            //     data
            //     transaction_version
            //   }
            // }

        } catch (e: Exception) {
            Log.e(TAG, "Error in event stream for $marketPair", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch events that occurred after a specific sequence number
     * This is used for polling-based event streaming
     */
    private suspend fun fetchEventsSinceSequence(
        marketPair: String,
        sinceSequence: Long
    ): List<AptosTradeEvent> {
        return try {
            Log.d(TAG, "Fetching events for $marketPair since sequence $sinceSequence")

            // Get type arguments for the market pair to construct OrderBook<Base, Quote>
            val typeArgs = MarketPairs.getTypeArguments(
                marketPair.split("_").firstOrNull() ?: "APT"
            )

            // Construct the OrderBook resource type with type arguments
            // Important: No spaces between comma and second type argument for Aptos API
            val orderBookType = "${AptosConfig.CLOB_CONTRACT_ADDRESS}::${AptosConfig.CLOB_MODULE_NAME}::OrderBook<${typeArgs.first},${typeArgs.second}>"

            Log.d(TAG, "OrderBook resource type: $orderBookType")

            // Fetch events from the trade_events event handle
            // API: GET /accounts/{address}/events/{event_handle_struct}/{field_name}
            val eventsResult = aptosClient.getEventsByHandle(
                address = AptosConfig.CLOB_CONTRACT_ADDRESS,
                eventHandleStruct = orderBookType,
                fieldName = "trade_events",
                start = sinceSequence,
                limit = 100 // Fetch up to 100 events at a time
            )

            eventsResult.fold(
                onSuccess = { events ->
                    Log.d(TAG, "Fetched ${events.size} events from contract")
                    parseTradeEvents(events)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to fetch events: ${error.message}")
                    emptyList()
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events", e)
            emptyList()
        }
    }

    /**
     * Parse raw event data into AptosTradeEvent objects
     */
    private fun parseTradeEvents(rawEvents: List<Map<String, Any>>): List<AptosTradeEvent> {
        return rawEvents.mapNotNull { event ->
            try {
                // Event structure from Aptos REST API:
                // {
                //   "version": "12345",
                //   "guid": {...},
                //   "sequence_number": "0",
                //   "type": "0x...::TradeEvent",
                //   "data": { "maker_order_id": "1", "taker_order_id": "2", "price": "100", "size": "10", "timestamp": "..." }
                // }

                val sequenceNumber = (event["sequence_number"] as? String)?.toLongOrNull() ?: 0L

                @Suppress("UNCHECKED_CAST")
                val data = event["data"] as? Map<String, Any> ?: return@mapNotNull null

                AptosTradeEvent(
                    eventSequenceNumber = sequenceNumber,
                    makerOrderId = (data["maker_order_id"] as? String)?.toLongOrNull() ?: 0L,
                    takerOrderId = (data["taker_order_id"] as? String)?.toLongOrNull() ?: 0L,
                    price = (data["price"] as? String)?.toDoubleOrNull() ?: 0.0,
                    size = (data["size"] as? String)?.toDoubleOrNull() ?: 0.0,
                    timestamp = (data["timestamp"] as? String)?.toLongOrNull() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing trade event", e)
                null
            }
        }
    }

    /**
     * Subscribe to trade events using GraphQL subscription
     * This is the preferred approach for production
     * TODO: Implement when integrating with Aptos Indexer
     */
    @Suppress("UNUSED_PARAMETER")
    private fun subscribeToTradeEventsViaGraphQL(
        marketPair: String
    ): Flow<AptosTradeEvent> = flow<AptosTradeEvent> {
        Log.d(TAG, "GraphQL subscription not yet implemented for $marketPair")

        // GraphQL subscription would be implemented here using a WebSocket client
        // connected to the Aptos Indexer GraphQL endpoint

        // Example GraphQL subscription query:
        //
        // subscription StreamTradeEvents($marketPair: String!) {
        //   events(
        //     where: {
        //       account_address: {_eq: "${AptosConfig.CLOB_CONTRACT_ADDRESS}"},
        //       type: {_eq: "${AptosConfig.TRADE_EVENT_TYPE}"},
        //       indexed_type: {_eq: "0x1::string::String"},
        //       data: {_cast: {String: {_contains: {market_pair: $marketPair}}}}
        //     }
        //     order_by: {sequence_number: asc}
        //   ) {
        //     sequence_number
        //     type
        //     data
        //     transaction_version
        //     transaction_block_height
        //   }
        // }

    }.flowOn(Dispatchers.IO)
}
