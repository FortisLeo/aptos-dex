package com.rishitgoklani.aptosdex.presentation.tokendetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rishitgoklani.aptosdex.presentation.tokendetail.components.*
import com.rishitgoklani.aptosdex.ui.components.CosmicBackground

/**
 * Token Detail Screen
 * Displays comprehensive token information including:
 * - Price and price changes
 * - Interactive charts (candlestick)
 * - Order book (bids/asks)
 * - Token metrics (market cap, volume, supply)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenDetailScreen(
    tokenSymbol: String,
    tokenName: String,
    tokenImageUrl: String?,
    tokenAddress: String = "",
    onBack: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TokenDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val selectedTimePeriod by viewModel.selectedTimePeriod.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(TabType.CHART) }

    // Load chart data when screen opens
    LaunchedEffect(tokenSymbol) {
        viewModel.loadTokenData(tokenSymbol, tokenAddress)
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CosmicBackground(modifier = Modifier.matchParentSize())

        Column(modifier = Modifier.fillMaxSize()) {
            TokenDetailTopBar(
                tokenSymbol = tokenSymbol,
                tokenName = tokenName,
                tokenImageUrl = tokenImageUrl,
                currentPrice = uiState.currentPrice,
                priceChange = uiState.priceChange24h,
                change24h = uiState.change24h
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Background with gradient fade at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp) // Space for buy button + bottom nav
                ) {
                    // Tabs: Chart / Order Book
                    item {
                        Spacer(Modifier.height(16.dp))
                        UnderlineTabRow(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }

                    // Chart or Order Book Section
                    if (selectedTab == TabType.CHART) {
                        // Chart Section
                        item {
                            Spacer(Modifier.height(16.dp))
                            ChartSection(viewModel = viewModel)
                        }

                        // Time Period Selector (below chart)
                        item {
                            Spacer(Modifier.height(16.dp))
                            UnderlineTimePeriodSelector(
                                selectedPeriod = selectedTimePeriod,
                                onPeriodSelected = { viewModel.onTimePeriodChanged(it) }
                            )
                        }
                    } else {
                        // Order Book Section
                        item {
                            Spacer(Modifier.height(16.dp))
                            val orderBookData = viewModel.generateMockOrderBookData()
                            OrderBookSection(
                                orderBookData = orderBookData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Token Info Grid
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Token Information",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        TokenInfoGrid(uiState = uiState)
                    }

                    // Bottom spacing
                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Buy Button positioned above bottom nav
                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = 64.dp) // Position above bottom nav bar
                ) {
                    BuyButton(onClick = onBuyClick)
                }
            }
        }
    }
}

/**
 * Top app bar showing token identity and price information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TokenDetailTopBar(
    tokenSymbol: String,
    tokenName: String,
    tokenImageUrl: String?,
    currentPrice: String,
    priceChange: String,
    change24h: Double
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = tokenImageUrl,
                    contentDescription = tokenName,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        text = tokenSymbol.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tokenName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            PriceInfo(
                currentPrice = currentPrice,
                priceChange = priceChange,
                change24h = change24h
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Price information displayed in top bar
 */
@Composable
private fun PriceInfo(
    currentPrice: String,
    priceChange: String,
    change24h: Double
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.End,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = currentPrice,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        val changeColor = if (change24h >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        val sign = if (change24h >= 0) "+" else ""
        Text(
            text = "$sign$priceChange (${sign}${String.format(java.util.Locale.US, "%.2f", change24h)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = changeColor
        )
    }
}

/**
 * Chart section wrapper
 */
@Composable
private fun ChartSection(
    viewModel: TokenDetailViewModel
) {
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (chartData.isNotEmpty()) {
            TradingViewChart(
                chartData = chartData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Fixed buy button at bottom
 */
@Composable
private fun BuyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "+ Buy",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
