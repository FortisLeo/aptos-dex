package com.rishitgoklani.aptosdex.domain.usecase

import android.util.Base64
import com.google.gson.Gson
import com.rishitgoklani.aptosdex.domain.model.ConnectionError
import com.rishitgoklani.aptosdex.domain.model.ConnectionResult
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import javax.inject.Inject

class HandleConnectionApprovalUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val gson: Gson
) {
    operator fun invoke(
        data: String,
        secretKey: ByteArray
    ): ConnectionResult {
        return try {
            val decodedData = String(Base64.decode(data, Base64.DEFAULT))
            val responseData = gson.fromJson(decodedData, Map::class.java)

            val petraPublicKey = responseData["petraPublicEncryptedKey"] as? String
            val address = responseData["address"] as? String

            if (petraPublicKey == null || address == null) {
                return ConnectionResult.Failure(ConnectionError.MISSING_DATA)
            }

            val sharedKeyResult = cryptoRepository.computeSharedKey(petraPublicKey, secretKey)

            sharedKeyResult.fold(
                onSuccess = { sharedKey ->
                    ConnectionResult.Success(address, sharedKey)
                },
                onFailure = {
                    ConnectionResult.Failure(ConnectionError.CRYPTO_ERROR)
                }
            )
        } catch (e: Exception) {
            ConnectionResult.Failure(ConnectionError.UNKNOWN)
        }
    }
}
