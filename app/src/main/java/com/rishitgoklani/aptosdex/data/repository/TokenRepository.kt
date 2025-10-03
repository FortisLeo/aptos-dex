package com.rishitgoklani.aptosdex.data.repository

import com.rishitgoklani.aptosdex.data.remote.HermesPriceFeedDto
import com.rishitgoklani.aptosdex.presentation.tokens.TokenUi
import javax.inject.Inject
import com.rishitgoklani.aptosdex.di.HermesApiClientOkHttp
import com.rishitgoklani.aptosdex.di.IndexerApiClientOkHttp
import android.util.Log

interface TokenRepository {
    suspend fun getAptosTokens(limit: Int = 500): List<TokenUi>
    suspend fun searchIndexerTokens(limit: Int, offset: Int, query: String?): List<TokenUi>
}

class TokenRepositoryImpl @Inject constructor(
    private val hermesClient: HermesApiClientOkHttp,
    private val indexerClient: IndexerApiClientOkHttp
) : TokenRepository {
    override suspend fun getAptosTokens(limit: Int): List<TokenUi> {
        val feeds: List<HermesPriceFeedDto> = hermesClient.getPriceFeeds(limit)
        // Build a symbol -> icon map from indexer (first page wide enough)
        val metas = indexerClient.getFungibleAssetMetadata(limit = 2000, offset = 0, search = null)
        val symbolToIcon: Map<String, String> = metas
            .mapNotNull { m ->
                val icon = m.iconUrl
                val symbol = m.symbol?.trim()?.uppercase()
                if (!icon.isNullOrBlank() && !symbol.isNullOrBlank()) symbol to icon else null
            }
            .toMap()
        Log.d("TokenRepository", "Raw feeds count: ${feeds.size}")
        val tokens = feeds
            .asSequence()
            .mapNotNull { dto: HermesPriceFeedDto ->
            val priceId = dto.id ?: return@mapNotNull null
            val attributes = dto.attributes ?: return@mapNotNull null
            val symbol = attributes.symbol ?: return@mapNotNull null
                val base = attributes.base ?: symbol
                val name = base.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val imageUrl = symbolToIcon[base.trim().uppercase()] ?: symbolToIcon[symbol.trim().uppercase()]
            Log.d("TokenRepository", "Parsed token: name=$name, symbol=$symbol, id=$priceId")
            TokenUi(
                name = name,
                    symbol = base, // show clean symbol like BONK rather than Crypto.BONK/USD
                address = priceId,
                    imageUrl = imageUrl,
                iconRes = com.rishitgoklani.aptosdex.R.drawable.ic_trending_up_24
            )
            }
            .sortedBy { it.name.lowercase() }
            .toList()
        Log.d("TokenRepository", "Final tokens count: ${tokens.size}")
        return tokens
    }

    override suspend fun searchIndexerTokens(limit: Int, offset: Int, query: String?): List<TokenUi> {
        val metas = indexerClient.getFungibleAssetMetadata(limit = limit, offset = offset, search = query)
        return metas
            .asSequence()
            .filter { it.assetType?.equals("Crypto", ignoreCase = true) == true }
            .map { m ->
                val name = m.name.trim()
                val symbol = m.symbol.trim()
                TokenUi(
                    name = name,
                    symbol = symbol,
                    address = symbol, // placeholder; price feed id resolved on selection
                    imageUrl = m.iconUrl,
                    iconRes = com.rishitgoklani.aptosdex.R.drawable.ic_trending_up_24
                )
            }
            .sortedBy { it.name.lowercase() }
            .toList()
    }
}


