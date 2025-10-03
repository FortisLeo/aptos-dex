package com.rishitgoklani.aptosdex.presentation.dex

data class DexTokenUi(
    val name: String,
    val symbol: String,
    val imageUrl: String? = null,
    val address: String = "",
    val priceUsd: String = "",
    val changePct24h: Double = 0.0,
    val volumeUsd: String = ""
)

data class DexUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val walletName: String = "My Wallet",
    val walletAddress: String = "",
    val query: String = "",
    val favorites: List<DexTokenUi> = emptyList(),
    val topTokens: List<DexTokenUi> = emptyList()
)


