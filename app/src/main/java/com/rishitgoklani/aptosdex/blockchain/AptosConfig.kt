package com.rishitgoklani.aptosdex.blockchain

/**
 * Aptos network configuration
 */
object AptosConfig {
    // Network endpoints
    const val MAINNET_URL = "https://fullnode.mainnet.aptoslabs.com/v1"
    const val TESTNET_URL = "https://fullnode.testnet.aptoslabs.com/v1"
    const val DEVNET_URL = "https://fullnode.devnet.aptoslabs.com/v1"

    // Indexer endpoints for event streaming
    const val MAINNET_INDEXER_URL = "https://indexer.mainnet.aptoslabs.com/v1/graphql"
    const val TESTNET_INDEXER_URL = "https://indexer.testnet.aptoslabs.com/v1/graphql"
    const val DEVNET_INDEXER_URL = "https://indexer.devnet.aptoslabs.com/v1/graphql"

    // Default network (configured for testnet)
    const val DEFAULT_NETWORK_URL = TESTNET_URL
    const val DEFAULT_INDEXER_URL = TESTNET_INDEXER_URL

    // DEX Smart Contract Address (from INTEGRATION_GUIDE.md)
    const val DEX_CONTRACT_ADDRESS = "0xd4f4a886d54d280f06e3beebde86c7ff27a824dffb1a410dda625635cd16af5e"

    // Module names
    const val DEX_MODULE = "DEX"
    const val ORDER_BOOK_MODULE = "OrderBook"
    const val VAULT_MODULE = "Vault"
    const val TOKEN_REGISTRY_MODULE = "TokenRegistry"

    // View function names for OrderBook
    const val VIEW_GET_USER_BALANCES = "get_user_balances"
    const val VIEW_GET_DEX_STATS = "get_dex_stats"
    const val VIEW_GET_MARKET_PRICE = "get_market_price"
    const val VIEW_GET_SPREAD = "get_spread"

    // Entry function names
    const val ENTRY_DEPOSIT = "deposit"
    const val ENTRY_WITHDRAW = "withdraw"
    const val ENTRY_PLACE_LIMIT_ORDER = "place_limit_order"
    const val ENTRY_PLACE_MARKET_ORDER = "place_market_order"
    const val ENTRY_CANCEL_ORDER = "cancel_order"

    // Event types
    const val TRADE_EVENT_TYPE = "$DEX_CONTRACT_ADDRESS::$DEX_MODULE::TradeEvent"
    const val ORDER_EVENT_TYPE = "$DEX_CONTRACT_ADDRESS::$DEX_MODULE::OrderEvent"
    const val DEPOSIT_EVENT_TYPE = "$DEX_CONTRACT_ADDRESS::$DEX_MODULE::DepositEvent"
    const val WITHDRAWAL_EVENT_TYPE = "$DEX_CONTRACT_ADDRESS::$DEX_MODULE::WithdrawalEvent"

    // Configuration
    const val MAX_RETRIES = 3
    const val REQUEST_TIMEOUT_MS = 10000L
}

/**
 * Coin type addresses for Aptos tokens (Testnet)
 * Using standard Aptos coin types
 */
object CoinTypes {
    // Aptos type tag format requires address::module::struct
    const val APT = "0x1::aptos_coin::AptosCoin"
    // These would be the actual token types on testnet - update as needed
    const val USDC = "0x1::test_coin::USDC"  // Placeholder - replace with actual testnet USDC
    const val BTC = "0x1::test_coin::BTC"    // Placeholder
    const val ETH = "0x1::test_coin::ETH"    // Placeholder
    const val USDT = "0x1::test_coin::USDT"  // Placeholder
}

/**
 * Market pair configuration with type arguments
 */
object MarketPairs {
    // Map token symbols to market pair identifiers in the smart contract
    val SYMBOL_TO_MARKET_PAIR = mapOf(
        "APT" to "APT_USDC",
        "BTC" to "BTC_USDC",
        "ETH" to "ETH_USDC",
        "USDC" to "USDC_USDT",
        "USDT" to "USDT_USDC"
    )

    // Map token symbols to their type arguments <Base, Quote>
    private val SYMBOL_TO_TYPE_ARGS = mapOf(
        "APT" to Pair(CoinTypes.APT, CoinTypes.USDC),
        "BTC" to Pair(CoinTypes.BTC, CoinTypes.USDC),
        "ETH" to Pair(CoinTypes.ETH, CoinTypes.USDC),
        "USDC" to Pair(CoinTypes.USDC, CoinTypes.USDT),
        "USDT" to Pair(CoinTypes.USDT, CoinTypes.USDC)
    )

    fun getMarketPair(symbol: String): String {
        return SYMBOL_TO_MARKET_PAIR[symbol.uppercase()] ?: "${symbol.uppercase()}_USDC"
    }

    /**
     * Get type arguments for a token symbol
     * @return Pair of <Base, Quote> type strings
     */
    fun getTypeArguments(symbol: String): Pair<String, String> {
        return SYMBOL_TO_TYPE_ARGS[symbol.uppercase()]
            ?: Pair(CoinTypes.APT, CoinTypes.USDC)
    }
}
