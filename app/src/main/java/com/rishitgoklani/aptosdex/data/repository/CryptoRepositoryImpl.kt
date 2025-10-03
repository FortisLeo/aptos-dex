package com.rishitgoklani.aptosdex.data.repository

import com.rishitgoklani.aptosdex.blockchain.crypto.CryptoKeyManager
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val cryptoKeyManager: CryptoKeyManager
) : CryptoRepository {

    override fun generateKeyPair(): ConnectionKeys {
        return cryptoKeyManager.generateKeyPair()
    }

    override fun computeSharedKey(petraPublicKey: String, secretKey: ByteArray): Result<ByteArray> {
        return cryptoKeyManager.computeSharedKey(petraPublicKey, secretKey)
    }

    override fun bytesToHex(bytes: ByteArray): String {
        return cryptoKeyManager.bytesToHex(bytes)
    }

    override fun hexToBytes(hex: String): ByteArray {
        return cryptoKeyManager.hexToBytes(hex)
    }
}
