package com.rishitgoklani.aptosdex

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rishitgoklani.aptosdex.presentation.biometric.BiometricAuthManager
import com.rishitgoklani.aptosdex.presentation.biometric.BiometricPermissionScreen
import com.rishitgoklani.aptosdex.presentation.biometric.BiometricViewModel
import com.rishitgoklani.aptosdex.presentation.home.HomeScreen
import com.rishitgoklani.aptosdex.presentation.navigation.AppScreen
import com.rishitgoklani.aptosdex.presentation.navigation.OnboardingNavigationManager
import com.rishitgoklani.aptosdex.presentation.navigation.rememberConnectionStateTracker
import com.rishitgoklani.aptosdex.presentation.notification.NotificationPermissionScreen
import com.rishitgoklani.aptosdex.presentation.notification.NotificationViewModel
import com.rishitgoklani.aptosdex.presentation.wallet.WalletConnectScreen
import com.rishitgoklani.aptosdex.presentation.wallet.WalletViewModel
import com.rishitgoklani.aptosdex.ui.theme.AptosDEXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val walletViewModel: WalletViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val biometricViewModel: BiometricViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            AptosDEXTheme {
                val walletUiState by walletViewModel.uiState.collectAsStateWithLifecycle()

                // Navigation management
                val prefs = remember { getSharedPreferences("app_prefs", MODE_PRIVATE) }
                val navigationManager = remember {
                    OnboardingNavigationManager(prefs, walletUiState.isConnected)
                }

                var currentScreen by remember { mutableStateOf(navigationManager.getInitialScreen()) }

                // Track new wallet connections
                val isNewConnection = rememberConnectionStateTracker(walletUiState.isConnected)

                // Biometric authentication
                val biometricAuthManager = remember {
                    BiometricAuthManager(
                        activity = this@MainActivity,
                        onSuccess = { biometricViewModel.onBiometricSuccess() },
                        onError = { biometricViewModel.onBiometricError() },
                        onFailed = { biometricViewModel.onBiometricFailed() }
                    )
                }

                // Permission launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        notificationViewModel.onPermissionGranted()
                    } else {
                        notificationViewModel.onPermissionDenied()
                    }
                }

                // Event collectors
                CollectWalletEvents(walletViewModel)
                CollectNotificationEvents(
                    notificationViewModel = notificationViewModel,
                    permissionLauncher = permissionLauncher,
                    navigationManager = navigationManager,
                    onNavigate = { currentScreen = AppScreen.BIOMETRIC_PERMISSION }
                )
                CollectBiometricEvents(
                    biometricViewModel = biometricViewModel,
                    biometricAuthManager = biometricAuthManager,
                    navigationManager = navigationManager,
                    onNavigate = { currentScreen = AppScreen.HOME }
                )

                // Handle new wallet connections
                LaunchedEffect(isNewConnection) {
                    if (isNewConnection &&
                        currentScreen == AppScreen.WALLET_CONNECT &&
                        !navigationManager.hasSeenNotificationScreen()) {
                        currentScreen = AppScreen.NOTIFICATION_PERMISSION
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.WALLET_CONNECT -> {
                            WalletConnectScreen(
                                modifier = Modifier.padding(innerPadding),
                                onConnectClick = { walletViewModel.connectWallet() },
                                isConnected = walletUiState.isConnected,
                                walletAddress = walletUiState.walletAddress,
                                walletBalance = walletUiState.walletBalance
                            )
                        }
                        AppScreen.NOTIFICATION_PERMISSION -> {
                            NotificationPermissionScreen(
                                onSkipClick = { notificationViewModel.onSkipClick() },
                                onEnableClick = { notificationViewModel.onEnableClick() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.BIOMETRIC_PERMISSION -> {
                            BiometricPermissionScreen(
                                onSkipClick = { biometricViewModel.onSkipClick() },
                                onEnableClick = { biometricViewModel.onEnableClick() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.HOME -> {
                            HomeScreen(
                                walletAddress = walletUiState.walletAddress,
                                walletBalance = walletUiState.walletBalance,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            walletViewModel.handleDeepLink(data)
        }
    }
}

@Composable
private fun CollectWalletEvents(walletViewModel: WalletViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        walletViewModel.navigationEvents.collect { event ->
            when (event) {
                is WalletViewModel.NavigationEvent.OpenPetraWallet -> {
                    try {
                        context.startActivity(event.intent)
                    } catch (e: Exception) {
                        // Handle error - Petra app not installed
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectNotificationEvents(
    notificationViewModel: NotificationViewModel,
    permissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>,
    navigationManager: OnboardingNavigationManager,
    onNavigate: () -> Unit
) {
    LaunchedEffect(Unit) {
        notificationViewModel.navigationEvents.collect { event ->
            when (event) {
                is NotificationViewModel.NavigationEvent.RequestPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                is NotificationViewModel.NavigationEvent.NavigateToHome -> {
                    navigationManager.markNotificationScreenSeen()
                    onNavigate()
                }
            }
        }
    }
}

@Composable
private fun CollectBiometricEvents(
    biometricViewModel: BiometricViewModel,
    biometricAuthManager: BiometricAuthManager,
    navigationManager: OnboardingNavigationManager,
    onNavigate: () -> Unit
) {
    LaunchedEffect(Unit) {
        biometricViewModel.navigationEvents.collect { event ->
            when (event) {
                is BiometricViewModel.NavigationEvent.ShowBiometricPrompt -> {
                    biometricAuthManager.authenticate()
                }
                is BiometricViewModel.NavigationEvent.NavigateToHome -> {
                    navigationManager.markBiometricScreenSeen()
                    onNavigate()
                }
            }
        }
    }
}