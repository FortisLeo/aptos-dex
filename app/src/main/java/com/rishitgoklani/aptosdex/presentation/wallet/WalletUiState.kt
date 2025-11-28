package com.rishitgoklani.aptosdex.presentation.wallet

data class WalletUiState(
    val isConnected: Boolean = false,
    val walletAddress: String? = null,
    val walletBalance: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

