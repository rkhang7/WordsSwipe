package com.example.wordsswipe.data.remote.repository

import com.example.wordsswipe.data.remote.api.DictionaryApi
import com.example.wordsswipe.data.remote.model.WordDetailResponse
import com.example.wordsswipe.domain.model.Definition
import com.example.wordsswipe.domain.model.Meaning
import com.example.wordsswipe.domain.model.Phonetic
import com.example.wordsswipe.domain.model.WordDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Dictionary API operations.
 *
 * Handles:
 * - API communication via Retrofit
 * - Error handling (HTTP, network, empty response)
 * - DTO to domain model mapping
 * - Proper coroutine dispatching
 */
@Singleton
class DictionaryRepository @Inject constructor(
    private val dictionaryApi: DictionaryApi
) {

    /**
     * Fetches detailed information about a word from Dictionary API.
     *
     * @param word The English word to look up
     * @return Domain model containing word details
     * @throws IllegalArgumentException if word is blank
     * @throws WordNotFoundException if word not found in dictionary (404)
     * @throws DictionaryApiException for other HTTP errors or network issues
     */
    suspend fun getWordDetail(word: String): WordDetail = withContext(Dispatchers.IO) {
        // Validate input
        require(word.isNotBlank()) { "Word cannot be blank" }

        try {
            // Make API call
            val response = dictionaryApi.getWordDetail(word.trim().lowercase())

            // Handle HTTP errors
            when {
                response.isSuccessful -> {
                    // Get response body
                    val body = response.body()

                    // Handle empty response
                    if (body.isNullOrEmpty()) {
                        throw DictionaryApiException("Empty response from API")
                    }

                    // API returns a list, we take the first item
                    val wordDetailResponse = body.first()

                    // Map DTO to domain model
                    wordDetailResponse.toDomainModel()
                }

                response.code() == 404 -> {
                    throw WordNotFoundException("Word '$word' not found in dictionary")
                }

                else -> {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    throw DictionaryApiException(
                        "HTTP ${response.code()}: $errorBody"
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                // Re-throw custom exceptions
                is WordNotFoundException,
                is DictionaryApiException -> throw e

                // Wrap network and other exceptions
                else -> throw DictionaryApiException(
                    "Error fetching word details: ${e.message}",
                    cause = e
                )
            }
        }
    }

    /**
     * Maps DTO response to domain model.
     */
    private fun WordDetailResponse.toDomainModel(): WordDetail = WordDetail(
        word = this.word,
        phonetic = this.phonetic,
        phonetics = this.phonetics.map { it.toDomainModel() },
        meanings = this.meanings.map { it.toDomainModel() }
    )

    private fun com.example.wordsswipe.data.remote.model.PhoneticDto.toDomainModel(): Phonetic =
        Phonetic(
            text = this.text,
            audio = this.audio
        )

    private fun com.example.wordsswipe.data.remote.model.MeaningDto.toDomainModel(): Meaning =
        Meaning(
            partOfSpeech = this.partOfSpeech,
            definitions = this.definitions.map { it.toDomainModel() },
            synonyms = this.synonyms,
            antonyms = this.antonyms
        )

    private fun com.example.wordsswipe.data.remote.model.DefinitionDto.toDomainModel(): Definition =
        Definition(
            definition = this.definition,
            example = this.example,
            synonyms = this.synonyms
        )
}

/**
 * Exception thrown when a word is not found in the dictionary (404 response).
 */
class WordNotFoundException(message: String) : Exception(message)

/**
 * Generic exception for Dictionary API errors.
 */
class DictionaryApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
