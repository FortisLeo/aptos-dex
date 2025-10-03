package com.rishitgoklani.aptosdex.presentation.tokendetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.domain.model.ChartDataResult
import com.rishitgoklani.aptosdex.domain.model.ChartTimePeriod
import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult
import com.rishitgoklani.aptosdex.domain.model.OrderBookLevel
import com.rishitgoklani.aptosdex.domain.model.OrderBookResult
import com.rishitgoklani.aptosdex.domain.usecase.FetchChartDataUseCase
import com.rishitgoklani.aptosdex.domain.usecase.FetchOrderBookSnapshotUseCase
import com.rishitgoklani.aptosdex.domain.usecase.FetchTokenPricesUseCase
import com.rishitgoklani.aptosdex.domain.usecase.SubscribeToPriceUpdatesUseCase
import com.rishitgoklani.aptosdex.domain.usecase.SubscribeToOrderBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Token Detail screen
 * Manages UI state, chart data, and real-time WebSocket updates
 */
@HiltViewModel
class TokenDetailViewModel @Inject constructor(
    private val fetchChartDataUseCase: FetchChartDataUseCase,
    private val fetchTokenPricesUseCase: FetchTokenPricesUseCase,
    private val fetchOrderBookSnapshotUseCase: FetchOrderBookSnapshotUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val subscribeToOrderBookUseCase: SubscribeToOrderBookUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "TokenDetailViewModel"
    }

    private val _uiState = MutableStateFlow(TokenDetailUiState())
    val uiState: StateFlow<TokenDetailUiState> = _uiState.asStateFlow()

    private val _chartData = MutableStateFlow<List<CandlestickData>>(emptyList())
    val chartData: StateFlow<List<CandlestickData>> = _chartData.asStateFlow()

    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.HOURS_24)
    val selectedTimePeriod: StateFlow<TimePeriod> = _selectedTimePeriod.asStateFlow()

    private val _orderBookBids = MutableStateFlow<List<OrderBookLevel>>(emptyList())
    val orderBookBids: StateFlow<List<OrderBookLevel>> = _orderBookBids.asStateFlow()

    private val _orderBookAsks = MutableStateFlow<List<OrderBookLevel>>(emptyList())
    val orderBookAsks: StateFlow<List<OrderBookLevel>> = _orderBookAsks.asStateFlow()

    private var currentSymbol: String = ""
    private var priceWebSocketJob: Job? = null
    private var orderBookWebSocketJob: Job? = null
    private var lastOrderBookUpdateId: Long = 0L

    /**
     * Loads all token data including price, chart, and metadata
     * Also starts WebSocket connections for real-time updates
     */
    fun loadTokenData(symbol: String, address: String) {
        currentSymbol = symbol

        // Update token address in UI state
        _uiState.update {
            it.copy(
                mint = if (address.length > 16) {
                    "${address.take(6)}...${address.takeLast(4)}"
                } else {
                    address
                }
            )
        }

        fetchTokenPrice()
        fetchChartData()
        loadOrderBookSnapshot()
        startWebSocketStreams()
    }

    /**
     * Loads initial order book snapshot from REST API
     */
    private fun loadOrderBookSnapshot() {
        if (currentSymbol.isEmpty()) return

        viewModelScope.launch {
            Log.d(TAG, "Loading order book snapshot for $currentSymbol")
            when (val result = fetchOrderBookSnapshotUseCase(currentSymbol, limit = 20)) {
                is OrderBookResult.Success -> {
                    val snapshot = result.snapshot
                    lastOrderBookUpdateId = snapshot.lastUpdateId
                    _orderBookBids.value = snapshot.bids
                    _orderBookAsks.value = snapshot.asks
                    Log.d(TAG, "Order book snapshot loaded: ${snapshot.bids.size} bids, ${snapshot.asks.size} asks, lastUpdateId=$lastOrderBookUpdateId")
                }
                is OrderBookResult.Failure -> {
                    Log.e(TAG, "Failed to load order book snapshot: ${result.error}")
                }
            }
        }
    }

    /**
     * Starts WebSocket streams for real-time price and order book updates
     */
    private fun startWebSocketStreams() {
        if (currentSymbol.isEmpty()) {
            Log.w(TAG, "Cannot start WebSocket streams: currentSymbol is empty")
            return
        }

        Log.d(TAG, "Starting WebSocket streams for $currentSymbol")

        // Cancel existing WebSocket connections
        priceWebSocketJob?.cancel()
        orderBookWebSocketJob?.cancel()

        // Subscribe to price updates
        priceWebSocketJob = viewModelScope.launch {
            Log.d(TAG, "Launching price WebSocket job for $currentSymbol")
            subscribeToPriceUpdatesUseCase(currentSymbol)
                .catch { e ->
                    Log.e(TAG, "Error in price WebSocket stream for $currentSymbol", e)
                }
                .collect { priceUpdate ->
                    Log.d(TAG, "Live Price Update Received: ${priceUpdate.symbol} = $${priceUpdate.lastPrice}")
                    Log.d(TAG, "Change: ${priceUpdate.priceChange} (${priceUpdate.priceChangePercent}%)")
                    Log.d(TAG, "Volume: ${priceUpdate.volume}, High: ${priceUpdate.highPrice}, Low: ${priceUpdate.lowPrice}")

                    _uiState.update {
                        it.copy(
                            currentPrice = formatPrice(priceUpdate.lastPrice),
                            priceChange24h = formatPriceChange(priceUpdate.priceChange),
                            change24h = priceUpdate.priceChangePercent,
                            volume24h = formatVolume(priceUpdate.volume)
                        )
                    }

                    // Update chart with new candle data periodically
                    updateChartWithLivePrice(priceUpdate.lastPrice, priceUpdate.timestamp)

                    Log.d(TAG, "UI State updated with new price")
                }
        }

        // Subscribe to order book updates
        orderBookWebSocketJob = viewModelScope.launch {
            Log.d(TAG, "Launching order book WebSocket job for $currentSymbol")
            subscribeToOrderBookUseCase(currentSymbol)
                .catch { e ->
                    Log.e(TAG, "Error in order book WebSocket stream for $currentSymbol", e)
                }
                .collect { orderBookUpdate ->
                    Log.d(TAG, "OrderBook Update Received: ${orderBookUpdate.symbol}")
                    Log.d(TAG, "Update IDs: first=${orderBookUpdate.firstUpdateId}, final=${orderBookUpdate.finalUpdateId}, last=$lastOrderBookUpdateId")
                    Log.d(TAG, "Bids: ${orderBookUpdate.bids.size}, Asks: ${orderBookUpdate.asks.size}")
                    if (orderBookUpdate.bids.isNotEmpty()) {
                        Log.d(TAG, "Best Bid: ${orderBookUpdate.bids.first().price}")
                    }
                    if (orderBookUpdate.asks.isNotEmpty()) {
                        Log.d(TAG, "Best Ask: ${orderBookUpdate.asks.first().price}")
                    }

                    applyOrderBookUpdate(orderBookUpdate)
                }
        }

        Log.d(TAG, "WebSocket streams started successfully for $currentSymbol")
    }

    /**
     * Applies order book update from WebSocket
     * Follows Binance's update ID validation rules
     */
    private fun applyOrderBookUpdate(update: com.rishitgoklani.aptosdex.domain.model.OrderBookUpdate) {
        // Validate update based on Binance rules:
        // 1. First event's firstUpdateId should be <= lastUpdateId+1
        // 2. Each event's firstUpdateId should equal previous finalUpdateId+1

        if (lastOrderBookUpdateId == 0L) {
            // No snapshot loaded yet, skip this update
            Log.w(TAG, "Skipping order book update: no snapshot loaded yet")
            return
        }

        // For first update after snapshot: firstUpdateId <= lastUpdateId+1 AND finalUpdateId >= lastUpdateId+1
        // For subsequent updates: firstUpdateId should equal previous finalUpdateId+1
        if (update.firstUpdateId > lastOrderBookUpdateId + 1) {
            Log.w(TAG, "Order book update gap detected. Expected firstUpdateId <= ${lastOrderBookUpdateId + 1}, got ${update.firstUpdateId}")
            // Re-fetch snapshot to resync
            loadOrderBookSnapshot()
            return
        }

        if (update.finalUpdateId <= lastOrderBookUpdateId) {
            Log.d(TAG, "Skipping old order book update: finalUpdateId=${update.finalUpdateId}, lastUpdateId=$lastOrderBookUpdateId")
            return
        }

        // Apply updates to bids and asks
        val updatedBids = mergePriceLevels(_orderBookBids.value, update.bids)
            .sortedByDescending { it.price } // Highest bid first
        val updatedAsks = mergePriceLevels(_orderBookAsks.value, update.asks)
            .sortedBy { it.price } // Lowest ask first

        _orderBookBids.value = updatedBids.take(20) // Keep top 20
        _orderBookAsks.value = updatedAsks.take(20) // Keep top 20
        lastOrderBookUpdateId = update.finalUpdateId

        Log.d(TAG, "Applied order book update: finalUpdateId=$lastOrderBookUpdateId, bids=${updatedBids.size}, asks=${updatedAsks.size}")
    }

    /**
     * Merges price level updates into existing order book
     * Removes levels with quantity 0, updates existing levels, adds new levels
     * Note: Sorting is done separately for bids (descending) and asks (ascending)
     */
    private fun mergePriceLevels(
        existing: List<OrderBookLevel>,
        updates: List<OrderBookLevel>
    ): List<OrderBookLevel> {
        val priceMap = existing.associateBy { it.price }.toMutableMap()

        updates.forEach { update ->
            if (update.quantity == 0.0) {
                // Remove this price level
                priceMap.remove(update.price)
            } else {
                // Update or add this price level
                priceMap[update.price] = update
            }
        }

        return priceMap.values.toList()
    }

    /**
     * Updates chart with live price data
     * Appends new candle or updates the latest candle
     */
    private fun updateChartWithLivePrice(price: Double, timestamp: Long) {
        val currentCandles = _chartData.value.toMutableList()
        if (currentCandles.isEmpty()) return

        val lastCandle = currentCandles.last()
        val timestampSeconds = timestamp / 1000

        // Determine candle interval based on selected time period
        val intervalSeconds = when (_selectedTimePeriod.value) {
            TimePeriod.HOURS_24 -> 3600L  // 1 hour
            TimePeriod.DAYS_7 -> 14400L    // 4 hours
            TimePeriod.MONTH_1 -> 86400L   // 1 day
            TimePeriod.YTD -> 86400L       // 1 day
        }

        // Check if we should update last candle or create new one
        val shouldCreateNewCandle = timestampSeconds - lastCandle.time >= intervalSeconds

        if (shouldCreateNewCandle) {
            // Create new candle
            val newCandle = CandlestickData(
                time = timestampSeconds,
                open = price.toFloat(),
                high = price.toFloat(),
                low = price.toFloat(),
                close = price.toFloat()
            )
            currentCandles.add(newCandle)

            // Keep only the last N candles based on period limit
            val limit = when (_selectedTimePeriod.value) {
                TimePeriod.HOURS_24 -> 24
                TimePeriod.DAYS_7 -> 42
                TimePeriod.MONTH_1 -> 30
                TimePeriod.YTD -> 365
            }

            if (currentCandles.size > limit) {
                currentCandles.removeAt(0)
            }

            Log.d(TAG, "Created new candle at $timestampSeconds with price $price")
        } else {
            // Update the last candle's close, high, low
            val updatedCandle = lastCandle.copy(
                close = price.toFloat(),
                high = maxOf(lastCandle.high, price.toFloat()),
                low = minOf(lastCandle.low, price.toFloat())
            )
            currentCandles[currentCandles.lastIndex] = updatedCandle
            Log.d(TAG, "Updated last candle: close=$price, high=${updatedCandle.high}, low=${updatedCandle.low}")
        }

        _chartData.value = currentCandles
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, closing WebSocket connections for $currentSymbol")
        // Cancel WebSocket connections when ViewModel is cleared
        priceWebSocketJob?.cancel()
        orderBookWebSocketJob?.cancel()
        Log.d(TAG, "WebSocket connections closed")
    }

    /**
     * Fetches current token price and updates UI state
     */
    private fun fetchTokenPrice() {
        if (currentSymbol.isEmpty()) return

        viewModelScope.launch {
            when (val result = fetchTokenPricesUseCase(listOf(currentSymbol))) {
                is TokenPriceResult.Success -> {
                    val tokenPrice = result.prices.firstOrNull()
                    if (tokenPrice != null) {
                        _uiState.update {
                            it.copy(
                                currentPrice = formatPrice(tokenPrice.currentPrice),
                                priceChange24h = formatPriceChange(tokenPrice.priceChange24h),
                                change24h = tokenPrice.priceChangePercent24h,
                                volume24h = formatVolume(tokenPrice.volume24h)
                            )
                        }
                        Log.d(TAG, "Updated price for $currentSymbol: ${tokenPrice.currentPrice}")
                    }
                }
                is TokenPriceResult.Failure -> {
                    Log.e(TAG, "Failed to fetch token price: ${result.error}")
                }
            }
        }
    }

    /**
     * Formats price to USD currency format
     */
    private fun formatPrice(price: Double): String {
        return when {
            price >= 1000 -> NumberFormat.getCurrencyInstance(Locale.US).format(price)
            price >= 1 -> String.format(Locale.US, "$%.2f", price)
            price >= 0.01 -> String.format(Locale.US, "$%.3f", price)
            else -> String.format(Locale.US, "$%.6f", price)
        }
    }

    /**
     * Formats price change with + or - sign
     */
    private fun formatPriceChange(change: Double): String {
        return when {
            change >= 0 -> "+${formatPrice(change).removePrefix("$")}"
            else -> formatPrice(change).removePrefix("$")
        }
    }

    /**
     * Formats volume to abbreviated format (K, M, B)
     */
    private fun formatVolume(volume: Double): String {
        return when {
            volume >= 1_000_000_000 -> String.format(Locale.US, "$%.2fB", volume / 1_000_000_000)
            volume >= 1_000_000 -> String.format(Locale.US, "$%.2fM", volume / 1_000_000)
            volume >= 1_000 -> String.format(Locale.US, "$%.2fK", volume / 1_000)
            else -> NumberFormat.getCurrencyInstance(Locale.US).format(volume)
        }
    }

    /**
     * Changes the selected time period and refetches chart data
     */
    fun onTimePeriodChanged(timePeriod: TimePeriod) {
        Log.d(TAG, "Time period changed to: ${timePeriod.label}")
        _selectedTimePeriod.value = timePeriod
        fetchChartData()
    }

    /**
     * Fetches chart data from Binance API
     */
    private fun fetchChartData() {
        if (currentSymbol.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Map UI TimePeriod to domain ChartTimePeriod
            val chartTimePeriod = when (_selectedTimePeriod.value) {
                TimePeriod.HOURS_24 -> ChartTimePeriod.HOURS_24
                TimePeriod.DAYS_7 -> ChartTimePeriod.DAYS_7
                TimePeriod.MONTH_1 -> ChartTimePeriod.MONTH_1
                TimePeriod.YTD -> ChartTimePeriod.YTD
            }

            Log.d(TAG, "Fetching chart data for $currentSymbol with period ${chartTimePeriod.name}")

            when (val result = fetchChartDataUseCase(currentSymbol, chartTimePeriod)) {
                is ChartDataResult.Success -> {
                    Log.d(TAG, "Successfully fetched ${result.data.candles.size} candles")

                    // Convert domain models to UI models
                    val candlesticks = result.data.candles.map { candle ->
                        CandlestickData(
                            time = candle.timestamp,
                            open = candle.open.toFloat(),
                            high = candle.high.toFloat(),
                            low = candle.low.toFloat(),
                            close = candle.close.toFloat()
                        )
                    }

                    _chartData.value = candlesticks
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

                is ChartDataResult.Failure -> {
                    Log.e(TAG, "Failed to fetch chart data: ${result.error}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load chart data"
                        )
                    }
                }
            }
        }
    }

    /**
     * Converts domain OrderBookLevel to UI OrderBookEntry
     * Formats price and amount for display
     */
    fun getOrderBookData(): OrderBookData {
        val bids = _orderBookBids.value.map { level ->
            val total = level.price * level.quantity
            OrderBookEntry(
                price = formatOrderBookPrice(level.price),
                amount = formatOrderBookAmount(level.quantity),
                total = formatOrderBookAmount(total)
            )
        }

        val asks = _orderBookAsks.value.map { level ->
            val total = level.price * level.quantity
            OrderBookEntry(
                price = formatOrderBookPrice(level.price),
                amount = formatOrderBookAmount(level.quantity),
                total = formatOrderBookAmount(total)
            )
        }

        return OrderBookData(bids = bids, asks = asks)
    }

    /**
     * Formats price for order book display
     */
    private fun formatOrderBookPrice(price: Double): String {
        return when {
            price >= 1000 -> String.format(Locale.US, "%.2f", price)
            price >= 1 -> String.format(Locale.US, "%.3f", price)
            price >= 0.01 -> String.format(Locale.US, "%.4f", price)
            else -> String.format(Locale.US, "%.6f", price)
        }
    }

    /**
     * Formats amount for order book display
     */
    private fun formatOrderBookAmount(amount: Double): String {
        return when {
            amount >= 1000 -> String.format(Locale.US, "%.2f", amount)
            amount >= 1 -> String.format(Locale.US, "%.3f", amount)
            else -> String.format(Locale.US, "%.4f", amount)
        }
    }
}
