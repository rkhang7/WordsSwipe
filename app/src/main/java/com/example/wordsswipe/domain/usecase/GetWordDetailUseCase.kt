package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.data.remote.repository.DictionaryRepository
import com.example.wordsswipe.domain.model.WordDetail
import javax.inject.Inject

/**
 * UseCase for fetching word details from Dictionary API.
 *
 * Encapsulates business logic for word lookup operations.
 * Provides clean operator function syntax for easy usage.
 *
 * Throws exceptions for error handling:
 * - IllegalArgumentException: Invalid input (blank word)
 * - WordNotFoundException: Word not found (404)
 * - DictionaryApiException: API or network errors
 */
class GetWordDetailUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {

    /**
     * Fetches detailed information about a word.
     *
     * @param word The English word to look up
     * @return Domain model with word details
     * @throws IllegalArgumentException if word is blank
     * @throws WordNotFoundException if word not found
     * @throws DictionaryApiException for other errors
     */
    suspend operator fun invoke(word: String): WordDetail =
        dictionaryRepository.getWordDetail(word)
}
