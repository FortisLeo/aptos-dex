package com.rishitgoklani.aptosdex.presentation.tokens

data class TokenUi(
    val name: String,
    val symbol: String,
    val address: String,
    val imageUrl: String? = null,
    val iconRes: Int
)


