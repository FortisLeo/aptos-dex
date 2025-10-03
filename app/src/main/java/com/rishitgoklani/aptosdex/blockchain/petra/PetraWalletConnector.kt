package com.rishitgoklani.aptosdex.blockchain.petra

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PetraWalletConnector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
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
        }
    }

    fun canOpenPetraWallet(): Boolean {
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PETRA_LINK_BASE))
        val activities = context.packageManager.queryIntentActivities(testIntent, 0)
        return activities.isNotEmpty()
    }
}
