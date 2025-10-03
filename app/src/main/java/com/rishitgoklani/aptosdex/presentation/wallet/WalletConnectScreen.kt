package com.rishitgoklani.aptosdex.presentation.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import com.rishitgoklani.aptosdex.ui.components.CosmicBackground
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WalletConnectScreen(
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit,
    isConnected: Boolean,
    walletAddress: String?,
    walletBalance: String?
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cosmic starfield + subtle gradients behind content
        CosmicBackground(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isConnected) {
                // 80% - Orbital Animation Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    com.rishitgoklani.aptosdex.ui.components.OrbitalAnimation(
                        centerContent = {
                            com.rishitgoklani.aptosdex.ui.components.AptosLogo(size = 80.dp)
                        },
                        orbitingTokens = listOf(
                            com.rishitgoklani.aptosdex.ui.components.OrbitingToken(
                                icon = { com.rishitgoklani.aptosdex.ui.components.UsdcLogo(size = 52.dp) },
                                orbitRadius = 240f,
                                speed = 1.2f,
                                startAngle = 0f,
                                iconSize = 52f
                            ),
                            com.rishitgoklani.aptosdex.ui.components.OrbitingToken(
                                icon = { com.rishitgoklani.aptosdex.ui.components.BonkLogo(size = 52.dp) },
                                orbitRadius = 420f,
                                speed = 0.8f,
                                startAngle = 180f,
                                iconSize = 52f
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        orbitColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }

                // 20% - Connect Button Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onConnectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Connect Petra Wallet",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // Connected State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Wallet Connected!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    walletAddress?.let {
                        Text(
                            text = "Address:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${it.take(8)}...${it.takeLast(8)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Balance:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = walletBalance ?: "Loading...",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onConnectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = false
                    ) {
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
