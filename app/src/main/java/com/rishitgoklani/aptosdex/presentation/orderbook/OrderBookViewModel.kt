package com.rishitgoklani.aptosdex.presentation.orderbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderBook
import com.rishitgoklani.aptosdex.domain.model.OrderStatus
import com.rishitgoklani.aptosdex.domain.repository.OrderBookRepository
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 * Manages orderbook data and user orders
 */
@HiltViewModel
class OrderBookViewModel @Inject constructor(
    private val orderBookRepository: OrderBookRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderBookUiState())
    val uiState: StateFlow<OrderBookUiState> = _uiState.asStateFlow()

    private var currentSymbol: String = ""

    /**
     * Initialize the orderbook for a specific symbol
     */
    fun initialize(symbol: String) {
        currentSymbol = symbol
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // Get wallet address
                val walletAddress = walletRepository.getConnectedWalletAddress()
                
                if (walletAddress != null) {
                    // Combine orderbook and user orders
                    combine(
                        orderBookRepository.getOrderBook(symbol),
                        orderBookRepository.getUserOrdersForSymbol(symbol, walletAddress)
                    ) { orderBook, userOrders ->
                        OrderBookUiState(
                            orderBook = orderBook,
                            userOrders = userOrders,
                            isLoading = false,
                            errorMessage = null
                        )
                    }.collect { newState ->
                        _uiState.value = newState
                    }
                } else {
                    // No wallet connected, just show orderbook
                    orderBookRepository.getOrderBook(symbol).collect { orderBook ->
                        _uiState.update {
                            it.copy(
                                orderBook = orderBook,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load orderbook"
                    )
                }
            }
        }
    }

    /**
     * Cancel an order
     */
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                orderBookRepository.cancelOrder(orderId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to cancel order")
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
