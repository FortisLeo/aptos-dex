package com.rishitgoklani.aptosdex.domain.model

data class WalletConnection(
    val address: String,
    val balance: String? = null,
    val isConnected: Boolean = false
)
