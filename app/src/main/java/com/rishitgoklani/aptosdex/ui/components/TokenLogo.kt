package com.rishitgoklani.aptosdex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rishitgoklani.aptosdex.R

@Composable
fun AptosLogo(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.aptos_apt_logo),
        contentDescription = "Aptos Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun UsdcLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.usd_coin_usdc_logo),
        contentDescription = "USDC Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun BonkLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.bonk1_bonk_logo),
        contentDescription = "BONK Logo",
        modifier = modifier.size(size)
    )
}
