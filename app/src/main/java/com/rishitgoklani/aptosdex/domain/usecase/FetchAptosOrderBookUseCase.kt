package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.data.repository.AptosOrderBookRepository
import com.rishitgoklani.aptosdex.domain.model.AptosOrderBookResult
import javax.inject.Inject

/**
 * Use case for fetching order book snapshot from Aptos smart contract
 * Calls the view function to get current market depth
 */
class FetchAptosOrderBookUseCase @Inject constructor(
    private val repository: AptosOrderBookRepository
) {
    /**
     * Execute the use case
     * @param symbol Token symbol (e.g., "APT")
     * @return AptosOrderBookResult containing snapshot or error
     */
    suspend operator fun invoke(symbol: String): AptosOrderBookResult {
        return repository.fetchOrderBookSnapshot(symbol)
    }
}
