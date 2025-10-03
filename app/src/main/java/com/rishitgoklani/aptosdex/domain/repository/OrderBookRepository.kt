package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderBook
import com.rishitgoklani.aptosdex.domain.model.OrderSide
import com.rishitgoklani.aptosdex.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing orderbook data and orders
 */
interface OrderBookRepository {
    
    /**
     * Get the orderbook for a specific symbol
     */
    fun getOrderBook(symbol: String): Flow<OrderBook>
    
    /**
     * Place a new order
     */
    suspend fun placeOrder(order: Order): Result<Order>
    
    /**
     * Cancel an existing order
     */
    suspend fun cancelOrder(orderId: String): Result<Unit>
    
    /**
     * Get all orders for a specific wallet address
     */
    fun getUserOrders(walletAddress: String): Flow<List<Order>>
    
    /**
     * Get orders for a specific symbol and wallet
     */
    fun getUserOrdersForSymbol(symbol: String, walletAddress: String): Flow<List<Order>>
    
    /**
     * Update order status
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
    
    /**
     * Get order by ID
     */
    suspend fun getOrderById(orderId: String): Order?
    
    /**
     * Clear all orders (for testing)
     */
    suspend fun clearAllOrders()
}

