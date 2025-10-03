package com.rishitgoklani.aptosdex.blockchain.crypto

import android.util.Log
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import javax.inject.Inject

class CryptoKeyManager @Inject constructor() : CryptoRepository {

    companion object {
        private const val TAG = "CryptoKeyManager"
    }

    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    override fun generateKeyPair(): ConnectionKeys {
        val publicKey = ByteArray(Box.PUBLICKEYBYTES)
        val secretKey = ByteArray(Box.SECRETKEYBYTES)

        lazySodium.cryptoBoxKeypair(publicKey, secretKey)

        Log.d(TAG, "Generated key pair - Public key: ${bytesToHex(publicKey)}")

        return ConnectionKeys(
            secretKey = secretKey,
            publicKey = publicKey
        )
    }

    override fun computeSharedKey(
        petraPublicKey: String,
        secretKey: ByteArray
    ): Result<ByteArray> {
        return try {
            // Remove 0x prefix if present
            val cleanKey = if (petraPublicKey.startsWith("0x")) {
                petraPublicKey.substring(2)
            } else {
                petraPublicKey
            }

            val petraPubKeyBytes = hexToBytes(cleanKey)
            val sharedKey = ByteArray(Box.BEFORENMBYTES)

            val success = lazySodium.cryptoBoxBeforeNm(sharedKey, petraPubKeyBytes, secretKey)

            if (success) {
                Log.d(TAG, "Shared key computed successfully")
                Result.success(sharedKey)
            } else {
                Log.e(TAG, "Failed to compute shared key")
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
}
