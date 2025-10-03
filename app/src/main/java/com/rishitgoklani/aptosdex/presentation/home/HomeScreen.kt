package com.rishitgoklani.aptosdex.presentation.home

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rishitgoklani.aptosdex.presentation.dex.DexScreen
import com.rishitgoklani.aptosdex.presentation.swap.SwapScreen
import com.rishitgoklani.aptosdex.presentation.tokens.SelectTokenScreen
import com.rishitgoklani.aptosdex.presentation.tokendetail.TokenDetailScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.rishitgoklani.aptosdex.presentation.swap.SwapViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import com.rishitgoklani.aptosdex.ui.components.CosmicBackground


enum class HomeTab(val title: String, val icon: ImageVector) {
    DEX("DEX", Icons.Filled.TrendingUp),
    SWAP("Swap", Icons.Filled.SwapVert)
}

data class TokenDetailNavArgs(
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val address: String = ""
)

@Composable
fun HomeScreen(
    walletAddress: String?,
    walletBalance: String?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(HomeTab.DEX) }
    var showSelectToken by remember { mutableStateOf(false) }
    var selectingForReceive by remember { mutableStateOf(false) }
    var showTokenDetail by remember { mutableStateOf(false) }
    var tokenDetailArgs by remember { mutableStateOf<TokenDetailNavArgs?>(null) }

    // Get SwapViewModel instance to pre-select token
    val swapVm: SwapViewModel = hiltViewModel()

    // Haze state for blur effect
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CosmicBackground(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            // Wallet Header (show only on DEX screen, not on token detail or when selecting tokens)
            if (!showTokenDetail && !showSelectToken && selectedTab == HomeTab.DEX) {
                WalletHeader(
                    walletAddress = walletAddress,
                    walletBalance = walletBalance
                )
            }

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                when {
                    showTokenDetail && tokenDetailArgs != null -> {
                        BackHandler(enabled = true) {
                            showTokenDetail = false
                            tokenDetailArgs = null
                        }
                        TokenDetailScreen(
                            tokenSymbol = tokenDetailArgs!!.symbol,
                            tokenName = tokenDetailArgs!!.name,
                            tokenImageUrl = tokenDetailArgs!!.imageUrl,
                            tokenAddress = tokenDetailArgs!!.address,
                            onBack = {
                                showTokenDetail = false
                                tokenDetailArgs = null
                            },
                            onBuyClick = {
                                // Find the token from whitelist
                                val token =
                                    com.rishitgoklani.aptosdex.presentation.tokens.TokenWhitelist.allowed
                                        .firstOrNull {
                                            it.symbol.equals(
                                                tokenDetailArgs!!.symbol,
                                                ignoreCase = true
                                            )
                                        }

                                // Set as receive token if found
                                token?.let {
                                    swapVm.onTokenChosen(it, selectingForReceive = true)
                                }

                                // Navigate to swap tab
                                selectedTab = HomeTab.SWAP

                                // Close token detail
                                showTokenDetail = false
                                tokenDetailArgs = null
                            }
                        )
                    }

                    else -> {
                        when (selectedTab) {
                            HomeTab.SWAP -> {
                                if (showSelectToken) {
                                    BackHandler(enabled = true) { showSelectToken = false }
                                    SelectTokenScreen(
                                        onBack = { showSelectToken = false },
                                        onTokenSelected = { token ->
                                            swapVm.onTokenChosen(token, selectingForReceive)
                                            showSelectToken = false
                                        }
                                    )
                                } else {
                                    SwapScreen(
                                        viewModel = swapVm,
                                        onOpenSelectToken = { forReceive ->
                                            selectingForReceive = forReceive
                                            showSelectToken = true
                                        }
                                    )
                                }
                            }

                            HomeTab.DEX -> {
                                DexScreen(
                                    onTokenClick = { symbol, name, imageUrl, address ->
                                        tokenDetailArgs = TokenDetailNavArgs(symbol, name, imageUrl, address)
                                        showTokenDetail = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rounded Bottom Navigation Bar with Haze
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeStyle.Unspecified),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeTab.entries.forEach { tab ->
                        BottomNavItem(
                            icon = tab.icon,
                            label = tab.title,
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom navigation item with icon and label
 */
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Wallet header displaying account information
 * Shows wallet avatar, name, address with copy functionality
 */
@Composable
private fun WalletHeader(
    walletAddress: String?,
    walletBalance: String?
) {
    val clipboard = LocalClipboardManager.current
    val displayAddress = walletAddress ?: "0x0000000000000000"
    val displayName = "My Wallet"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Wallet Avatar
        AsyncImage(
            model = "https://api.dicebear.com/7.x/identicon/png?seed=$displayAddress",
            contentDescription = "Wallet Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        // Wallet Name and Address
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = truncateAddress(displayAddress),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            clipboard.setText(AnnotatedString(displayAddress))
                        }
                )
            }
        }
    }
}

/**
 * Truncates wallet address for display
 * Format: 0x1a2b...9a0b
 */
private fun truncateAddress(address: String): String {
    return if (address.length > 12) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
}
