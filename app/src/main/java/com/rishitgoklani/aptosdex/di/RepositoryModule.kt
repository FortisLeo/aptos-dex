package com.rishitgoklani.aptosdex.di
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.rishitgoklani.aptosdex.data.repository.TokenRepository
import com.rishitgoklani.aptosdex.data.repository.TokenRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import com.google.gson.Gson
import com.rishitgoklani.aptosdex.blockchain.crypto.CryptoKeyManager
import com.rishitgoklani.aptosdex.data.repository.CryptoRepositoryImpl
import com.rishitgoklani.aptosdex.data.repository.WalletRepositoryImpl
import com.rishitgoklani.aptosdex.domain.repository.CryptoRepository
import com.rishitgoklani.aptosdex.domain.repository.WalletRepository
import dagger.Binds
import com.rishitgoklani.aptosdex.domain.repository.PriceRepository
import com.rishitgoklani.aptosdex.data.repository.PriceRepositoryImpl


@Module
@InstallIn(SingletonComponent::class)
object TokenRepositoryModule {

    @Provides
    @Singleton
    fun provideHermesApiClient(okHttpClient: OkHttpClient, gson: Gson): HermesApiClientOkHttp = HermesApiClientOkHttp(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideIndexerApiClient(okHttpClient: OkHttpClient, gson: Gson): IndexerApiClientOkHttp = IndexerApiClientOkHttp(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideTokenRepository(client: HermesApiClientOkHttp, indexerClient: IndexerApiClientOkHttp): TokenRepository = TokenRepositoryImpl(client, indexerClient)
}

// Simple OkHttp-based Hermes client (no Retrofit)
class HermesApiClientOkHttp(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    suspend fun getPriceFeeds(limit: Int? = null): List<com.rishitgoklani.aptosdex.data.remote.HermesPriceFeedDto> {
        val req = okhttp3.Request.Builder()
            .url("https://hermes.pyth.network/v2/price_feeds?chain=aptos")
            .get()
            .build()
        okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                Log.w("HermesApi", "Non-200: code=${resp.code}")
                return emptyList()
            }
            val reader = resp.body?.charStream() ?: return emptyList()
            val jsonReader = com.google.gson.stream.JsonReader(reader)
            val out = mutableListOf<com.rishitgoklani.aptosdex.data.remote.HermesPriceFeedDto>()
            var count = 0
            try {
                jsonReader.beginArray()
                while (jsonReader.hasNext()) {
                    val dto = gson.fromJson<com.rishitgoklani.aptosdex.data.remote.HermesPriceFeedDto>(
                        jsonReader,
                        com.rishitgoklani.aptosdex.data.remote.HermesPriceFeedDto::class.java
                    )
                    out.add(dto)
                    count++
                    if (limit != null && count >= limit) break
                }
                if (jsonReader.peek() != com.google.gson.stream.JsonToken.END_ARRAY) {
                    while (jsonReader.hasNext()) jsonReader.skipValue()
                }
                jsonReader.endArray()
            } finally {
                try { jsonReader.close() } catch (_: Throwable) {}
            }
            Log.d("HermesApi", "stream parsed count=${out.size} limit=$limit")
            return out
        }
    }
}


// Simple GraphQL client for Aptos Indexer (fetch token metadata)
class IndexerApiClientOkHttp(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val endpoint = "https://api.mainnet.aptoslabs.com/v1/graphql"

    data class TokenMeta(
        val name: String,
        val symbol: String,
        val iconUrl: String?,
        val assetType: String?
    )

    suspend fun getFungibleAssetMetadata(limit: Int, offset: Int, search: String?): List<TokenMeta> {
        val query = """
            query Tokens(${'$'}limit: Int!, ${'$'}offset: Int!, ${'$'}search: String) {
              fungible_asset_metadata(
                limit: ${'$'}limit
                offset: ${'$'}offset
                order_by: { name: asc }
                where: {
                  _or: [
                    { name: { _ilike: ${'$'}search } }
                    { symbol: { _ilike: ${'$'}search } }
                  ]
                }
              ) {
                name
                symbol
                icon_uri
                asset_type
              }
            }
        """.trimIndent()

        val variables = com.google.gson.JsonObject().apply {
            addProperty("limit", limit)
            addProperty("offset", offset)
            addProperty("search", if (search.isNullOrBlank()) "%" else "%${search.trim()}%")
        }
        val bodyJson = com.google.gson.JsonObject().apply {
            addProperty("query", query)
            add("variables", variables)
        }

        val req = okhttp3.Request.Builder()
            .url(endpoint)
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        Log.d("IndexerApi", "POST $endpoint body=${bodyJson.toString().take(400)}")
        okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string().orEmpty()
                Log.w("IndexerApi", "Non-200: code=${resp.code} body=${errBody.take(400)}")
                return emptyList()
            }
            val bodyStr = resp.body?.string().orEmpty()
            Log.d("IndexerApi", "resp len=${bodyStr.length} body=${bodyStr.take(500)}")
            val root = gson.fromJson(bodyStr, com.google.gson.JsonObject::class.java)
            val data = root.getAsJsonObject("data") ?: return emptyList()
            val arr = data.getAsJsonArray("fungible_asset_metadata") ?: return emptyList()
            val out = mutableListOf<TokenMeta>()
            arr.forEach { el ->
                val obj = el.asJsonObject
                val name = obj.get("name")?.asString ?: return@forEach
                val symbol = obj.get("symbol")?.asString ?: return@forEach
                val icon = obj.get("icon_uri")?.takeIf { !it.isJsonNull }?.asString
                val assetType = obj.get("asset_type")?.takeIf { !it.isJsonNull }?.asString
                out.add(TokenMeta(name = name, symbol = symbol, iconUrl = icon, assetType = assetType))
            }
            Log.d("IndexerApi", "metadata count=${out.size}")
            return out
        }
    }
}



@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCryptoRepository(
        cryptoRepositoryImpl: CryptoRepositoryImpl
    ): CryptoRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(
        impl: PriceRepositoryImpl
    ): PriceRepository

    @Binds
    @Singleton
    abstract fun bindTokenPriceRepository(
        impl: com.rishitgoklani.aptosdex.data.repository.TokenPriceRepositoryImpl
    ): com.rishitgoklani.aptosdex.domain.repository.TokenPriceRepository

    @Binds
    @Singleton
    abstract fun bindChartDataRepository(
        impl: com.rishitgoklani.aptosdex.data.repository.ChartDataRepositoryImpl
    ): com.rishitgoklani.aptosdex.domain.repository.ChartDataRepository

    @Binds
    @Singleton
    abstract fun bindLivePriceRepository(
        impl: com.rishitgoklani.aptosdex.data.repository.LivePriceRepositoryImpl
    ): com.rishitgoklani.aptosdex.domain.repository.LivePriceRepository

    @Binds
    @Singleton
    abstract fun bindOrderBookRepository(
        impl: com.rishitgoklani.aptosdex.data.repository.OrderBookRepositoryImpl
    ): com.rishitgoklani.aptosdex.domain.repository.OrderBookRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideCryptoKeyManager(): CryptoKeyManager {
        return CryptoKeyManager()
    }
}
