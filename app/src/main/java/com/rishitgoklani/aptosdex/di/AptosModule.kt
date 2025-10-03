package com.rishitgoklani.aptosdex.di

import com.rishitgoklani.aptosdex.data.repository.AptosOrderBookRepository
import com.rishitgoklani.aptosdex.data.repository.AptosOrderBookRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Aptos blockchain dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AptosModule {

    @Binds
    @Singleton
    abstract fun bindAptosOrderBookRepository(
        impl: AptosOrderBookRepositoryImpl
    ): AptosOrderBookRepository
}
