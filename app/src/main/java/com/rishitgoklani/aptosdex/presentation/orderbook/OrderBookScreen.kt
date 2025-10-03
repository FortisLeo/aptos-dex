package com.rishitgoklani.aptosdex.presentation.orderbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rishitgoklani.aptosdex.domain.model.Order
import com.rishitgoklani.aptosdex.domain.model.OrderSide
import com.rishitgoklani.aptosdex.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * OrderBook Screen showing buy/sell orders and user's orders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBookScreen(
    symbol: String,
    onNavigateBack: () -> Unit,
    onNavigateToTrading: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderBookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Initialize with symbol
    LaunchedEffect(symbol) {
        viewModel.initialize(symbol)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$symbol OrderBook") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToTrading) {
                        Text("Trade")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Market Summary
            MarketSummaryCard(
                symbol = symbol,
                bestBid = uiState.orderBook.bestBid,
                bestAsk = uiState.orderBook.bestAsk,
                spread = uiState.orderBook.spread,
                lastPrice = uiState.orderBook.lastPrice,
                priceChange24h = uiState.orderBook.priceChange24h
            )

            // OrderBook Tabs
            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("OrderBook") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Orders") }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> OrderBookContent(
                    buyOrders = uiState.orderBook.buyOrders,
                    sellOrders = uiState.orderBook.sellOrders,
                    modifier = Modifier.weight(1f)
                )
                1 -> MyOrdersContent(
                    orders = uiState.userOrders,
                    onCancelOrder = { orderId -> viewModel.cancelOrder(orderId) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MarketSummaryCard(
    symbol: String,
    bestBid: Double?,
    bestAsk: Double?,
    spread: Double?,
    lastPrice: Double?,
    priceChange24h: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Best Bid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bestBid?.let { "$${String.format("%.2f", it)}" } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column {
                    Text(
                        text = "Best Ask",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bestAsk?.let { "$${String.format("%.2f", it)}" } ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                }
                
                Column {
                    Text(
                        text = "Spread",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = spread?.let { "$${String.format("%.2f", it)}" } ?: "--",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderBookContent(
    buyOrders: List<Order>,
    sellOrders: List<Order>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = "Price",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Amount",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Total",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }

        LazyColumn {
            // Sell orders (asks) - red
            items(sellOrders.reversed()) { order ->
                OrderBookItem(
                    order = order,
                    isSell = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Buy orders (bids) - green
            items(buyOrders) { order ->
                OrderBookItem(
                    order = order,
                    isSell = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MyOrdersContent(
    orders: List<Order>,
    onCancelOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (orders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No orders placed yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(orders) { order ->
                MyOrderItem(
                    order = order,
                    onCancel = { onCancelOrder(order.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OrderBookItem(
    order: Order,
    isSell: Boolean,
    modifier: Modifier = Modifier
) {
    val priceColor = if (isSell) Color(0xFFF44336) else Color(0xFF4CAF50)
    
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$${String.format("%.2f", order.price)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = priceColor
        )
        Text(
            text = String.format("%.4f", order.amount),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$${String.format("%.2f", order.total)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun MyOrderItem(
    order: Order,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sideColor = if (order.isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.PARTIAL -> Color(0xFF2196F3)
        OrderStatus.FILLED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color(0xFF9E9E9E)
        OrderStatus.REJECTED -> Color(0xFFF44336)
    }
    
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.side.name} ${order.symbol}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = sideColor
                )
                
                if (order.status == OrderStatus.PENDING) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel Order",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price: $${String.format("%.2f", order.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Amount: ${String.format("%.4f", order.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: $${String.format("%.2f", order.total)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = timeFormat.format(Date(order.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = order.status.name,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

