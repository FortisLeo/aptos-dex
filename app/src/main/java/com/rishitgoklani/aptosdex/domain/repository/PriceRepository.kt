package com.rishitgoklani.aptosdex.domain.repository

interface PriceRepository {
    /**
     * Returns USD prices for the given symbols using Pyth Hermes latest endpoint.
     * Symbols are case-insensitive; returns 0.0 if not available.
     */
    suspend fun getUsdPrices(fromSymbol: String, toSymbol: String): Pair<Double, Double>
}


