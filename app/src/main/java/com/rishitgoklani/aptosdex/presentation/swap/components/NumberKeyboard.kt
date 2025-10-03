package com.rishitgoklani.aptosdex.presentation.swap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom number keyboard for swap amount input
 * Follows Material Design principles with compact layout
 */
@Composable
fun NumberKeyboard(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onDecimalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Keyboard rows - more compact layout
            val numbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(".", "0", "⌫")
            )
            
            numbers.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { key ->
                        val isBackspace = key == "⌫"
                        val isDecimal = key == "."
                        
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isBackspace -> MaterialTheme.colorScheme.errorContainer
                                        isDecimal -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable {
                                    when {
                                        isBackspace -> onBackspaceClick()
                                        isDecimal -> onDecimalClick()
                                        else -> onNumberClick(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBackspace) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    ),
                                    color = when {
                                        isDecimal -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
