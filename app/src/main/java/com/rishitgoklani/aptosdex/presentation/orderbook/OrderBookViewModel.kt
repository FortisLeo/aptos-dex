package com.rishitgoklani.aptosdex.presentation.orderbook

import androidx.lifecycle.ViewModel
import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state for OrderBook screen
 */
data class OrderBookUiState(
    val orderBook: OrderBook = OrderBook(""),
    val userOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for OrderBook screen
 * Manages orderbook data and user orders (UI only - no integration)
 */
@HiltViewModel
class OrderBookViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OrderBookUiState())
    val uiState: StateFlow<OrderBookUiState> = _uiState.asStateFlow()

    private var currentSymbol: String = ""

    /**
     * Initialize the orderbook for a specific symbol
     * Currently displays empty state - integration removed
     */
    fun initialize(symbol: String) {
        currentSymbol = symbol
        _uiState.update {
            it.copy(
                orderBook = OrderBook(symbol),
                isLoading = false,
                errorMessage = null
            )
        }
    }

    /**
     * Cancel an order
     * Currently a no-op - integration removed
     */
    fun cancelOrder(orderId: String) {
        // No-op: integration removed
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
