package com.rishitgoklani.aptosdex.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.model.WalletConnection
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ConnectionStateStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoRepository: CryptoRepository
) {
    companion object {
        private const val TAG = "ConnectionStateStorage"
        private const val PREFS_NAME = "PetraWalletPrefs"
        private const val KEY_WALLET_ADDRESS = "wallet_address"
        private const val KEY_SECRET_KEY = "secret_key"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_SHARED_KEY = "shared_key"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveConnectionState(address: String, keys: ConnectionKeys) {
        prefs.edit().apply {
            putString(KEY_WALLET_ADDRESS, address)
            putString(KEY_SECRET_KEY, cryptoRepository.bytesToHex(keys.secretKey))
            putString(KEY_PUBLIC_KEY, cryptoRepository.bytesToHex(keys.publicKey))
            keys.sharedKey?.let {
                putString(KEY_SHARED_KEY, cryptoRepository.bytesToHex(it))
            }
            apply()
        }
        Log.d(TAG, "Connection state saved for address: $address")
    }

    fun restoreConnectionState(): WalletConnection? {
        val address = prefs.getString(KEY_WALLET_ADDRESS, null) ?: return null
        val sharedKeyHex = prefs.getString(KEY_SHARED_KEY, null) ?: return null

        Log.d(TAG, "Restored wallet address: $address")

        return WalletConnection(
            address = address,
            isConnected = true
        )
    }

    fun restoreConnectionKeys(): ConnectionKeys? {
        val secretKeyHex = prefs.getString(KEY_SECRET_KEY, null) ?: return null
        val publicKeyHex = prefs.getString(KEY_PUBLIC_KEY, null) ?: return null
        val sharedKeyHex = prefs.getString(KEY_SHARED_KEY, null)

        return ConnectionKeys(
            secretKey = cryptoRepository.hexToBytes(secretKeyHex),
            publicKey = cryptoRepository.hexToBytes(publicKeyHex),
            sharedKey = sharedKeyHex?.let { cryptoRepository.hexToBytes(it) }
        )
    }

    fun clearConnectionState() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Connection state cleared")
    }
}
