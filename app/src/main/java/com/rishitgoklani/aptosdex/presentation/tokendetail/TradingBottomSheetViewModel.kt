package com.rishitgoklani.aptosdex.presentation.tokendetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderSide
import com.rishitgoklani.aptosdex.domain.model.OrderType as DomainOrderType
import com.rishitgoklani.aptosdex.domain.repository.OrderBookRepository
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import com.rishitgoklani.aptosdex.domain.usecase.PlaceOrderResult
import com.rishitgoklani.aptosdex.domain.usecase.PlaceOrderUseCase
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
import com.rishitgoklani.aptosdex.blockchain.petra.PetraWalletConnector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val petraWalletConnector: PetraWalletConnector,
    private val orderBookRepository: OrderBookRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradingBottomSheetUiState())
    val uiState: StateFlow<TradingBottomSheetUiState> = _uiState.asStateFlow()

    sealed class NavigationEvent {
        data class OpenPetraWallet(val intent: android.content.Intent) : NavigationEvent()
    }

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

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
     * Places order in frontend orderbook (since smart contract is not working)
     */
    fun executeOrder(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (!state.canExecute) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true, errorMessage = null) }

            try {
                val price = state.effectivePrice.toDoubleOrNull() ?: 0.0
                val amount = state.effectiveAmount.toDoubleOrNull() ?: 0.0
                val isBuy = state.selectedTradeType == TradeType.BUY

                Log.d(TAG, "Executing ${if (isBuy) "BUY" else "SELL"} order:")
                Log.d(TAG, "Token: ${state.tokenSymbol}, Price: $price, Amount: $amount")

                // Get wallet address
                val walletAddress = walletRepository.getConnectedWalletAddress()
                if (walletAddress == null) {
                    _uiState.update {
                        it.copy(
                            isExecuting = false,
                            errorMessage = "Connect your wallet to place an order"
                        )
                    }
                    return@launch
                }

                // Create order
                val order = Order(
                    symbol = state.tokenSymbol,
                    side = if (isBuy) OrderSide.BUY else OrderSide.SELL,
                    type = if (state.selectedOrderType == OrderType.LIMIT) DomainOrderType.LIMIT else DomainOrderType.MARKET,
                    price = price,
                    amount = amount,
                    walletAddress = walletAddress
                )

                // Place order in frontend orderbook
                val result = orderBookRepository.placeOrder(order)
                if (result.isSuccess) {
                    Log.d(TAG, "Order placed successfully: ${order.id}")
                    _uiState.update { it.copy(isExecuting = false) }
                    onSuccess()
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Failed to place order"
                    Log.e(TAG, "Order placement failed: $errorMessage")
                    _uiState.update {
                        it.copy(
                            isExecuting = false,
                            errorMessage = errorMessage
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error executing order", e)
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
