package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.ChartDataResult
import com.rishitgoklani.aptosdex.domain.model.ChartTimePeriod
import com.rishitgoklani.aptosdex.domain.repository.ChartDataRepository
import javax.inject.Inject

/**
 * Use case for fetching chart candlestick data
 * Encapsulates business logic for retrieving chart data
 */
class FetchChartDataUseCase @Inject constructor(
    private val chartDataRepository: ChartDataRepository
) {
    /**
     * Executes the use case to fetch chart data
     *
     * @param symbol Token symbol (e.g., "APT", "BTC")
     * @param timePeriod The time period for the chart
     * @return ChartDataResult with success or failure
     */
    suspend operator fun invoke(
        symbol: String,
        timePeriod: ChartTimePeriod
    ): ChartDataResult {
        return chartDataRepository.fetchChartData(symbol, timePeriod)
    }
}
