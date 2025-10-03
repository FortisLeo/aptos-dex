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

    // Smart contract addresses
    const val CLOB_CONTRACT_ADDRESS = "0xaa4efc5f6235612f916dbb2ba356876e740505113ac0062da07e64e41618422f"
    const val CLOB_MODULE_NAME = "CLOB"

    // Deployment transaction info (Latest deployment)
    const val DEPLOYMENT_TX_HASH = "0x3a0eed55613c002748f4c7e01265be627af4f707a1a575234733ff899a97eb4c"
    const val DEPLOYMENT_VERSION = 6893375986L
    const val DEPLOYMENT_SEQUENCE = 0L

    // View function names
    const val VIEW_FUNCTION_GET_BOOK_DEPTH = "get_book_depth"
    const val VIEW_FUNCTION_GET_BEST_BID = "get_best_bid"
    const val VIEW_FUNCTION_GET_BEST_ASK = "get_best_ask"
    const val VIEW_FUNCTION_GET_SPREAD = "get_spread"

    // Entry function names for trading
    const val ENTRY_FUNCTION_PLACE_BUY_ORDER = "place_buy_order"
    const val ENTRY_FUNCTION_PLACE_SELL_ORDER = "place_sell_order"

    // Event types
    const val TRADE_EVENT_TYPE = "$CLOB_CONTRACT_ADDRESS::$CLOB_MODULE_NAME::TradeEvent"

    // Order book address (where the OrderBook resource is stored)
    // For deployed contracts, the OrderBook resource is typically stored at the contract address
    const val ORDER_BOOK_ADDRESS = CLOB_CONTRACT_ADDRESS

    // Configuration
    const val MAX_RETRIES = 3
    const val REQUEST_TIMEOUT_MS = 10000L
}

/**
 * Coin type addresses for Aptos tokens (Testnet)
 * Using test coins deployed at the CLOB contract address
 */
object CoinTypes {
    // Test coins deployed at CLOB contract address (format: address::module)
    // Aptos type tag format requires address::module::struct
    const val APT = "0x1::aptos_coin::AptosCoin"
    const val USDC = "0xaa4efc5f6235612f916dbb2ba356876e740505113ac0062da07e64e41618422f::TestUSDC::TestUSDC"
    const val BTC = "0xaa4efc5f6235612f916dbb2ba356876e740505113ac0062da07e64e41618422f::TestBTC::TestBTC"
    const val ETH = "0xaa4efc5f6235612f916dbb2ba356876e740505113ac0062da07e64e41618422f::TestETH::TestETH"
    const val USDT = "0xaa4efc5f6235612f916dbb2ba356876e740505113ac0062da07e64e41618422f::TestUSDT::TestUSDT"
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
