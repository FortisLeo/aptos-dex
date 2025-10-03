package com.rishitgoklani.aptosdex.domain.model

data class ConnectionKeys(
    val secretKey: ByteArray,
    val publicKey: ByteArray,
    val sharedKey: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionKeys

        if (!secretKey.contentEquals(other.secretKey)) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (sharedKey != null) {
            if (other.sharedKey == null) return false
            if (!sharedKey.contentEquals(other.sharedKey)) return false
        } else if (other.sharedKey != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = secretKey.contentHashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + (sharedKey?.contentHashCode() ?: 0)
        return result
    }
}