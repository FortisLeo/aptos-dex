package com.rishitgoklani.aptosdex.blockchain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.mcxross.kaptos.Aptos
import xyz.mcxross.kaptos.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around Kaptos SDK for Aptos blockchain interactions
 * Provides simplified API for order book operations
 */
@Singleton
class AptosClientWrapper @Inject constructor() {

    companion object {
        private const val TAG = "AptosClientWrapper"
    }

    // Initialize Aptos client configured for devnet
    private val aptos: Aptos by lazy {
        try {
            // Use default Aptos client which connects to devnet
            // The SDK will use the default devnet endpoint
            Aptos(AptosConfig(settings = AptosSettings(network = Network.TESTNET)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Aptos client", e)
            throw e
        }
    }

    /**
     * Call a view function on the Aptos smart contract via REST API
     * View functions are read-only and don't require gas fees
     *
     * @param functionName The view function name (e.g., "get_book_depth")
     * @param typeArguments Generic type arguments for the function
     * @param arguments Function arguments
     * @return Result as a list of Any (needs to be parsed by caller)
     */
    suspend fun callViewFunction(
        functionName: String,
        typeArguments: List<String> = emptyList(),
        arguments: List<Any> = emptyList()
    ): Result<List<Any>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calling view function: $functionName")
            Log.d(TAG, "Contract: ${AptosConfig.DEX_CONTRACT_ADDRESS}::${AptosConfig.DEX_MODULE}")
            Log.d(TAG, "Type arguments: $typeArguments")
            Log.d(TAG, "Arguments: $arguments")

            // Build the function identifier using DEX contract
            val functionId = "${AptosConfig.DEX_CONTRACT_ADDRESS}::${AptosConfig.DEX_MODULE}::$functionName"

            // Construct the payload
            val payload = mapOf(
                "function" to functionId,
                "type_arguments" to typeArguments,
                "arguments" to arguments
            )

            val gson = com.google.gson.Gson()
            val jsonPayload = gson.toJson(payload)

            Log.d(TAG, "View function payload: $jsonPayload")

            val url = "${AptosConfig.DEFAULT_NETWORK_URL}/view"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toRequestBody(mediaType)

            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okhttp3.OkHttpClient.Builder()
                .connectTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                Log.d(TAG, "View function response: $body")

                @Suppress("UNCHECKED_CAST")
                val result = gson.fromJson(body, List::class.java) as List<Any>
                Log.d(TAG, "View function call successful, returned ${result.size} items")
                Result.success(result)
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Log.e(TAG, "View function failed: ${response.code} ${response.message}")
                Log.e(TAG, "Error body: $errorBody")
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error calling view function: $functionName", e)
            Result.failure(e)
        }
    }

    /**
     * Get ledger information (current blockchain state)
     */
    suspend fun getLedgerInfo(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching ledger info")
            val ledgerInfo = aptos.getLedgerInfo()
            Log.d(TAG, "Ledger info retrieved successfully")
            Result.success(ledgerInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ledger info", e)
            Result.failure(e)
        }
    }

    /**
     * Get account resource to extract event handle
     * Used to get the OrderBook resource and its trade_events handle
     */
    suspend fun getAccountResource(
        address: String,
        resourceType: String
    ): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching resource: $resourceType for address: $address")

