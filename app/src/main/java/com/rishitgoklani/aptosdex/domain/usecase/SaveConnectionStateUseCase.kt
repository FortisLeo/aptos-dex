package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import javax.inject.Inject

class SaveConnectionStateUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(address: String, keys: ConnectionKeys) {
        walletRepository.saveConnectionState(address, keys)
    }
}
