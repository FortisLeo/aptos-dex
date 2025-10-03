package com.rishitgoklani.aptosdex.presentation.tokendetail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rishitgoklani.aptosdex.presentation.tokendetail.OrderBookData

/**
 * Order book view displaying bids (green/left) and asks (red/right)
 */
@Composable
fun OrderBookSection(
    orderBookData: OrderBookData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bids Header (Green - Left)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bids",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Asks Header (Red - Right)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Asks",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFC62828)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        // Order Book Rows
        val maxRows = maxOf(orderBookData.bids.size, orderBookData.asks.size)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            (0 until maxRows).forEach { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bid Row (Green - Left)
                    if (index < orderBookData.bids.size) {
                        val bid = orderBookData.bids[index]
                        OrderBookRow(
                            price = bid.price,
                            amount = bid.amount,
                            isGreen = true,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Ask Row (Red - Right)
                    if (index < orderBookData.asks.size) {
                        val ask = orderBookData.asks[index]
                        OrderBookRow(
                            price = ask.price,
                            amount = ask.amount,
                            isGreen = false,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Single order book row showing price and amount
 */
@Composable
private fun OrderBookRow(
    price: String,
    amount: String,
    isGreen: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isGreen) {
        Color(0xFF2E7D32).copy(alpha = 0.08f)
    } else {
        Color(0xFFC62828).copy(alpha = 0.08f)
    }
    val textColor = if (isGreen) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = price,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
