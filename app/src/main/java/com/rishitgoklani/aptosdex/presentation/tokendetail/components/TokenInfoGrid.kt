package com.rishitgoklani.aptosdex.presentation.tokendetail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rishitgoklani.aptosdex.presentation.tokendetail.TokenDetailUiState

/**
 * Grid displaying token information metrics
 * Shows market cap, volume, supply, etc. in a 2-column layout
 */
@Composable
fun TokenInfoGrid(
    uiState: TokenDetailUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Market Cap, Mint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = "Market Cap",
                value = uiState.marketCap,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "Mint",
                value = uiState.mint,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Volume (24h), FDV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = "Volume (24h)",
                value = uiState.volume24h,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "FDV",
                value = uiState.fdv,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Circulating Supply, Total Supply
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = "Circulating Supply",
                value = uiState.circulatingSupply,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "Total Supply",
                value = uiState.totalSupply,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single info card displaying a label and value
 */
@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