            val url = "${AptosConfig.DEFAULT_NETWORK_URL}/accounts/$address/resource/$resourceType"
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okhttp3.OkHttpClient.Builder()
                .connectTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                val gson = com.google.gson.Gson()
                @Suppress("UNCHECKED_CAST")
                val data = gson.fromJson(body, Map::class.java) as Map<String, Any>
                Log.d(TAG, "Resource fetched successfully")
                Result.success(data)
            } else {
                Log.e(TAG, "Failed to fetch resource: ${response.code} ${response.message}")
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching resource: $resourceType", e)
            Result.failure(e)
        }
    }

    /**
     * Get events by event handle
     * Fetches events from the Aptos REST API
     */
    suspend fun getEventsByHandle(
        address: String,
        eventHandleStruct: String,
        fieldName: String,
        start: Long? = null,
        limit: Int? = null
    ): Result<List<Map<String, Any>>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching events for handle: $fieldName at $address")
            Log.d(TAG, "Event handle struct: $eventHandleStruct")

            // URL encode the event handle struct to handle < > and :: characters
            val encodedStruct = java.net.URLEncoder.encode(eventHandleStruct, "UTF-8")

            var url = "${AptosConfig.DEFAULT_NETWORK_URL}/accounts/$address/events/$encodedStruct/$fieldName"
            val params = mutableListOf<String>()
            if (start != null) params.add("start=$start")
            if (limit != null) params.add("limit=$limit")
            if (params.isNotEmpty()) url += "?${params.joinToString("&")}"

            Log.d(TAG, "Full URL: $url")

            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()

            val response = okhttp3.OkHttpClient.Builder()
                .connectTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                val gson = com.google.gson.Gson()
                @Suppress("UNCHECKED_CAST")
                val data = gson.fromJson(body, List::class.java) as List<Map<String, Any>>
                Log.d(TAG, "Fetched ${data.size} events")
                Result.success(data)
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Log.e(TAG, "Failed to fetch events: ${response.code} ${response.message}")
                Log.e(TAG, "Error body: $errorBody")
                Log.e(TAG, "Request URL: $url")
                Result.failure(Exception("HTTP ${response.code}: ${response.message} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events", e)
            Result.failure(e)
        }
    }

    /**
     * Submit entry function transaction (for placing orders)
     */
    suspend fun submitEntryFunctionTransaction(
        senderAddress: String,
        functionId: String,
        typeArguments: List<String>,
        arguments: List<Any>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Submitting entry function transaction")
            Log.d(TAG, "Function: $functionId")
            Log.d(TAG, "Type arguments: $typeArguments")
            Log.d(TAG, "Arguments: $arguments")

            // Build transaction payload
            val payload = mapOf(
                "type" to "entry_function_payload",
                "function" to functionId,
                "type_arguments" to typeArguments,
                "arguments" to arguments
            )

            // Build full transaction (without signature - this is just simulation)
            val transaction = mapOf(
                "sender" to senderAddress,
                "sequence_number" to "0",
                "max_gas_amount" to "10000",
                "gas_unit_price" to "100",
                "expiration_timestamp_secs" to ((System.currentTimeMillis() / 1000) + 600).toString(),
                "payload" to payload
            )

            val gson = com.google.gson.Gson()
            val jsonPayload = gson.toJson(transaction)

            Log.d(TAG, "Transaction payload: $jsonPayload")

            // Note: This will fail without proper signature
            // TODO: Integrate wallet to sign the transaction
            val url = "${AptosConfig.DEFAULT_NETWORK_URL}/transactions"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toRequestBody(mediaType)

            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okhttp3.OkHttpClient.Builder()
                .connectTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(AptosConfig.REQUEST_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
                .newCall(request)
                .execute()

            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                Log.d(TAG, "Transaction submitted: $body")

                @Suppress("UNCHECKED_CAST")
                val responseData = gson.fromJson(body, Map::class.java) as Map<String, Any>
                val txHash = responseData["hash"] as? String ?: "unknown"
                Result.success(txHash)
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                Log.e(TAG, "Transaction submission failed: ${response.code} ${response.message}")
                Log.e(TAG, "Error body: $errorBody")
                Result.failure(Exception("HTTP ${response.code}: $errorBody"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error submitting transaction", e)
            Result.failure(e)
        }
    }

    /**
     * Build a transaction for order placement
     */
    suspend fun buildPlaceOrderTransaction(
        senderAddress: String,
        marketPair: String,
        typeArguments: List<String>,
        price: Double,
        quantity: Double,
        isBuy: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Building place order transaction")
            Log.d(TAG, "Market: $marketPair, Price: $price, Quantity: $quantity, IsBuy: $isBuy")
            Log.d(TAG, "Type arguments: $typeArguments")

            // Use place_limit_order for both buy and sell (side is determined by parameter)
            val functionName = AptosConfig.ENTRY_PLACE_LIMIT_ORDER
            val functionId = "${AptosConfig.DEX_CONTRACT_ADDRESS}::${AptosConfig.DEX_MODULE}::$functionName"

            // Entry function arguments: (admin_addr: address, market_id: u64, side: bool, price: u64, size: u64)
            // side: false = buy, true = sell
            val marketId = 0 // Default market ID - should be determined by market pair
            val arguments = listOf(
                AptosConfig.DEX_CONTRACT_ADDRESS,  // admin_addr
                marketId.toString(),                // market_id
                (!isBuy).toString(),               // side (false=buy, true=sell)
                price.toLong().toString(),          // price
                quantity.toLong().toString()        // size
            )

            // Return unsigned transaction payload for wallet to sign and submit
            val payload = mapOf(
                "type" to "entry_function_payload",
                "function" to functionId,
                "type_arguments" to typeArguments,
                "arguments" to arguments
            )

            val transactionForWallet = mapOf(
                "sender" to senderAddress,
                "payload" to payload
            )

            val gson = com.google.gson.Gson()
            val jsonPayload = gson.toJson(transactionForWallet)
            Result.success(jsonPayload)

        } catch (e: Exception) {
            Log.e(TAG, "Error building transaction", e)
            Result.failure(e)
        }
    }
}
