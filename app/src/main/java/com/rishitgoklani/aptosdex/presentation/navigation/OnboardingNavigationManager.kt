package com.rishitgoklani.aptosdex.presentation.navigation

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Manages onboarding navigation flow state and logic.
 * Handles screen transitions and one-time display logic.
 */
enum class AppScreen {
    WALLET_CONNECT,
    NOTIFICATION_PERMISSION,
    BIOMETRIC_PERMISSION,
    HOME
}

class OnboardingNavigationManager(
    private val prefs: SharedPreferences,
    private val isWalletConnected: Boolean
) {

    fun getInitialScreen(): AppScreen {
        val hasSeenNotificationScreen = prefs.getBoolean(PREF_NOTIFICATION_SCREEN, false)
        val hasSeenBiometricScreen = prefs.getBoolean(PREF_BIOMETRIC_SCREEN, false)

        return if (isWalletConnected && hasSeenNotificationScreen && hasSeenBiometricScreen) {
            AppScreen.HOME
        } else {
            AppScreen.WALLET_CONNECT
        }
    }

    fun hasSeenNotificationScreen(): Boolean {
        return prefs.getBoolean(PREF_NOTIFICATION_SCREEN, false)
    }

    fun hasSeenBiometricScreen(): Boolean {
        return prefs.getBoolean(PREF_BIOMETRIC_SCREEN, false)
    }

    fun markNotificationScreenSeen() {
        prefs.edit().putBoolean(PREF_NOTIFICATION_SCREEN, true).apply()
    }

    fun markBiometricScreenSeen() {
        prefs.edit().putBoolean(PREF_BIOMETRIC_SCREEN, true).apply()
    }

    companion object {
        private const val PREF_NOTIFICATION_SCREEN = "has_seen_notification_screen"
        private const val PREF_BIOMETRIC_SCREEN = "has_seen_biometric_screen"
    }
}

/**
 * Composable helper to track wallet connection state changes.
 * Returns true only when a NEW connection occurs (false -> true transition).
 */
@Composable
fun rememberConnectionStateTracker(isConnected: Boolean): Boolean {
    var previousConnectionState by remember { mutableStateOf(false) }
    var isNewConnection by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        isNewConnection = isConnected && !previousConnectionState
        previousConnectionState = isConnected
    }

    return isNewConnection
}
