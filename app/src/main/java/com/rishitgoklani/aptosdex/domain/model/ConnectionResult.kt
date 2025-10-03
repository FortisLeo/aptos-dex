package com.rishitgoklani.aptosdex.domain.model

sealed class ConnectionResult {
    data class Success(val address: String, val sharedKey: ByteArray) : ConnectionResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (address != other.address) return false
            if (!sharedKey.contentEquals(other.sharedKey)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = address.hashCode()
            result = 31 * result + sharedKey.contentHashCode()
            return result
        }
    }
    data class Failure(val error: ConnectionError) : ConnectionResult()
    object Rejected : ConnectionResult()
}

enum class ConnectionError {
    MISSING_DATA,
    INVALID_KEY,
    CRYPTO_ERROR,
    UNKNOWN
}