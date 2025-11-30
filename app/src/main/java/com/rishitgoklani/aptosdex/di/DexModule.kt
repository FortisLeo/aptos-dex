package com.rishitgoklani.aptosdex.di

import com.rishitgoklani.aptosdex.data.repository.DexRepositoryImpl
import com.rishitgoklani.aptosdex.domain.repository.DexRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for DEX smart contract integrations
 * Provides DexRepository implementation using Kaptos SDK
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DexModule {

    @Binds
    @Singleton
    abstract fun bindDexRepository(
        impl: DexRepositoryImpl
    ): DexRepository
}
