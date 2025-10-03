package com.rishitgoklani.aptosdex.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class AptosApiClient @Inject constructor(
    private val client: OkHttpClient,
    private val baseUrl: String
) {
    companion object {
        private const val TAG = "AptosApiClient"
        private const val ASSET_TYPE = "0x1::aptos_coin::AptosCoin"
    }

    class AccountNotFoundException(message: String) : Exception(message)
    class NetworkException(message: String, cause: Throwable?) : Exception(message, cause)

    suspend fun getBalance(address: String): Long = withContext(Dispatchers.IO) {
        val url = "$baseUrl/accounts/$address/balance/$ASSET_TYPE"
        Log.d(TAG, "Fetching balance from URL: $url")

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Balance API response code: ${response.code}")
            Log.d(TAG, "Balance API response body: $responseBody")

            when {
                response.isSuccessful && responseBody != null -> {
                    val balanceInOctas = responseBody.trim().toLongOrNull()
                        ?: throw NetworkException("Invalid balance format: $responseBody", null)

                    Log.d(TAG, "Parsed balance (octas): $balanceInOctas")
                    balanceInOctas
                }
                response.code == 404 -> {
                    throw AccountNotFoundException("Account not found or not initialized: $address")
                }
                else -> {
                    throw NetworkException(
                        "Failed to fetch balance. Code: ${response.code}, body: $responseBody",
                        null
                    )
                }
            }
        } catch (e: AccountNotFoundException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching balance", e)
            throw NetworkException("Network error: ${e.message}", e)
        }
    }
}
