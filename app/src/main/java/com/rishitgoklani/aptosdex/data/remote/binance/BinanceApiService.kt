package com.rishitgoklani.aptosdex.data.remote.binance

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Binance Spot API Service
 * Base URL: https://api.binance.com
 *
 * Documentation: https://developers.binance.com/docs/binance-spot-api-docs/rest-api
 */
interface BinanceApiService {

    /**
     * 24hr Ticker Price Change Statistics
     * GET /api/v3/ticker/24hr
     *
     * @param symbol Trading pair symbol (e.g., "APTUSDT", "BTCUSDT")
     * @return 24hr ticker statistics including last price, price change, and volume
     */
    @GET("/api/v3/ticker/24hr")
    suspend fun get24hrTicker(
        @Query("symbol") symbol: String
    ): BinanceTicker24hrDto

    /**
     * 24hr Ticker Price Change Statistics for Multiple Symbols
     * GET /api/v3/ticker/24hr
     *
     * @param symbols Trading pair symbols as JSON array (e.g., ["APTUSDT","BTCUSDT"])
     * @return List of 24hr ticker statistics
     */
    @GET("/api/v3/ticker/24hr")
    suspend fun get24hrTickers(
        @Query("symbols") symbols: String
    ): List<BinanceTicker24hrDto>

    /**
     * Kline/Candlestick Data
     * GET /api/v3/klines
     *
     * @param symbol Trading pair symbol (e.g., "APTUSDT", "BTCUSDT")
     * @param interval Kline interval: 1m, 3m, 5m, 15m, 30m, 1h, 2h, 4h, 6h, 8h, 12h, 1d, 3d, 1w, 1M
     * @param limit Number of data points (default 500, max 1000)
     * @return List of kline data arrays
     */
    @GET("/api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<BinanceKlineDto>

    /**
     * Order Book Snapshot
     * GET /api/v3/depth
     *
     * @param symbol Trading pair symbol (e.g., "APTUSDT", "BTCUSDT")
     * @param limit Default 100; max 5000. Valid limits:[5, 10, 20, 50, 100, 500, 1000, 5000]
     * @return Order book snapshot with bids and asks
     */
    @GET("/api/v3/depth")
    suspend fun getOrderBookSnapshot(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 20
    ): BinanceOrderBookSnapshotDto
}
