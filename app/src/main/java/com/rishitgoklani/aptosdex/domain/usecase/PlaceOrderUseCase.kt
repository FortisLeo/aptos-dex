package com.rishitgoklani.aptosdex.domain.usecase

import android.util.Log
import com.rishitgoklani.aptosdex.blockchain.AptosClientWrapper
import com.rishitgoklani.aptosdex.blockchain.AptosConfig
import com.rishitgoklani.aptosdex.blockchain.MarketPairs
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Result of placing an order
 */
sealed class PlaceOrderResult {
    data class Success(val payloadJson: String) : PlaceOrderResult()
    data class Failure(val error: String) : PlaceOrderResult()
    data object WalletNotConnected : PlaceOrderResult()
}

/**
 * Use case for placing buy or sell orders on the CLOB
 */
class PlaceOrderUseCase @Inject constructor(
    private val aptosClient: AptosClientWrapper,
    private val walletRepository: WalletRepository
) {
    companion object {
        private const val TAG = "PlaceOrderUseCase"
    }

    /**
     * Place an order (buy or sell)
     * @param symbol Token symbol (e.g., "APT")
     * @param isBuy true for buy order, false for sell order
     * @param price Price in USDC
     * @param size Size/amount of token
     */
    suspend operator fun invoke(
        symbol: String,
        isBuy: Boolean,
        price: Double,
        size: Double
    ): PlaceOrderResult {
        return try {
            Log.d(TAG, "Placing ${if (isBuy) "BUY" else "SELL"} order: $symbol, price=$price, size=$size")

            val senderAddress = walletRepository.getConnectedWalletAddress()
            if (senderAddress.isNullOrEmpty()) {
                Log.w(TAG, "No connected wallet found while placing order")
                return PlaceOrderResult.WalletNotConnected
            }

            // Get type arguments for the market pair
            val typeArgs = MarketPairs.getTypeArguments(symbol)

            // Convert price and size to microunits (u64 in contract expects microunits)
            // Assuming 6 decimal places for both price and size
            val priceInMicrounits = (price * 1_000_000).toLong()
            val sizeInMicrounits = (size * 1_000_000).toLong()

            Log.d(TAG, "Type args: Base=${typeArgs.first}, Quote=${typeArgs.second}")
            Log.d(TAG, "Price in microunits: $priceInMicrounits, Size in microunits: $sizeInMicrounits")

            // Build transaction payload for placing order (wallet will sign & submit)
            val txResult = aptosClient.buildPlaceOrderTransaction(
                senderAddress = senderAddress,
                marketPair = MarketPairs.getMarketPair(symbol),
                typeArguments = listOf(typeArgs.first, typeArgs.second),
                price = priceInMicrounits.toDouble(),
                quantity = sizeInMicrounits.toDouble(),
                isBuy = isBuy
            )

            txResult.fold(
                onSuccess = { payloadJson ->
                    Log.d(TAG, "Transaction payload built for signing")
                    PlaceOrderResult.Success(payloadJson)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to place order: ${error.message}")
                    PlaceOrderResult.Failure(error.message ?: "Unknown error")
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error placing order", e)
            PlaceOrderResult.Failure(e.message ?: "Failed to place order")
        }
    }
}
