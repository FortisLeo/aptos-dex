package com.rishitgoklani.aptosdex.presentation.tokendetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

enum class TradeType {
    BUY, SELL
}

enum class OrderType {
    MARKET, LIMIT
}

/**
 * Trading bottom sheet for executing buy/sell orders
 * Displays trading pair, order type selection, price/amount inputs, and execution button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingBottomSheet(
    tokenSymbol: String,
    currentPrice: String,
    onDismiss: () -> Unit,
    onExecute: (tradeType: TradeType, orderType: OrderType, price: String, amount: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTradeType by remember { mutableStateOf(TradeType.BUY) }
    var selectedOrderType by remember { mutableStateOf(OrderType.MARKET) }
    var priceInput by remember { mutableStateOf(currentPrice.replace("$", "")) }
    var amountInput by remember { mutableStateOf("0") }

    // For MARKET: price is fixed (current market price), amount is editable
    // For LIMIT: both price and amount are editable
    val effectivePrice = if (selectedOrderType == OrderType.MARKET) {
        currentPrice.replace("$", "")
    } else {
        priceInput
    }

    val effectiveAmount = if (selectedOrderType == OrderType.LIMIT) {
        val price = priceInput.toDoubleOrNull() ?: 0.0
        if (price > 0) {
            val defaultTradeValue = 1000.0
            String.format("%.4f", defaultTradeValue / price)
        } else {
            ""
        }
    } else {
        amountInput
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Trading Pair Header
            Text(
                text = "$tokenSymbol/USDC",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Buy/Sell Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TradeTypeButton(
                    text = "Buy",
                    isSelected = selectedTradeType == TradeType.BUY,
                    onClick = { selectedTradeType = TradeType.BUY },
                    isBuy = true,
                    modifier = Modifier.weight(1f)
                )
                TradeTypeButton(
                    text = "Sell",
                    isSelected = selectedTradeType == TradeType.SELL,
                    onClick = { selectedTradeType = TradeType.SELL },
                    isBuy = false,
                    modifier = Modifier.weight(1f)
                )
            }

            // Market/Limit Tabs
            TradingOrderTypeTabs(
                selectedOrderType = selectedOrderType,
                onOrderTypeSelected = { selectedOrderType = it },
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Price Input - Editable only in LIMIT mode
            OutlinedTextField(
                value = effectivePrice,
                onValueChange = {
                    if (selectedOrderType == OrderType.LIMIT) {
                        priceInput = it
                    }
                },
                label = { Text("Price (USDC)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                enabled = selectedOrderType == OrderType.LIMIT,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Amount Input with +/- controls (for MARKET) or auto-calculated (for LIMIT)
            if (selectedOrderType == OrderType.MARKET) {
                // MARKET mode: Amount is editable with +/- controls and numeric keyboard
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        // Prevent negative values and invalid input
                        if (it.isEmpty() || it == "-") {
                            amountInput = "0"
                        } else {
                            val newValue = it.toDoubleOrNull()
                            if (newValue != null && newValue >= 0) {
                                amountInput = it
                            }
                        }
                    },
                    label = { Text("Amount ($tokenSymbol)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Minus button
                            IconButton(
                                onClick = {
                                    val current = amountInput.toDoubleOrNull() ?: 0.0
                                    if (current > 0) {
                                        amountInput = String.format("%.4f", (current - 0.1).coerceAtLeast(0.0))
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "-",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            // Plus button
                            IconButton(
                                onClick = {
                                    val current = amountInput.toDoubleOrNull() ?: 0.0
                                    amountInput = String.format("%.4f", current + 0.1)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            } else {
                // LIMIT mode: Amount is auto-calculated, non-editable
                OutlinedTextField(
                    value = effectiveAmount,
                    onValueChange = { },
                    label = { Text("Amount ($tokenSymbol)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    singleLine = true,
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Trading Info
            TradingInfoRow(
                label = "Liquidation Price",
                value = calculateLiquidationPrice(effectivePrice, selectedTradeType),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TradingInfoRow(
                label = "Order Value",
                value = calculateOrderValue(effectivePrice, effectiveAmount),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TradingInfoRow(
                label = "Margin Required",
                value = calculateMarginRequired(effectivePrice, effectiveAmount),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Execute Button
            Button(
                onClick = {
                    onExecute(selectedTradeType, selectedOrderType, effectivePrice, effectiveAmount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTradeType == TradeType.BUY) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                ),
                enabled = effectivePrice.isNotBlank() && effectiveAmount.isNotBlank() &&
                         effectiveAmount.toDoubleOrNull() ?: 0.0 > 0
            ) {
                Text(
                    text = "Execute ${if (selectedTradeType == TradeType.BUY) "Buy" else "Sell"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Buy/Sell toggle button
 */
@Composable
private fun TradeTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isBuy: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected && isBuy -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        isSelected && !isBuy -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isSelected && isBuy -> MaterialTheme.colorScheme.tertiary
        isSelected && !isBuy -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Market/Limit tabs with underline effect
 */
@Composable
private fun TradingOrderTypeTabs(
    selectedOrderType: OrderType,
    onOrderTypeSelected: (OrderType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            OrderType.entries.forEach { orderType ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrderTypeSelected(orderType) }
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = orderType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selectedOrderType == orderType) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedOrderType == orderType) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (selectedOrderType == orderType) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Trading info row displaying label and value
 */
@Composable
private fun TradingInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Calculate liquidation price based on entry price and trade type
 */
private fun calculateLiquidationPrice(price: String, tradeType: TradeType): String {
    val priceValue = price.toDoubleOrNull() ?: return "--"
    // Simple calculation: ±10% from entry price
    val liquidationPrice = if (tradeType == TradeType.BUY) {
        priceValue * 0.9
    } else {
        priceValue * 1.1
    }
    return "$${String.format("%.2f", liquidationPrice)}"
}

/**
 * Calculate order value (price * amount)
 */
private fun calculateOrderValue(price: String, amount: String): String {
    val priceValue = price.toDoubleOrNull() ?: return "--"
    val amountValue = amount.toDoubleOrNull() ?: return "--"
    val orderValue = priceValue * amountValue
    return "$${String.format("%.2f", orderValue)}"
}

/**
 * Calculate margin required (20% of order value)
 */
private fun calculateMarginRequired(price: String, amount: String): String {
    val priceValue = price.toDoubleOrNull() ?: return "--"
    val amountValue = amount.toDoubleOrNull() ?: return "--"
    val marginRequired = (priceValue * amountValue) * 0.2
    return "$${String.format("%.2f", marginRequired)}"
}
