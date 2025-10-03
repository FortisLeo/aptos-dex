package com.rishitgoklani.aptosdex.presentation.dex

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DexScreen(
    modifier: Modifier = Modifier,
    viewModel: DexViewModel = hiltViewModel(),
    onTokenClick: (symbol: String, name: String, imageUrl: String?, address: String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf(uiState.query) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("Search tokens") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Favourite tokens header
        Text(
            text = "Favourite tokens",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // Favourite tokens horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.favorites.forEach { t ->
                FavouriteTokenCard(
                    symbol = t.symbol,
                    priceUsd = t.priceUsd,
                    changePct = t.changePct24h,
                    imageUrl = t.imageUrl,
                    onClick = { onTokenClick(t.symbol, t.name, t.imageUrl, t.address) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Top Tokens header
        Text(
            text = "Top Tokens",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.topTokens) { t ->
                TopTokenRow(
                    name = t.name,
                    volumeUsd = t.volumeUsd,
                    priceUsd = t.priceUsd,
                    changePct = t.changePct24h,
                    imageUrl = t.imageUrl,
                    onClick = { onTokenClick(t.symbol, t.name, t.imageUrl, t.address) }
                )
            }
        }
    }
}

@Composable
private fun FavouriteTokenCard(
    symbol: String,
    priceUsd: String,
    changePct: Double,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUp = changePct >= 0
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .width(140.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = symbol,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = symbol.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = priceUsd,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val arrow = if (isUp) "▲" else "▼"
                val color = if (isUp) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                Text(
                    text = "$arrow ${String.format("%.2f", kotlin.math.abs(changePct))}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TopTokenRow(
    name: String,
    volumeUsd: String,
    priceUsd: String,
    changePct: Double,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUp = changePct >= 0
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vol $volumeUsd",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = priceUsd,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                val arrow = if (isUp) "▲" else "▼"
                val color = if (isUp) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                Text(
                    text = "$arrow ${String.format("%.2f", kotlin.math.abs(changePct))}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

private fun truncateAddress(address: String, head: Int = 6, tail: Int = 4): String {
    if (address.length <= head + tail + 3) return address
    return address.take(head) + "..." + address.takeLast(tail)
}

