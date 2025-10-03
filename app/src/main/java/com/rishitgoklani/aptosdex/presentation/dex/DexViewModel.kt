package com.rishitgoklani.aptosdex.presentation.dex

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.rishitgoklani.aptosdex.domain.model.TokenPriceResult
import com.rishitgoklani.aptosdex.domain.usecase.FetchTokenPricesUseCase
import com.rishitgoklani.aptosdex.presentation.tokens.TokenWhitelist
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DexViewModel @Inject constructor(
    private val fetchTokenPricesUseCase: FetchTokenPricesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "DexViewModel"
    }

    private val _uiState = MutableStateFlow(DexUiState())
    val uiState: StateFlow<DexUiState> = _uiState.asStateFlow()

    init {
        loadTokenPrices()
    }

    /**
     * Fetches real-time token prices from Binance API
     */
    private fun loadTokenPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val symbols = TokenWhitelist.allowed.map { it.symbol }
            Log.d(TAG, "Fetching prices for symbols: $symbols")

            when (val result = fetchTokenPricesUseCase(symbols)) {
                is TokenPriceResult.Success -> {
                    Log.d(TAG, "Successfully fetched ${result.prices.size} token prices")

                    // Map domain models to UI models
                    val priceMap = result.prices.associateBy { it.symbol }

                    val topTokens = TokenWhitelist.allowed.map { token ->
                        // Special handling for stablecoins
                        if (token.symbol.equals("USDt", ignoreCase = true) ||
                            token.symbol.equals("USDT", ignoreCase = true)) {
                            DexTokenUi(
                                name = token.name,
                                symbol = token.symbol,
                                imageUrl = token.imageUrl,
                                address = token.address,
                                priceUsd = "$1.00",
                                changePct24h = 0.0,
                                volumeUsd = "-"
                            )
                        } else {
                            val price = priceMap[token.symbol]
                            if (price != null) {
                                DexTokenUi(
                                    name = token.name,
                                    symbol = token.symbol,
                                    imageUrl = token.imageUrl,
                                    address = token.address,
                                    priceUsd = formatPrice(price.currentPrice),
                                    changePct24h = price.priceChangePercent24h,
                                    volumeUsd = formatVolume(price.volume24h)
                                )
                            } else {
                                Log.w(TAG, "No price data for ${token.symbol}")
                                DexTokenUi(
                                    name = token.name,
                                    symbol = token.symbol,
                                    imageUrl = token.imageUrl,
                                    address = token.address,
                                    priceUsd = "-",
                                    changePct24h = 0.0,
                                    volumeUsd = "-"
                                )
                            }
                        }
                    }

                    // Favorites: only APT
                    val favorites = topTokens.filter { it.symbol == "APT" }

                    _uiState.update {
                        it.copy(
                            favorites = favorites,
                            topTokens = topTokens,
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is TokenPriceResult.Failure -> {
                    Log.e(TAG, "Failed to fetch token prices: ${result.error}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to fetch prices: ${result.error}"
                        )
                    }
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

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        viewModelScope.launch {
            // TODO: implement search functionality
        }
    }

    /**
     * Refreshes token prices
     */
    fun refresh() {
        loadTokenPrices()
    }
}


