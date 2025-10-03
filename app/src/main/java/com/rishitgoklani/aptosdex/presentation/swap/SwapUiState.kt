package com.rishitgoklani.aptosdex.presentation.swap

/**
 * Immutable UI state for the Swap screen
 * Follows Clean Architecture principles from CLAUDE.md
 */
data class SwapUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val swapAmount: String = "0.00",
    val receiveAmount: String = "0.00",
    val canSwap: Boolean = false,
    val swapButtonText: String = "Enter amount",
    val isEditingReceive: Boolean = false,
    val fromTokenSymbol: String = "USDC",
    val toTokenSymbol: String = "APT",
    val fromTokenAddress: String = "",
    val toTokenAddress: String = "",
    val fromTokenImageUrl: String? = null,
    val toTokenImageUrl: String? = null,
    val fromUsdPrice: Double = 0.0,
    val toUsdPrice: Double = 1.0,
    val isSelectingToken: Boolean = false,
    val selectingForReceive: Boolean = false
)

