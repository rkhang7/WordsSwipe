package com.example.wordsswipe.di

import com.example.wordsswipe.data.repository.WordRepositoryImpl
import com.example.wordsswipe.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for dependency injection configuration.
 * This module binds interfaces to their implementations at the application singleton level.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        implementation: WordRepositoryImpl
    ): WordRepository
}
