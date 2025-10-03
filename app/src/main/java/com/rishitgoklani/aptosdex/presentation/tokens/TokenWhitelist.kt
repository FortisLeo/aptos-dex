package com.rishitgoklani.aptosdex.presentation.tokens

import com.rishitgoklani.aptosdex.R

object TokenWhitelist {
    val allowed: List<TokenUi> = listOf(
        TokenUi(
            name = "Aptos Coin",
            symbol = "APT",
            address = "0x1::aptos_coin::AptosCoin",
            imageUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/21794.png",
            iconRes = R.drawable.ic_trending_up_24
        ),
        TokenUi(
            name = "Tether USD",
            symbol = "USDt",
            address = "0x357b0b74bc833e95a115ad22604854d6b0fca151cecd94111770e5d6ffc9dc2b",
            imageUrl = "https://tether.to/images/logoCircle.png",
            iconRes = R.drawable.ic_trending_up_24
        ),
        TokenUi(
            name = "Wrapped BTC",
            symbol = "WBTC",
            address = "0x68844a0d7f2587e726ad0579f3d640865bb4162c08a4589eeda3f9689ec52a3d",
            imageUrl = "https://assets.coingecko.com/coins/images/7598/standard/wrapped_bitcoin_wbtc.png",
            iconRes = R.drawable.ic_trending_up_24
        ),
        TokenUi(
            name = "USDC",
            symbol = "USDC",
            address = "0xbae207659db88bea0cbead6da0ed00aac12edcdda169e591cd41c94180b46f3b",
            imageUrl = "https://s2.coinmarketcap.com/static/img/coins/200x200/3408.png",
            iconRes = R.drawable.ic_trending_up_24
        ),
        TokenUi(
            name = "PancakeSwap Token",
            symbol = "Cake",
            address = "0x159df6b7689437016108a019fd5bef736bac692b6d4a1f10c941f6fbb9a74ca6::oft::CakeOFT",
            imageUrl = "https://assets.coingecko.com/coins/images/12632/standard/pancakeswap-cake-logo_(1).png?1696512440",
            iconRes = R.drawable.ic_trending_up_24
        )
    )
}


