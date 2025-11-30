package com.rishitgoklani.aptosdex.domain.repository

import com.rishitgoklani.aptosdex.domain.model.*

/**
 * Repository interface for DEX smart contract operations
 * Based on INTEGRATION_GUIDE.md specifications
 */
interface DexRepository {

    /**
     * Deposit tokens into DEX vault
     * @param tokenType Token type string (e.g., "0x1::aptos_coin::AptosCoin")
     * @param amount Amount to deposit (in smallest units)
     * @param symbol Token symbol (e.g., "APT")
     * @return Transaction result
     */
    suspend fun deposit(
        tokenType: String,
        amount: Long,
        symbol: String
    ): DexResult<TransactionResult>

    /**
     * Withdraw tokens from DEX vault
     * @param tokenType Token type string
     * @param amount Amount to withdraw (in smallest units)
     * @param symbol Token symbol
     * @return Transaction result
     */
    suspend fun withdraw(
        tokenType: String,
        amount: Long,
        symbol: String
    ): DexResult<TransactionResult>

    /**
     * Place a limit order
     * @param baseToken Base token type
     * @param quoteToken Quote token type
     * @param marketId Market pair ID
     * @param side Order side (false=buy, true=sell)
     * @param price Order price
     * @param size Order quantity
     * @return Order placement result with order ID
     */
    suspend fun placeLimitOrder(
        baseToken: String,
        quoteToken: String,
        marketId: Long,
        side: Boolean,
        price: Long,
        size: Long
    ): DexResult<OrderPlacementResult>

    /**
     * Place a market order
     * @param baseToken Base token type
     * @param quoteToken Quote token type
     * @param marketId Market pair ID
     * @param side Order side (false=buy, true=sell)
     * @param size Order quantity
     * @return List of matched order IDs
     */
    suspend fun placeMarketOrder(
        baseToken: String,
        quoteToken: String,
        marketId: Long,
        side: Boolean,
        size: Long
    ): DexResult<List<Long>>

    /**
     * Cancel an existing order
     * @param baseToken Base token type
     * @param quoteToken Quote token type
     * @param orderId Order ID to cancel
     * @return Remaining order size
     */
    suspend fun cancelOrder(
        baseToken: String,
        quoteToken: String,
        orderId: Long
    ): DexResult<Long>

    /**
     * Get user token balances in DEX vault
     * @param tokenType Token type to query
     * @param userAddress User wallet address
     * @return User balance (available and locked)
     */
    suspend fun getUserBalances(
        tokenType: String,
        userAddress: String
    ): DexResult<DexBalance>

    /**
     * Get DEX statistics
     * @return DEX statistics (trades, volume, fees, users)
     */
    suspend fun getDexStats(): DexResult<DexStats>

    /**
     * Get current market price for a trading pair
     * @param baseToken Base token type
     * @param quoteToken Quote token type
     * @return Current market price
     */
    suspend fun getMarketPrice(
        baseToken: String,
        quoteToken: String
    ): DexResult<MarketPrice>

    /**
     * Get bid-ask spread for a trading pair
     * @param baseToken Base token type
     * @param quoteToken Quote token type
     * @return Spread information
     */
    suspend fun getSpread(
        baseToken: String,
        quoteToken: String
    ): DexResult<Spread>
}
