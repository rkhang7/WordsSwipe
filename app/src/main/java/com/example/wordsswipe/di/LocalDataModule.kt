package com.example.wordsswipe.di

import android.content.Context
import com.example.wordsswipe.data.local.WordsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for local data repositories.
 *
 * Provides singleton instances of repositories that handle
 * local data operations (asset files, preferences, etc).
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    /**
     * Provides the WordsRepository singleton instance.
     *
     * @param context Application context for accessing asset files
     * @return Singleton instance of WordsRepository
     */
    @Provides
    @Singleton
    fun provideWordsRepository(
        @ApplicationContext context: Context
    ): WordsRepository = WordsRepository(context)
}
