package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.WalletConnection
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import javax.inject.Inject

class RestoreConnectionStateUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(): WalletConnection? {
        return walletRepository.restoreConnectionState()
    }
}
