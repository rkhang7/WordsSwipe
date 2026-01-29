package com.example.wordsswipe.di

import com.example.wordsswipe.data.remote.api.DictionaryApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Hilt module for configuring Retrofit and networking dependencies.
 *
 * Provides:
 * - OkHttpClient with logging interceptor
 * - Moshi JSON adapter factory
 * - Retrofit instance configured for Dictionary API
 * - DictionaryApi service interface
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DICTIONARY_API_BASE_URL = "https://api.dictionaryapi.dev/api/v2/"

    /**
     * Provides Moshi instance for JSON serialization/deserialization.
     * Includes KotlinJsonAdapterFactory for proper Kotlin support.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Provides OkHttpClient with logging interceptor for debugging.
     * Logging disabled in release builds for privacy.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provides Retrofit instance configured for Dictionary API.
     * Uses Moshi for JSON conversion and OkHttpClient for HTTP operations.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(DICTIONARY_API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    /**
     * Provides DictionaryApi service for making API calls.
     * Created from Retrofit instance.
     */
    @Provides
    @Singleton
    fun provideDictionaryApi(retrofit: Retrofit): DictionaryApi =
        retrofit.create(DictionaryApi::class.java)
}
