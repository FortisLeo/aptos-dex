package com.rishitgoklani.aptosdex.presentation.swap

/**
 * Static mapping of token symbols to Pyth price feed ids for Hermes.
 * Values provided by product requirements.
 */
object PythFeeds {
    private val symbolToId: Map<String, String> = mapOf(
        // Stablecoins / majors used in app
        "USDT" to "2b89b9dc8fdf9f34709a5b106b472f0f39bb6ca9ce04b0fd7f2e971688e2e53b",
        "USDC" to "eaa020c61cc479712813461ce153894a96a6c00b21ed0cfc2798d1f9a9e9c94a",
        // Aptos native
        "APT" to "03ae4db29ed4ae33d323568895aa00337e658e348b37509f5372ae51f0af00d5",
        // Pancake token
        "CAKE" to "2356af9529a1064d41e32d617e2ce1dca5733afa901daba9e2b68dee5d53ecf9",
        // Wrapped BTC
        "WBTC" to "c9d8b075a5c69303365ae23633d4e085199bf5c520a3b90fed1322a0342ffc33"
    )

    fun idFor(symbol: String): String? = symbolToId[symbol.uppercase()]
}


