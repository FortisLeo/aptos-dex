package com.rishitgoklani.aptosdex.presentation.tokendetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.presentation.tokendetail.components.OrderType
import com.rishitgoklani.aptosdex.presentation.tokendetail.components.TradeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * UI state for trading bottom sheet
 */
data class TradingBottomSheetUiState(
    val tokenSymbol: String = "",
    val currentMarketPrice: String = "",
    val selectedTradeType: TradeType = TradeType.BUY,
    val selectedOrderType: OrderType = OrderType.MARKET,
    val priceInput: String = "",
    val amountInput: String = "0",
    val isExecuting: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Get effective price based on order type
     * MARKET: uses current market price
     * LIMIT: uses user input price
     */
    val effectivePrice: String
        get() = if (selectedOrderType == OrderType.MARKET) {
            currentMarketPrice.replace("$", "")
        } else {
            priceInput
        }

    /**
     * Get effective amount based on order type
     * MARKET: uses user input amount
     * LIMIT: auto-calculates based on $1000 trade value
     */
    val effectiveAmount: String
        get() = if (selectedOrderType == OrderType.LIMIT) {
            val price = priceInput.toDoubleOrNull() ?: 0.0
            if (price > 0) {
                val defaultTradeValue = 1000.0
                String.format(Locale.US, "%.4f", defaultTradeValue / price)
            } else {
                ""
            }
        } else {
            amountInput
        }

    /**
     * Calculate liquidation price
     */
    val liquidationPrice: String
        get() {
            val priceValue = effectivePrice.toDoubleOrNull() ?: return "--"
            val liquidationPrice = if (selectedTradeType == TradeType.BUY) {
                priceValue * 0.9
            } else {
                priceValue * 1.1
            }
            return "$${String.format(Locale.US, "%.2f", liquidationPrice)}"
        }

    /**
     * Calculate order value (price * amount)
     */
    val orderValue: String
        get() {
            val priceValue = effectivePrice.toDoubleOrNull() ?: return "--"
            val amountValue = effectiveAmount.toDoubleOrNull() ?: return "--"
            val orderValue = priceValue * amountValue
            return "$${String.format(Locale.US, "%.2f", orderValue)}"
        }

    /**
     * Calculate margin required (20% of order value)
     */
    val marginRequired: String
        get() {
            val priceValue = effectivePrice.toDoubleOrNull() ?: return "--"
            val amountValue = effectiveAmount.toDoubleOrNull() ?: return "--"
            val marginRequired = (priceValue * amountValue) * 0.2
            return "$${String.format(Locale.US, "%.2f", marginRequired)}"
        }

    /**
     * Check if execute button should be enabled
     */
    val canExecute: Boolean
        get() = !isExecuting &&
                effectivePrice.isNotBlank() &&
                effectiveAmount.isNotBlank() &&
                (effectiveAmount.toDoubleOrNull() ?: 0.0) > 0
}

/**
 * ViewModel for trading bottom sheet
 * Manages trading state, input validation, and order execution
 */
@HiltViewModel
class TradingBottomSheetViewModel @Inject constructor(
    // TODO: Inject trading use cases when implementing smart contracts
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradingBottomSheetUiState())
    val uiState: StateFlow<TradingBottomSheetUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "TradingBottomSheetVM"
    }

    /**
     * Initialize bottom sheet with token data
     */
    fun initialize(tokenSymbol: String, currentPrice: String) {
        _uiState.update {
            it.copy(
                tokenSymbol = tokenSymbol,
                currentMarketPrice = currentPrice,
                priceInput = currentPrice.replace("$", "")
            )
        }
    }

    /**
     * Update selected trade type (BUY/SELL)
     */
    fun onTradeTypeChanged(tradeType: TradeType) {
        _uiState.update { it.copy(selectedTradeType = tradeType) }
    }

    /**
     * Update selected order type (MARKET/LIMIT)
     */
    fun onOrderTypeChanged(orderType: OrderType) {
        _uiState.update { it.copy(selectedOrderType = orderType) }
    }

    /**
     * Update price input (only for LIMIT orders)
     */
    fun onPriceChanged(price: String) {
        if (_uiState.value.selectedOrderType == OrderType.LIMIT) {
            _uiState.update { it.copy(priceInput = price) }
        }
    }

    /**
     * Update amount input (only for MARKET orders)
     */
    fun onAmountChanged(amount: String) {
        // Prevent negative values
        if (amount.isEmpty() || amount == "-") {
            _uiState.update { it.copy(amountInput = "0") }
        } else {
            val newValue = amount.toDoubleOrNull()
            if (newValue != null && newValue >= 0) {
                _uiState.update { it.copy(amountInput = amount) }
            }
        }
    }

    /**
     * Increment amount by 0.1
     */
    fun incrementAmount() {
        val current = _uiState.value.amountInput.toDoubleOrNull() ?: 0.0
        val newAmount = String.format(Locale.US, "%.4f", current + 0.1)
        _uiState.update { it.copy(amountInput = newAmount) }
    }

    /**
     * Decrement amount by 0.1 (minimum 0)
     */
    fun decrementAmount() {
        val current = _uiState.value.amountInput.toDoubleOrNull() ?: 0.0
        if (current > 0) {
            val newAmount = String.format(Locale.US, "%.4f", (current - 0.1).coerceAtLeast(0.0))
            _uiState.update { it.copy(amountInput = newAmount) }
        }
    }

    /**
     * Execute the trade order
     * TODO: Implement smart contract integration
     */
    fun executeOrder(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (!state.canExecute) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true, errorMessage = null) }

            try {
                // TODO: Implement order execution logic
                // 1. Validate wallet balance
                // 2. Build transaction
                // 3. Sign transaction
                // 4. Submit to blockchain
                // 5. Wait for confirmation

                // Simulating order execution
                kotlinx.coroutines.delay(1000)

                // On success
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        errorMessage = null
                    )
                }
                onSuccess()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExecuting = false,
                        errorMessage = e.message ?: "Failed to execute order"
                    )
                }
            }
        }
    }

    /**
     * Reset state when bottom sheet is dismissed
     */
    fun onDismiss() {
        _uiState.update {
            TradingBottomSheetUiState(
                tokenSymbol = it.tokenSymbol,
                currentMarketPrice = it.currentMarketPrice,
                priceInput = it.currentMarketPrice.replace("$", "")
            )
        }
    }
}
