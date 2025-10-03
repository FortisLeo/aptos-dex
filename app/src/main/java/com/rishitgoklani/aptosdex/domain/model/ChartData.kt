package com.rishitgoklani.aptosdex.domain.model

/**
 * Domain model for chart candlestick data
 */
data class ChartData(
    val candles: List<Candle>
)

/**
 * Single candlestick data point
 */
data class Candle(
    val timestamp: Long,  // Unix timestamp in seconds
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

/**
 * Time period for chart data
 */
enum class ChartTimePeriod(val interval: String, val limit: Int) {
    HOURS_24("1h", 24),      // 24 data points for 24h
    DAYS_7("4h", 42),        // 42 data points for 7 days
    MONTH_1("1d", 30),       // 30 data points for 1 month
    YTD("1d", 365)           // Up to 365 data points for year to date
}

/**
 * Result wrapper for chart data operations
 */
sealed class ChartDataResult {
    data class Success(val data: ChartData) : ChartDataResult()
    data class Failure(val error: ChartDataError) : ChartDataResult()
}

/**
 * Chart data error types
 */
enum class ChartDataError {
    NETWORK_ERROR,
    INVALID_SYMBOL,
    RATE_LIMIT_EXCEEDED,
    UNKNOWN
}
