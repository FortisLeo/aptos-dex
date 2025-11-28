package com.rishitgoklani.aptosdex.domain.usecase

import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import javax.inject.Inject

class ConnectWalletUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) {
    operator fun invoke(): Pair<ConnectionKeys, String> {
        val keys = cryptoRepository.generateKeyPair()
        val publicKeyHex = cryptoRepository.bytesToHex(keys.publicKey)
        return Pair(keys, publicKeyHex)
    }
}

