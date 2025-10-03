package com.rishitgoklani.aptosdex.presentation.tokendetail

/**
 * UI state for Token Detail screen
 * Represents all data needed to render the token details view
 */
data class TokenDetailUiState(
    val currentPrice: String = "$12.345",
    val priceChange24h: String = "0.42",
    val change24h: Double = 3.21,
    val change7d: Double = 5.67,
    val marketCap: String = "$1.2B",
    val mint: String = "0x1a2b...9a0b",
    val volume24h: String = "$523M",
    val fdv: String = "$2.5B",
    val circulatingSupply: String = "500M APT",
    val totalSupply: String = "1B APT",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Tab types for token detail view
 */
enum class TabType {
    CHART,
    ORDER_BOOK
}

/**
 * Time period options for chart view
 */
enum class TimePeriod(val label: String) {
    HOURS_24("24h"),
    DAYS_7("7d"),
    MONTH_1("1M"),
    YTD("YTD")
}
