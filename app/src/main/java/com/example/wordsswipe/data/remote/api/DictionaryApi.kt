package com.example.wordsswipe.data.remote.api

import com.example.wordsswipe.data.remote.model.WordDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for Dictionary API.
 *
 * Endpoint: https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 *
 * Thread-safe and can be shared across the application.
 */
interface DictionaryApi {

    /**
     * Fetches detailed information about a word.
     *
     * @param word The English word to look up
     * @return Response containing word details or error information
     *
     * Success (200): Returns list of word details (usually single item)
     * Not found (404): Word not found in dictionary
     * Error (5xx): Server error
     */
    @GET("entries/en/{word}")
    suspend fun getWordDetail(@Path("word") word: String): Response<List<WordDetailResponse>>
}
