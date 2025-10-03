package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.ChartDataResult
import com.rishitgoklani.aptosdex.domain.model.ChartTimePeriod

/**
 * Repository interface for fetching chart/candlestick data
 */
interface ChartDataRepository {
    /**
     * Fetches candlestick chart data for a given symbol and time period
     *
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @param timePeriod The time period for the chart
     * @return ChartDataResult with success or failure
     */
    suspend fun fetchChartData(
        symbol: String,
        timePeriod: ChartTimePeriod
    ): ChartDataResult
}
