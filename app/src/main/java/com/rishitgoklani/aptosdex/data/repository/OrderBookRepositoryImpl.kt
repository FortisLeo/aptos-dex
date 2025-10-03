package com.rishitgoklani.aptosdex.data.repository

import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderBook
import com.rishitgoklani.aptosdex.domain.model.OrderSide
import com.rishitgoklani.aptosdex.domain.model.OrderStatus
import com.rishitgoklani.aptosdex.domain.repository.OrderBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of OrderBookRepository for frontend-only orderbook
 */
@Singleton
class OrderBookRepositoryImpl @Inject constructor() : OrderBookRepository {
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    private val orders: Flow<List<Order>> = _orders.asStateFlow()
    
    override fun getOrderBook(symbol: String): Flow<OrderBook> {
        return orders.map { orderList ->
            val symbolOrders = orderList.filter { it.symbol == symbol }
            val buyOrders = symbolOrders
                .filter { it.side == OrderSide.BUY && it.status == OrderStatus.PENDING }
                .sortedByDescending { it.price }
            val sellOrders = symbolOrders
                .filter { it.side == OrderSide.SELL && it.status == OrderStatus.PENDING }
                .sortedBy { it.price }
            
            OrderBook(
                symbol = symbol,
                buyOrders = buyOrders,
                sellOrders = sellOrders,
                lastPrice = null, // Would come from price feed
                priceChange24h = null // Would come from price feed
            )
        }
    }
    
    override suspend fun placeOrder(order: Order): Result<Order> {
        return try {
            val currentOrders = _orders.value.toMutableList()
            currentOrders.add(order)
            _orders.value = currentOrders
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            val currentOrders = _orders.value.toMutableList()
            val orderIndex = currentOrders.indexOfFirst { it.id == orderId }
            if (orderIndex != -1) {
                currentOrders[orderIndex] = currentOrders[orderIndex].copy(status = OrderStatus.CANCELLED)
                _orders.value = currentOrders
                Result.success(Unit)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserOrders(walletAddress: String): Flow<List<Order>> {
        return orders.map { orderList ->
            orderList.filter { it.walletAddress == walletAddress }
        }
    }
    
    override fun getUserOrdersForSymbol(symbol: String, walletAddress: String): Flow<List<Order>> {
        return orders.map { orderList ->
            orderList.filter { it.symbol == symbol && it.walletAddress == walletAddress }
        }
    }
    
    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            val currentOrders = _orders.value.toMutableList()
            val orderIndex = currentOrders.indexOfFirst { it.id == orderId }
            if (orderIndex != -1) {
                currentOrders[orderIndex] = currentOrders[orderIndex].copy(status = status)
                _orders.value = currentOrders
                Result.success(Unit)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }
    
    override suspend fun clearAllOrders() {
        _orders.value = emptyList()
    }
}

