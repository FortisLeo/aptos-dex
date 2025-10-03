package com.rishitgoklani.aptosdex.presentation.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Manager for handling biometric authentication setup and callbacks.
 * Encapsulates BiometricPrompt configuration and lifecycle.
 */
class BiometricAuthManager(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onError: () -> Unit,
    private val onFailed: () -> Unit
) {

    private val biometricPrompt: BiometricPrompt by lazy {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )
    }

    private val promptInfo: BiometricPrompt.PromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify your identity")
            .setSubtitle("Use your fingerprint to enable biometric security")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
    }

    /**
     * Initiates biometric authentication flow.
     */
    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }
}
