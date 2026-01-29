package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.data.local.WordsRepository
import javax.inject.Inject

/**
 * UseCase for retrieving random English words from the asset file.
 *
 * This encapsulates the business logic of word retrieval with random selection.
 * Follows the UseCase pattern for clean separation between domain and data layers.
 */
class GetRandomWordsUseCase @Inject constructor(
    private val wordsRepository: WordsRepository
) {

    /**
     * Retrieves a random selection of English words.
     *
     * Operator function allows clean invocation syntax:
     * ```
     * val words = getRandomWordsUseCase(5)  // Instead of invoke(5)
     * ```
     *
     * @param count Number of random words to retrieve
     * @return List of randomly selected words without duplicates
     */
    suspend operator fun invoke(count: Int): List<String> =
        wordsRepository.getRandomWords(count)
}

/**
 * UseCase for retrieving all available English words.
 *
 * Useful for initialization, preloading, or operations that need
 * access to the complete word set.
 */
class GetAllWordsUseCase @Inject constructor(
    private val wordsRepository: WordsRepository
) {

    /**
     * Retrieves all English words from the asset file.
     *
     * Results are cached after first call for performance.
     *
     * @return List of all available English words
     */
    suspend operator fun invoke(): List<String> =
        wordsRepository.getAllWords()
}

/**
 * UseCase for retrieving a single random word.
 *
 * Useful for quick random selections or single-word operations.
 */
class GetRandomWordUseCase @Inject constructor(
    private val wordsRepository: WordsRepository
) {

    /**
     * Retrieves a single randomly selected English word.
     *
     * @return A random English word from the asset file
     */
    suspend operator fun invoke(): String =
        wordsRepository.getRandomWord()
}
