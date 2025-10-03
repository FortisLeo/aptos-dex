package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.data.repository.AptosOrderBookRepository
import com.rishitgoklani.aptosdex.domain.model.AptosTradeEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for streaming trade events from Aptos blockchain
 * Subscribes to TradeEvent emissions from the CLOB smart contract
 */
class StreamAptosTradeEventsUseCase @Inject constructor(
    private val repository: AptosOrderBookRepository
) {
    /**
     * Execute the use case
     * @param symbol Token symbol (e.g., "APT")
     * @return Flow of AptosTradeEvent updates
     */
    operator fun invoke(symbol: String): Flow<AptosTradeEvent> {
        return repository.streamTradeEvents(symbol)
    }
}
