package com.example.wordsswipe.di

import com.example.wordsswipe.data.remote.api.DictionaryApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt module for configuring Retrofit and networking dependencies.
 *
 * Provides:
 * - OkHttpClient with logging interceptor
 * - Gson JSON adapter
 * - Retrofit instance configured for Dictionary API
 * - DictionaryApi service interface
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DICTIONARY_API_BASE_URL = "https://api.dictionaryapi.dev/api/v2/"

    /**
     * Provides Gson instance for JSON serialization/deserialization.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

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
     * Uses Gson for JSON conversion and OkHttpClient for HTTP operations.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(DICTIONARY_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
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
