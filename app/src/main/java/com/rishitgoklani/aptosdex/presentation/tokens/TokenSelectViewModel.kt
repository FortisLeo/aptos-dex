package com.rishitgoklani.aptosdex.presentation.tokens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class TokenSelectViewModel @Inject constructor(
    private val tokenRepository: com.rishitgoklani.aptosdex.data.repository.TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TokenSelectUiState())
    val uiState: StateFlow<TokenSelectUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init { seedWhitelist() }

    private fun seedWhitelist() {
        val list = TokenWhitelist.allowed
        _uiState.update { it.copy(isLoading = false, tokens = list, filtered = list, errorMessage = null) }
    }

    private fun seedFallback(error: String? = null) {
        val sample = listOf(
            TokenUi(name = "Aptos", symbol = "APT", address = "0x1::aptos_coin::AptosCoin", imageUrl = null, iconRes = com.rishitgoklani.aptosdex.R.drawable.ic_trending_up_24),
            TokenUi(name = "USD Coin", symbol = "USDC", address = "0x3::usdc::USDC", imageUrl = null, iconRes = com.rishitgoklani.aptosdex.R.drawable.ic_swap_vert_24)
        )
        _uiState.update { it.copy(isLoading = false, tokens = sample, filtered = sample, errorMessage = error) }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.Default) {
            delay(300)
            val query = newQuery.trim()
            val base = TokenWhitelist.allowed
            val results = if (query.isBlank()) base else base.filter { t ->
                t.name.contains(query, ignoreCase = true) ||
                t.symbol.contains(query, ignoreCase = true)
            }
            _uiState.update { current -> current.copy(filtered = results) }
        }
    }
}


