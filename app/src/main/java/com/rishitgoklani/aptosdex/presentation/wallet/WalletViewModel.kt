package com.rishitgoklani.aptosdex.presentation.wallet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.blockchain.petra.PetraWalletConnector
import com.rishitgoklani.aptosdex.domain.model.BalanceResult
import com.rishitgoklani.aptosdex.domain.model.ConnectionKeys
import com.rishitgoklani.aptosdex.domain.model.ConnectionResult
import com.rishitgoklani.aptosdex.domain.usecase.ConnectWalletUseCase
import com.rishitgoklani.aptosdex.domain.usecase.FetchBalanceUseCase
import com.rishitgoklani.aptosdex.domain.usecase.HandleConnectionApprovalUseCase
import com.rishitgoklani.aptosdex.domain.usecase.RestoreConnectionStateUseCase
import com.rishitgoklani.aptosdex.domain.usecase.SaveConnectionStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val connectWalletUseCase: ConnectWalletUseCase,
    private val handleConnectionApprovalUseCase: HandleConnectionApprovalUseCase,
    private val fetchBalanceUseCase: FetchBalanceUseCase,
    private val saveConnectionStateUseCase: SaveConnectionStateUseCase,
    private val restoreConnectionStateUseCase: RestoreConnectionStateUseCase,
    private val petraWalletConnector: PetraWalletConnector
) : ViewModel() {

    companion object {
        private const val TAG = "WalletViewModel"
    }

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private var currentKeys: ConnectionKeys? = null

    sealed class NavigationEvent {
        data class OpenPetraWallet(val intent: android.content.Intent) : NavigationEvent()
    }

    init {
        restoreConnectionState()
    }

    fun connectWallet() {
        Log.d(TAG, "Initiating wallet connection")

        viewModelScope.launch {
            try {
                val (keys, publicKeyHex) = connectWalletUseCase()
                currentKeys = keys

                val intent = petraWalletConnector.buildConnectionIntent(publicKeyHex)
                _navigationEvents.emit(NavigationEvent.OpenPetraWallet(intent))
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting wallet", e)
                _uiState.update { it.copy(error = "Failed to connect wallet") }
            }
        }
    }

    fun handleDeepLink(uri: Uri) {
        Log.d(TAG, "Received deeplink: $uri")
        val response = uri.getQueryParameter("response")
        val data = uri.getQueryParameter("data")

        Log.d(TAG, "Path: ${uri.path}, Response: $response")

        when (uri.path) {
            "/api/v1/connect", "/v1/connect" -> {
                if (response == "approved" && data != null) {
                    Log.d(TAG, "Connection approved")
                    handleConnectionApproval(data)
                } else {
                    Log.d(TAG, "Connection rejected or missing data")
                    handleConnectionRejection()
                }
            }
            "/api/v1/response", "/v1/response" -> {
                if (response == "approved" && data != null) {
                    Log.d(TAG, "Transaction response received")
                    handleTransactionResponse(data)
                }
            }
        }
    }

    private fun handleConnectionApproval(data: String) {
        val keys = currentKeys
        if (keys == null) {
            Log.e(TAG, "No keys available for connection approval")
            _uiState.update { it.copy(error = "Connection error: missing keys") }
            return
        }

        viewModelScope.launch {
            when (val result = handleConnectionApprovalUseCase(data, keys.secretKey)) {
                is ConnectionResult.Success -> {
                    Log.d(TAG, "Connection successful with address: ${result.address}")

                    val updatedKeys = keys.copy(sharedKey = result.sharedKey)
                    currentKeys = updatedKeys

                    _uiState.update {
                        it.copy(
                            isConnected = true,
                            walletAddress = result.address,
                            error = null
                        )
                    }

                    // Save connection state
                    saveConnectionStateUseCase(result.address, updatedKeys)

                    // Fetch balance
                    fetchBalance(result.address)
                }
                is ConnectionResult.Failure -> {
                    Log.e(TAG, "Connection failed: ${result.error}")
                    _uiState.update { it.copy(error = "Connection failed: ${result.error}") }
                }
                is ConnectionResult.Rejected -> {
                    Log.w(TAG, "Connection rejected by user")
                    _uiState.update { it.copy(error = "Connection rejected") }
                }
            }
        }
    }

    private fun handleConnectionRejection() {
        _uiState.update { it.copy(error = "Connection rejected by user") }
    }

    private fun handleTransactionResponse(data: String) {
        Log.d(TAG, "Transaction response: $data")
        // Future implementation for transaction handling
    }

    private fun fetchBalance(address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = fetchBalanceUseCase(address)) {
                is BalanceResult.Success -> {
                    Log.d(TAG, "Balance fetched: ${result.balance}")
                    _uiState.update {
                        it.copy(
                            walletBalance = result.balance,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is BalanceResult.Failure -> {
                    Log.e(TAG, "Failed to fetch balance: ${result.error}")
                    _uiState.update {
                        it.copy(
                            walletBalance = "Error fetching balance",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun restoreConnectionState() {
        viewModelScope.launch {
            val connection = restoreConnectionStateUseCase()
            if (connection != null && connection.isConnected) {
                Log.d(TAG, "Restored connection for address: ${connection.address}")
                _uiState.update {
                    it.copy(
                        isConnected = true,
                        walletAddress = connection.address,
                        walletBalance = connection.balance
                    )
                }

                // Fetch fresh balance
                fetchBalance(connection.address)
            }
        }
    }

    fun refreshBalance() {
        val address = _uiState.value.walletAddress
        if (address != null) {
            fetchBalance(address)
        }
    }
}

