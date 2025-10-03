package com.rishitgoklani.aptosdex.data.remote.binance

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Binance WebSocket Service
 * Manages WebSocket connections to Binance streams
 * Base URL: wss://stream.binance.com:9443
 */
@Singleton
class BinanceWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "BinanceWebSocket"
        private const val WS_BASE_URL = "wss://stream.binance.com:9443/ws"
    }

    /**
     * Subscribe to Individual Symbol Ticker Stream
     * Stream: <symbol>@ticker
     * Updates: ~1 second
     *
     * @param symbol Trading pair in lowercase (e.g., "aptusdt", "btcusdt")
     * @return Flow of ticker updates
     */
    fun subscribeToTicker(symbol: String): Flow<BinanceTickerStreamDto> = callbackFlow {
        val symbolLower = symbol.lowercase()
        val url = "$WS_BASE_URL/${symbolLower}@ticker"

        Log.d(TAG, "Connecting to ticker stream: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Ticker WebSocket opened for $symbol")
                Log.d(TAG, "Response: ${response.code} ${response.message}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ticker = gson.fromJson(text, BinanceTickerStreamDto::class.java)
                    Log.d(TAG, "Ticker Update - ${ticker.symbol}: Price=${ticker.lastPrice}, Change=${ticker.priceChangePercent}%")
                    trySend(ticker)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ticker message", e)
                    Log.e(TAG, "Raw message: ${text.take(200)}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Ticker WebSocket failure for $symbol", t)
                Log.e(TAG, "Response code: ${response?.code}, message: ${response?.message}")
                close(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Ticker WebSocket closing for $symbol: $code - $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Ticker WebSocket closed for $symbol: $code - $reason")
                close()
            }
        }

        val webSocket = okHttpClient.newWebSocket(request, listener)

        awaitClose {
            Log.d(TAG, "Closing ticker WebSocket for $symbol")
            webSocket.close(1000, "Flow closed")
        }
    }

    /**
     * Subscribe to Diff. Depth Stream
     * Stream: <symbol>@depth
     * Updates: Real-time order book deltas
     *
     * @param symbol Trading pair in lowercase (e.g., "aptusdt", "btcusdt")
     * @return Flow of depth updates
     */
    fun subscribeToDepth(symbol: String): Flow<BinanceDepthStreamDto> = callbackFlow {
        val symbolLower = symbol.lowercase()
        val url = "$WS_BASE_URL/${symbolLower}@depth"

        Log.d(TAG, "Connecting to depth stream: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Depth WebSocket opened for $symbol")
                Log.d(TAG, "Response: ${response.code} ${response.message}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val depth = gson.fromJson(text, BinanceDepthStreamDto::class.java)
                    Log.d(TAG, "OrderBook Update - ${depth.symbol}: ${depth.bids.size} bids, ${depth.asks.size} asks")
                    if (depth.bids.isNotEmpty()) {
                        Log.d(TAG, "Best Bid: ${depth.bids.first()[0]} @ ${depth.bids.first()[1]}")
                    }
                    if (depth.asks.isNotEmpty()) {
                        Log.d(TAG, "Best Ask: ${depth.asks.first()[0]} @ ${depth.asks.first()[1]}")
                    }
                    trySend(depth)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing depth message", e)
                    Log.e(TAG, "Raw message: ${text.take(200)}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Depth WebSocket failure for $symbol", t)
                Log.e(TAG, "Response code: ${response?.code}, message: ${response?.message}")
                close(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Depth WebSocket closing for $symbol: $code - $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Depth WebSocket closed for $symbol: $code - $reason")
                close()
            }
        }

        val webSocket = okHttpClient.newWebSocket(request, listener)

        awaitClose {
            Log.d(TAG, "Closing depth WebSocket for $symbol")
            webSocket.close(1000, "Flow closed")
        }
    }
}
