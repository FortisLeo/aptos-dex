package com.rishitgoklani.aptosdex.presentation.swap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.rishitgoklani.aptosdex.presentation.tokens.TokenUi
import com.rishitgoklani.aptosdex.presentation.tokens.TokenWhitelist
import com.rishitgoklani.aptosdex.domain.repository.PriceRepository

/**
 * ViewModel for Swap screen
 * Coordinates use cases and manages UI state following MVVM pattern
 * Follows Clean Architecture principles from CLAUDE.md
 */
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val priceRepository: PriceRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SwapViewModel"
    }

    private val _uiState = MutableStateFlow(SwapUiState())
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    init {
        // Default: Swap USDC -> APT
        val from = TokenWhitelist.allowed.firstOrNull { it.symbol.equals("USDC", true) }
        val to = TokenWhitelist.allowed.firstOrNull { it.symbol.equals("APT", true) }
        _uiState.update { state ->
            state.copy(
                fromTokenSymbol = from?.symbol ?: state.fromTokenSymbol,
                toTokenSymbol = to?.symbol ?: state.toTokenSymbol,
                fromTokenAddress = from?.address ?: state.fromTokenAddress,
                toTokenAddress = to?.address ?: state.toTokenAddress,
                fromTokenImageUrl = from?.imageUrl ?: state.fromTokenImageUrl,
                toTokenImageUrl = to?.imageUrl ?: state.toTokenImageUrl,
                fromUsdPrice = 0.0,
                toUsdPrice = 0.0
            )
        }
        refreshPrices()
    }

    /**
     * Handles reset action
     * Clears swap form and resets to initial state
     */
    fun onResetClick() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                errorMessage = null,
                swapAmount = "0.00",
                receiveAmount = "0.00",
                canSwap = false,
                swapButtonText = "Enter amount"
            )
        }
    }

    /**
     * Handles number input from keyboard
     */
    fun onNumberClick(number: String) {
        _uiState.update { currentState ->
            if (currentState.isEditingReceive) {
                val currentAmount = if (currentState.receiveAmount == "0.00") "" else currentState.receiveAmount
                val newAmount = currentAmount + number
                val validatedAmount = validateAmount(newAmount)
                val canSwap = validatedAmount.isNotEmpty() && validatedAmount != "0.00"
                
                currentState.copy(
                    receiveAmount = validatedAmount,
                    swapAmount = calculateSwapAmount(validatedAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            } else {
                val currentAmount = if (currentState.swapAmount == "0.00") "" else currentState.swapAmount
                val newAmount = currentAmount + number
                val validatedAmount = validateAmount(newAmount)
                val canSwap = validatedAmount.isNotEmpty() && validatedAmount != "0.00"
                
                currentState.copy(
                    swapAmount = validatedAmount,
                    receiveAmount = calculateReceiveAmount(validatedAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Handles backspace from keyboard
     */
    fun onBackspaceClick() {
        _uiState.update { currentState ->
            if (currentState.isEditingReceive) {
                val newAmount = if (currentState.receiveAmount.length > 1) {
                    currentState.receiveAmount.dropLast(1)
                } else {
                    "0.00"
                }
                val canSwap = newAmount.isNotEmpty() && newAmount != "0.00"
                
                currentState.copy(
                    receiveAmount = newAmount,
                    swapAmount = calculateSwapAmount(newAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            } else {
                val newAmount = if (currentState.swapAmount.length > 1) {
                    currentState.swapAmount.dropLast(1)
                } else {
                    "0.00"
                }
                val canSwap = newAmount.isNotEmpty() && newAmount != "0.00"
                
                currentState.copy(
                    swapAmount = newAmount,
                    receiveAmount = calculateReceiveAmount(newAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Handles decimal point input
     */
    fun onDecimalClick() {
        _uiState.update { currentState ->
            if (currentState.isEditingReceive) {
                val currentAmount = if (currentState.receiveAmount == "0.00") "" else currentState.receiveAmount
                val newAmount = if (currentAmount.contains(".")) {
                    currentAmount // Don't add decimal if already present
                } else {
                    currentAmount + "."
                }
                val validatedAmount = validateAmount(newAmount)
                val canSwap = validatedAmount.isNotEmpty() && validatedAmount != "0.00"
                
                currentState.copy(
                    receiveAmount = validatedAmount,
                    swapAmount = calculateSwapAmount(validatedAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            } else {
                val currentAmount = if (currentState.swapAmount == "0.00") "" else currentState.swapAmount
                val newAmount = if (currentAmount.contains(".")) {
                    currentAmount // Don't add decimal if already present
                } else {
                    currentAmount + "."
                }
                val validatedAmount = validateAmount(newAmount)
                val canSwap = validatedAmount.isNotEmpty() && validatedAmount != "0.00"
                
                currentState.copy(
                    swapAmount = validatedAmount,
                    receiveAmount = calculateReceiveAmount(validatedAmount),
                    canSwap = canSwap,
                    swapButtonText = if (canSwap) "Swap" else "Enter amount",
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Validates and formats amount input
     */
    private fun validateAmount(amount: String): String {
        if (amount.isEmpty()) return "0.00"
        
        // Remove leading zeros except for decimal
        val cleaned = amount.trimStart('0')
        if (cleaned.isEmpty() || cleaned == ".") return "0.00"
        
        // Limit to 2 decimal places
        val parts = cleaned.split(".")
        return if (parts.size > 2) {
            parts[0] + "." + parts[1].take(2)
        } else if (parts.size == 2 && parts[1].length > 2) {
            parts[0] + "." + parts[1].take(2)
        } else {
            cleaned
        }
    }

    /**
     * Calculates receive amount based on swap amount
     * TODO: Implement actual price calculation
     */
    private fun calculateReceiveAmount(swapAmount: String): String {
        if (swapAmount.isEmpty() || swapAmount == "0.00") return "0.00"
        val amount = swapAmount.toDoubleOrNull() ?: 0.0
        // Use dynamic prices if available; fallback to 1:1
        val rate = if (_uiState.value.toUsdPrice > 0) {
            // amount in FROM * (fromUsdPrice / toUsdPrice)
            _uiState.value.fromUsdPrice / _uiState.value.toUsdPrice
        } else 1.0
        val receiveAmount = amount * rate
        return String.format("%.2f", receiveAmount)
    }

    /**
     * Calculates swap amount based on receive amount
     * TODO: Implement actual price calculation
     */
    private fun calculateSwapAmount(receiveAmount: String): String {
        if (receiveAmount.isEmpty() || receiveAmount == "0.00") return "0.00"
        val amount = receiveAmount.toDoubleOrNull() ?: 0.0
        val rate = if (_uiState.value.fromUsdPrice > 0) {
            _uiState.value.fromUsdPrice / _uiState.value.toUsdPrice
        } else 1.0
        val swapAmount = if (rate == 0.0) 0.0 else amount / rate
        return String.format("%.2f", swapAmount)
    }

    /**
     * Handles MAX button click
     * Sets maximum available amount for swap
     */
    fun onMaxClick() {
        // TODO: Implement max amount logic
    }

    /**
     * Handles token selection
     * Opens token selection dialog
     */
    fun onTokenSelectClick(selectingForReceive: Boolean) {
        _uiState.update { it.copy(isSelectingToken = true, selectingForReceive = selectingForReceive) }
    }

    /**
     * Handles swap direction toggle
     * Swaps the from/to tokens
     */
    fun onSwapDirectionClick() {
        _uiState.update { current ->
            current.copy(
                fromTokenSymbol = current.toTokenSymbol,
                toTokenSymbol = current.fromTokenSymbol,
                fromTokenAddress = current.toTokenAddress,
                toTokenAddress = current.fromTokenAddress,
                fromTokenImageUrl = current.toTokenImageUrl,
                toTokenImageUrl = current.fromTokenImageUrl,
                fromUsdPrice = current.toUsdPrice,
                toUsdPrice = current.fromUsdPrice
            )
        }
    }

    /**
     * Handles swap execution
     * Initiates the swap transaction
     */
    fun onSwapClick() {
        // TODO: Implement swap execution
    }

    /**
     * Handles receive amount click
     * Switches focus to receive amount input
     */
    fun onReceiveAmountClick() {
        _uiState.update { currentState ->
            currentState.copy(
                isEditingReceive = true,
                errorMessage = null
            )
        }
    }

    /**
     * Handles swap amount click
     * Switches focus to swap amount input
     */
    fun onSwapAmountClick() {
        _uiState.update { currentState ->
            currentState.copy(
                isEditingReceive = false,
                errorMessage = null
            )
        }
    }

    fun onTokenChosen(token: TokenUi, selectingForReceive: Boolean) {
        _uiState.update { current ->
            if (selectingForReceive) {
                current.copy(
                    toTokenSymbol = token.symbol,
                    toTokenAddress = token.address,
                    toTokenImageUrl = token.imageUrl,
                    isSelectingToken = false
                )
            } else {
                current.copy(
                    fromTokenSymbol = token.symbol,
                    fromTokenAddress = token.address,
                    fromTokenImageUrl = token.imageUrl,
                    isSelectingToken = false
                )
            }
        }
        refreshPrices()
    }

    private fun refreshPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            val fromSymbol = _uiState.value.fromTokenSymbol
            val toSymbol = _uiState.value.toTokenSymbol
            val prices = runCatching { priceRepository.getUsdPrices(fromSymbol, toSymbol) }.getOrNull()
                ?: (0.0 to 0.0)
            var fromPrice = prices.first
            var toPrice = prices.second
            if (toPrice == 0.0) toPrice = 1.0
            val current = _uiState.value
            val isEditingReceive = current.isEditingReceive
            val newSwapAmount: String
            val newReceiveAmount: String
            if (isEditingReceive) {
                val recvAmt = current.receiveAmount.toDoubleOrNull() ?: 0.0
                val rate = if (fromPrice > 0) fromPrice / toPrice else 0.0
                val swapVal = if (rate == 0.0) 0.0 else recvAmt / rate
                newReceiveAmount = String.format("%.2f", recvAmt)
                newSwapAmount = String.format("%.2f", swapVal)
            } else {
                val swapAmt = current.swapAmount.toDoubleOrNull() ?: 0.0
                val rate = if (toPrice > 0) fromPrice / toPrice else 0.0
                val recvVal = swapAmt * rate
                newSwapAmount = String.format("%.2f", swapAmt)
                newReceiveAmount = String.format("%.2f", recvVal)
            }
            _uiState.update { it.copy(
                fromUsdPrice = fromPrice,
                toUsdPrice = toPrice,
                swapAmount = newSwapAmount,
                receiveAmount = newReceiveAmount
            ) }
        }
    }

    // Hermes reference: https://hermes.pyth.network/docs/#/
}

