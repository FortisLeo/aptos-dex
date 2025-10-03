package com.rishitgoklani.aptosdex.data.remote.binance

/**
 * Binance Kline/Candlestick DTO
 *
 * Response format: Array of arrays
 * [
 *   [
 *     1499040000000,      // 0: Open time
 *     "0.01634000",       // 1: Open price
 *     "0.80000000",       // 2: High price
 *     "0.01575800",       // 3: Low price
 *     "0.01577100",       // 4: Close price
 *     "148976.11427815",  // 5: Volume
 *     1499644799999,      // 6: Close time
 *     "2434.19055334",    // 7: Quote asset volume
 *     308,                // 8: Number of trades
 *     "1756.87402397",    // 9: Taker buy base asset volume
 *     "28.46694368",      // 10: Taker buy quote asset volume
 *     "0"                 // 11: Ignore
 *   ]
 * ]
 */
typealias BinanceKlineDto = List<Any>

/**
 * Extension functions to safely extract kline data from raw array
 */
fun BinanceKlineDto.getOpenTime(): Long = (this[0] as? Number)?.toLong() ?: 0L
fun BinanceKlineDto.getOpenPrice(): String = this[1] as? String ?: "0"
fun BinanceKlineDto.getHighPrice(): String = this[2] as? String ?: "0"
fun BinanceKlineDto.getLowPrice(): String = this[3] as? String ?: "0"
fun BinanceKlineDto.getClosePrice(): String = this[4] as? String ?: "0"
fun BinanceKlineDto.getVolume(): String = this[5] as? String ?: "0"
fun BinanceKlineDto.getCloseTime(): Long = (this[6] as? Number)?.toLong() ?: 0L
