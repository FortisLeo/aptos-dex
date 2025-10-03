package com.rishitgoklani.aptosdex.blockchain.crypto

import android.util.Log
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.interfaces.Random
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import javax.inject.Inject
 

class CryptoKeyManager @Inject constructor() : CryptoRepository {

    companion object {
        private const val TAG = "CryptoKeyManager"
    }

    private val lazySodium by lazy {
        try {
            LazySodiumAndroid(SodiumAndroid())
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "LazySodium native library not available", e)
            null
        }
    }
    

    override fun generateKeyPair(): ConnectionKeys {
        val sodium = lazySodium ?: throw IllegalStateException("libsodium not available")
        return run {
            val publicKey = ByteArray(Box.PUBLICKEYBYTES)
            val secretKey = ByteArray(Box.SECRETKEYBYTES)
            sodium.cryptoBoxKeypair(publicKey, secretKey)
            Log.d(TAG, "Generated key pair with LazySodium - Public key: ${bytesToHex(publicKey)}")
            ConnectionKeys(secretKey = secretKey, publicKey = publicKey)
        }
    }

    override fun computeSharedKey(
        petraPublicKey: String,
        secretKey: ByteArray
    ): Result<ByteArray> {
        return try {
            val sodium = lazySodium ?: throw IllegalStateException("libsodium not available")
                // Remove 0x prefix if present
                val cleanKey = if (petraPublicKey.startsWith("0x")) {
                    petraPublicKey.substring(2)
                } else {
                    petraPublicKey
                }

                val petraPubKeyBytes = hexToBytes(cleanKey)
                val sharedKey = ByteArray(Box.BEFORENMBYTES)

                val success = sodium.cryptoBoxBeforeNm(sharedKey, petraPubKeyBytes, secretKey)

                if (success) {
                    Log.d(TAG, "Shared key computed successfully with LazySodium")
                    Result.success(sharedKey)
                } else {
                    Log.e(TAG, "Failed to compute shared key with LazySodium")
                    Result.failure(Exception("Failed to compute shared key"))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error computing shared key", e)
            Result.failure(e)
        }
    }

    override fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    override fun encryptPayload(
        payload: String,
        nonce: ByteArray,
        sharedKey: ByteArray
    ): Result<ByteArray> {
        return try {
            val sodium = lazySodium ?: throw IllegalStateException("libsodium not available")
                val payloadBytes = payload.toByteArray()
                val encryptedBytes = ByteArray(payloadBytes.size + Box.MACBYTES)
                
                val success = sodium.cryptoBoxEasyAfterNm(
                    encryptedBytes,
                    payloadBytes,
                    payloadBytes.size.toLong(),
                    nonce,
                    sharedKey
                )
                
                if (success) {
                    Log.d(TAG, "Payload encrypted successfully with LazySodium")
                    Result.success(encryptedBytes)
                } else {
                    Log.e(TAG, "Failed to encrypt payload with LazySodium")
                    Result.failure(Exception("Failed to encrypt payload"))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting payload", e)
            Result.failure(e)
        }
    }

    override fun generateNonce(): ByteArray {
        val sodium = lazySodium ?: throw IllegalStateException("libsodium not available")
        return sodium.randomBytesBuf(Box.NONCEBYTES)
    }
}
