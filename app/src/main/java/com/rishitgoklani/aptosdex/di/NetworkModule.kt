package com.rishitgoklani.aptosdex.di

import com.rishitgoklani.aptosdex.data.remote.binance.BinanceApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import com.rishitgoklani.aptosdex.data.remote.AptosApiClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AptosBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BinanceBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @AptosBaseUrl
    fun provideAptosBaseUrl(): String {
        return "https://api.testnet.aptoslabs.com/v1"
    }

    @Provides
    @BinanceBaseUrl
    fun provideBinanceBaseUrl(): String {
        return "https://api.binance.com"
    }

    @Provides
    @Singleton
    fun provideAptosApiClient(
        okHttpClient: OkHttpClient,
        @AptosBaseUrl baseUrl: String
    ): AptosApiClient {
        return AptosApiClient(okHttpClient, baseUrl)
    }

    @Provides
    @Singleton
    fun provideBinanceRetrofit(
        okHttpClient: OkHttpClient,
        @BinanceBaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBinanceApiService(retrofit: Retrofit): BinanceApiService {
        return retrofit.create(BinanceApiService::class.java)
    }
}
