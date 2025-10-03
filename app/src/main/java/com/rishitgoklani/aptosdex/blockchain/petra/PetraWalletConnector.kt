package com.rishitgoklani.aptosdex.blockchain.petra

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.rishitgoklani.aptosdex.data.local.ConnectionStateStorage
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PetraWalletConnector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val connectionStateStorage: ConnectionStateStorage,
    private val cryptoRepository: CryptoRepository
) {
    companion object {
        private const val TAG = "PetraWalletConnector"
        private const val PETRA_LINK_BASE = "petra://api/v1"
        private const val DAPP_LINK_BASE = "aptosdex://api/v1"
        private const val APP_DOMAIN = "https://aptosdex.app"
        private const val APP_NAME = "AptosDEX"
    }

    fun buildConnectionIntent(publicKeyHex: String): Intent {
        val appInfo = mapOf(
            "domain" to APP_DOMAIN,
            "name" to APP_NAME
        )

        val connectionData = mapOf(
            "appInfo" to appInfo,
            "redirectLink" to "$DAPP_LINK_BASE/connect",
            "dappEncryptionPublicKey" to publicKeyHex
        )

        val jsonData = gson.toJson(connectionData)
        val encodedData = Base64.encodeToString(jsonData.toByteArray(), Base64.NO_WRAP)

        Log.d(TAG, "Connection data: $jsonData")

        val petraUri = Uri.parse("$PETRA_LINK_BASE/connect?data=$encodedData")
        Log.d(TAG, "Opening Petra app with URI: $petraUri")

        return Intent(Intent.ACTION_VIEW, petraUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.aptoslabs.petra.wallet")
        }
    }

    fun canOpenPetraWallet(): Boolean {
        // Method 1: Check for the base Petra deep link format
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("petra://api/v1"))
        val activities = context.packageManager.queryIntentActivities(testIntent, 0)
        
        Log.d(TAG, "Checking for Petra wallet - found ${activities.size} activities")
        activities.forEach { activity ->
            Log.d(TAG, "Petra activity: ${activity.activityInfo.packageName}")
        }
        
        if (activities.isNotEmpty()) {
            return true
        }
        
        // Method 2: Check if Petra package is installed directly
        try {
            context.packageManager.getPackageInfo("com.aptoslabs.petra.wallet", 0)
            Log.d(TAG, "Petra wallet package found directly")
            return true
        } catch (e: Exception) {
            Log.d(TAG, "Petra wallet package not found: ${e.message}")
        }
        
        // Method 3: Try alternative deep link formats
        val alternativeIntents = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("petra://")),
            Intent(Intent.ACTION_VIEW, Uri.parse("petra://api")),
            Intent(Intent.ACTION_VIEW, Uri.parse("petra://api/v1/connect"))
        )
        
        for (intent in alternativeIntents) {
            val altActivities = context.packageManager.queryIntentActivities(intent, 0)
            if (altActivities.isNotEmpty()) {
                Log.d(TAG, "Found Petra with alternative deep link: ${intent.data}")
                return true
            }
        }
        
        return false
    }

    fun buildSignAndSubmitIntent(payloadJson: String): Intent {
        // Ensure we have the shared key from the connect flow
        val connectionKeys = connectionStateStorage.restoreConnectionKeys()
        if (connectionKeys == null || connectionKeys.sharedKey == null) {
            Log.e(TAG, "No connection keys or shared key found")
            throw IllegalStateException("Wallet not connected. Please connect wallet first.")
        }

        // Parse the transaction payload (contains sender and payload)
        val transactionData = gson.fromJson(payloadJson, Map::class.java) as Map<String, Any>
        val payload = transactionData["payload"] as Map<String, Any>

        // Create the exact payload structure that Petra expects
        val entryFunctionPayload = mapOf(
            "type" to "entry_function_payload",
            "function" to payload["function"],
            "type_arguments" to payload["type_arguments"],
            "arguments" to payload["arguments"]
        )
        
        // Base64 encode the JSON payload
        val entryFunctionJson = gson.toJson(entryFunctionPayload)
        val base64Payload = Base64.encodeToString(entryFunctionJson.toByteArray(), Base64.NO_WRAP)
        
        // Encrypt the base64 string directly (not as JSON)
        val payloadToEncrypt = base64Payload

        // Generate 24-byte nonce and encrypt with NaCl box.after (shared key)
        val nonce = cryptoRepository.generateNonce()
        val encryptionResult = cryptoRepository.encryptPayload(
            payloadToEncrypt,
            nonce,
            connectionKeys.sharedKey
        )
        if (encryptionResult.isFailure) {
            Log.e(TAG, "Failed to encrypt payload: ${encryptionResult.exceptionOrNull()?.message}")
            throw Exception("Failed to encrypt transaction payload")
        }
        val encryptedPayload = encryptionResult.getOrThrow()

        // Build request per Petra spec: appInfo, payload (hex), redirectLink, dappEncryptionPublicKey (hex), nonce (hex)
        val requestData = mapOf(
            "appInfo" to mapOf(
                "domain" to APP_DOMAIN,
                "name" to APP_NAME
            ),
            "payload" to cryptoRepository.bytesToHex(encryptedPayload),
            "redirectLink" to "$DAPP_LINK_BASE/response",
            "dappEncryptionPublicKey" to cryptoRepository.bytesToHex(connectionKeys.publicKey).removePrefix("0x"),
            "nonce" to cryptoRepository.bytesToHex(nonce)
        )

        val jsonData = gson.toJson(requestData)
        val encodedData = Base64.encodeToString(jsonData.toByteArray(), Base64.NO_WRAP)

        Log.d(TAG, "Sign request data: $jsonData")

        val petraUri = Uri.parse("$PETRA_LINK_BASE/signAndSubmit?data=$encodedData")
        Log.d(TAG, "Opening Petra app with URI: $petraUri")

        return Intent(Intent.ACTION_VIEW, petraUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.aptoslabs.petra.wallet")
        }
    }
}
