package com.rishitgoklani.aptosdex.presentation.tokendetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rishitgoklani.aptosdex.domain.model.ChartDataResult
import com.rishitgoklani.aptosdex.domain.model.ChartTimePeriod
import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult
import com.rishitgoklani.aptosdex.domain.model.OrderBookLevel
import com.rishitgoklani.aptosdex.domain.model.AptosOrderBookResult
import com.rishitgoklani.aptosdex.domain.usecase.FetchChartDataUseCase
import com.rishitgoklani.aptosdex.domain.usecase.FetchAptosOrderBookUseCase
import com.rishitgoklani.aptosdex.domain.usecase.StreamAptosTradeEventsUseCase
import com.rishitgoklani.aptosdex.domain.usecase.FetchTokenPricesUseCase
import com.rishitgoklani.aptosdex.domain.usecase.SubscribeToPriceUpdatesUseCase
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
    private val fetchAptosOrderBookUseCase: FetchAptosOrderBookUseCase,
    private val streamAptosTradeEventsUseCase: StreamAptosTradeEventsUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase
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
    private var aptosTradeEventsJob: Job? = null
    private var localOrderBook = mutableMapOf<Double, OrderBookLevel>() // Price -> Level mapping

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
     * Loads initial order book snapshot from Aptos smart contract
     * Calls get_book_depth view function
     */
    private fun loadOrderBookSnapshot() {
        if (currentSymbol.isEmpty()) return

        viewModelScope.launch {
            Log.d(TAG, "Loading Aptos order book snapshot for $currentSymbol")
            when (val result = fetchAptosOrderBookUseCase(currentSymbol)) {
                is AptosOrderBookResult.Success -> {
                    val snapshot = result.snapshot

                    // Convert Aptos PriceLevels to OrderBookLevel format for UI
                    // Aggregate all orders at each price level
                    val bids = snapshot.buyOrders.map { priceLevel ->
                        OrderBookLevel(
                            price = priceLevel.price,
                            quantity = priceLevel.totalSize
                        )
                    }
                    val asks = snapshot.sellOrders.map { priceLevel ->
                        OrderBookLevel(
                            price = priceLevel.price,
                            quantity = priceLevel.totalSize
                        )
                    }

                    _orderBookBids.value = bids
                    _orderBookAsks.value = asks

                    // Initialize local order book for event updates
                    localOrderBook.clear()
                    bids.forEach { localOrderBook[it.price] = it }
                    asks.forEach { localOrderBook[it.price] = it }

                    Log.d(TAG, "Aptos order book snapshot loaded: ${snapshot.buyOrders.size} buy levels, ${snapshot.sellOrders.size} sell levels")
                }
                is AptosOrderBookResult.Failure -> {
                    Log.e(TAG, "Failed to load Aptos order book snapshot: ${result.error}")
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

        // Cancel existing connections
        priceWebSocketJob?.cancel()
        aptosTradeEventsJob?.cancel()

        // Subscribe to price updates (from Binance for now)
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

        // Subscribe to Aptos trade events for order book updates
        aptosTradeEventsJob = viewModelScope.launch {
            Log.d(TAG, "Starting Aptos trade event stream for $currentSymbol")
            streamAptosTradeEventsUseCase(currentSymbol)
                .catch { e ->
                    Log.e(TAG, "Error in Aptos trade event stream for $currentSymbol", e)
                }
                .collect { tradeEvent ->
                    Log.d(TAG, "Aptos TradeEvent Received: seq=${tradeEvent.eventSequenceNumber}")
                    Log.d(TAG, "MakerOrderId: ${tradeEvent.makerOrderId}, TakerOrderId: ${tradeEvent.takerOrderId}")
                    Log.d(TAG, "Price: ${tradeEvent.price}, Size: ${tradeEvent.size}, Timestamp: ${tradeEvent.timestamp}")

                    // Update local order book based on the trade
                    applyTradeEventToOrderBook(tradeEvent)
                }
        }

        Log.d(TAG, "WebSocket streams started successfully for $currentSymbol")
    }

    /**
     * Apply trade event to local order book
     * Updates the order book when a trade is executed on-chain
     */
    private fun applyTradeEventToOrderBook(tradeEvent: com.rishitgoklani.aptosdex.domain.model.AptosTradeEvent) {
        // When a trade happens, it consumes liquidity at a specific price level
        // TradeEvent contains: makerOrderId, takerOrderId, price, size, timestamp

        val priceLevel = localOrderBook[tradeEvent.price]

        if (priceLevel != null) {
            // Update the quantity at this price level
            val newQuantity = (priceLevel.quantity - tradeEvent.size).coerceAtLeast(0.0)

            if (newQuantity > 0) {
                // Update the level
                localOrderBook[tradeEvent.price] = OrderBookLevel(
                    price = tradeEvent.price,
                    quantity = newQuantity
                )
            } else {
                // Remove the level if fully consumed
                localOrderBook.remove(tradeEvent.price)
            }
        }

        // Rebuild bids and asks from local order book
        val allLevels = localOrderBook.values.toList()
        val bids = allLevels.filter { it.price < (tradeEvent.price * 1.01) } // Approximate bid side
            .sortedByDescending { it.price }
            .take(20)
        val asks = allLevels.filter { it.price >= (tradeEvent.price * 1.01) } // Approximate ask side
            .sortedBy { it.price }
            .take(20)

        _orderBookBids.value = bids
        _orderBookAsks.value = asks

        Log.d(TAG, "Applied trade event to order book: ${bids.size} bids, ${asks.size} asks")
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
        Log.d(TAG, "ViewModel cleared, closing connections for $currentSymbol")
        // Cancel WebSocket and Aptos event stream connections when ViewModel is cleared
        priceWebSocketJob?.cancel()
        aptosTradeEventsJob?.cancel()
        Log.d(TAG, "All connections closed")
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
