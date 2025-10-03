package com.rishitgoklani.aptosdex.presentation.tokens

data class TokenSelectUiState(
    val query: String = "",
    val tokens: List<TokenUi> = emptyList(),
    val filtered: List<TokenUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

