package com.rishitgoklani.aptosdex.data.repository

import android.util.Log
import com.rishitgoklani.aptosdex.data.local.ConnectionStateStorage
import com.rishitgoklani.aptosdex.data.remote.AptosApiClient
import com.rishitgoklani.aptosdex.domain.model.BalanceError
import com.rishitgoklani.aptosdex.domain.model.BalanceResult
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.model.WalletConnection
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val aptosApiClient: AptosApiClient,
    private val connectionStateStorage: ConnectionStateStorage
) : WalletRepository {

    companion object {
        private const val TAG = "WalletRepository"
    }

    override suspend fun fetchBalance(address: String): BalanceResult {
        Log.d(TAG, "Fetching balance for address: $address")
        return try {
            val balanceInOctas = aptosApiClient.getBalance(address)
            val balanceInApt = balanceInOctas / 100_000_000.0
            val formattedBalance = String.format(java.util.Locale.US, "%.4f APT", balanceInApt)

            Log.d(TAG, "Balance fetched successfully: $formattedBalance")
            BalanceResult.Success(formattedBalance)
        } catch (e: AptosApiClient.AccountNotFoundException) {
            Log.w(TAG, "Account not found on testnet: $address")
            BalanceResult.Success("0.0000 APT")
        } catch (e: AptosApiClient.NetworkException) {
            Log.e(TAG, "Network error fetching balance", e)
            BalanceResult.Failure(BalanceError.NETWORK_ERROR)
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error fetching balance", e)
            BalanceResult.Failure(BalanceError.UNKNOWN)
        }
    }

    override suspend fun saveConnectionState(address: String, keys: ConnectionKeys) {
        Log.d(TAG, "Saving connection state for address: $address")
        connectionStateStorage.saveConnectionState(address, keys)
    }

    override suspend fun restoreConnectionState(): WalletConnection? {
        Log.d(TAG, "Restoring connection state")
        return connectionStateStorage.restoreConnectionState()
    }

    override suspend fun clearConnectionState() {
        Log.d(TAG, "Clearing connection state")
        connectionStateStorage.clearConnectionState()
    }

    override suspend fun getConnectedWalletAddress(): String? {
        val connection = restoreConnectionState()
        return if (connection?.isConnected == true) {
            connection.address
        } else {
            null
        }
    }
}

