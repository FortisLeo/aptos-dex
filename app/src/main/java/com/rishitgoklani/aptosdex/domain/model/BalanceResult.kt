package com.rishitgoklani.aptosdex.domain.model

sealed class BalanceResult {
    data class Success(val balance: String) : BalanceResult()
    data class Failure(val error: BalanceError) : BalanceResult()
}

enum class BalanceError {
    ACCOUNT_NOT_FOUND,
    NETWORK_ERROR,
    PARSE_ERROR,
    UNKNOWN
}