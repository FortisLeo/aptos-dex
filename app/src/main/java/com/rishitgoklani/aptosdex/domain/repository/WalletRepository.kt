package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.BalanceResult
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.model.WalletConnection

interface WalletRepository {
    suspend fun fetchBalance(address: String): BalanceResult
    suspend fun saveConnectionState(
        address: String,
        keys: ConnectionKeys
    )
    suspend fun restoreConnectionState(): WalletConnection?
    suspend fun clearConnectionState()
    suspend fun getConnectedWalletAddress(): String?
}