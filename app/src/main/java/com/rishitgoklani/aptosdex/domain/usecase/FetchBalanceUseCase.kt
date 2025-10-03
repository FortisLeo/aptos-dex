package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.BalanceResult
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import javax.inject.Inject

class FetchBalanceUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(address: String): BalanceResult {
        return walletRepository.fetchBalance(address)
    }
}
