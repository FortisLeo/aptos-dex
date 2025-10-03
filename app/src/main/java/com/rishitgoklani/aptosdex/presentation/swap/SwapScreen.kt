package com.rishitgoklani.aptosdex.presentation.swap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rishitgoklani.aptosdex.presentation.swap.components.NumberKeyboard
import com.rishitgoklani.aptosdex.presentation.swap.components.SwapArrow
import com.rishitgoklani.aptosdex.presentation.swap.components.SwapCard
import com.rishitgoklani.aptosdex.presentation.tokens.SelectTokenScreen
import com.rishitgoklani.aptosdex.presentation.tokens.TokenUi
import com.rishitgoklani.aptosdex.ui.components.CosmicBackground

/**
 * Swap screen following Clean Architecture and MVVM pattern
 * Pure declarative UI that observes ViewModel state
 */
@Composable
fun SwapScreen(
    modifier: Modifier = Modifier,
    viewModel: SwapViewModel = hiltViewModel(),
    onOpenSelectToken: (selectingForReceive: Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CosmicBackground(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top app bar section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                // Swap tokens text centered
                Text(
                    text = "Swap tokens",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Reset button on the right
                TextButton(
                    onClick = { viewModel.onResetClick() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                // Swap Card
                SwapCard(
                    title = "Swap",
                    quantity = uiState.swapAmount,
                    priceInDollars = "~$" + String.format(
                        "%.2f",
                        (uiState.swapAmount.toDoubleOrNull() ?: 0.0) * uiState.fromUsdPrice
                    ),
                    tokenSymbol = uiState.fromTokenSymbol,
                    tokenImageUrl = uiState.fromTokenImageUrl,
                    availableAmount = "5.00",
                    onTokenSelectClick = { onOpenSelectToken(false) },
                    onMaxClick = { viewModel.onMaxClick() },
                    onQuantityClick = { viewModel.onSwapAmountClick() }
                )

                // Swap Arrow
                SwapArrow(
                    onSwapClick = { viewModel.onSwapDirectionClick() }
                )

                // Receive Card
                SwapCard(
                    title = "Receive",
                    quantity = uiState.receiveAmount,
                    priceInDollars = "~$" + String.format(
                        "%.2f",
                        (uiState.receiveAmount.toDoubleOrNull() ?: 0.0) * uiState.toUsdPrice
                    ),
                    tokenSymbol = uiState.toTokenSymbol,
                    tokenImageUrl = uiState.toTokenImageUrl,
                    availableAmount = "1,250.00",
                    onTokenSelectClick = { onOpenSelectToken(true) },
                    onQuantityClick = { viewModel.onReceiveAmountClick() }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Error message
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Swap Button
                Button(
                    onClick = { viewModel.onSwapClick() },
                    enabled = uiState.canSwap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.canSwap) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = uiState.swapButtonText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (uiState.canSwap) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Number Keyboard
                NumberKeyboard(
                    onNumberClick = { number -> viewModel.onNumberClick(number) },
                    onBackspaceClick = { viewModel.onBackspaceClick() },
                    onDecimalClick = { viewModel.onDecimalClick() }
                )
            }

            // Selection handled via navigation; overlay removed
        }
    }
}
