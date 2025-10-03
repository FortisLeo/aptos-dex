package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.model.ConnectionResult

interface CryptoRepository {
    fun generateKeyPair(): ConnectionKeys
    fun computeSharedKey(
        petraPublicKey: String,
        secretKey: ByteArray
    ): Result<ByteArray>
    fun bytesToHex(bytes: ByteArray): String
    fun hexToBytes(hex: String): ByteArray
    fun encryptPayload(
        payload: String,
        nonce: ByteArray,
        sharedKey: ByteArray
    ): Result<ByteArray>
    fun generateNonce(): ByteArray
}