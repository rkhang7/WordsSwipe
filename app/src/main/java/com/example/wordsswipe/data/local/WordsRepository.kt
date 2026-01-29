package com.example.wordsswipe.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing English words loaded from local asset file.
 *
 * This repository:
 * - Loads words from assets/words.txt
 * - Caches words in memory after first load
 * - Provides random word selection with no duplicates
 * - Uses Kotlin Coroutines for asynchronous operations
 *
 * Thread-safe and optimized for repeated access.
 */
@Singleton
class WordsRepository @Inject constructor(
    private val context: Context
) {

    // Cached words list - loaded once and reused
    private var cachedWords: List<String>? = null

    /**
     * Loads all words from the asset file.
     * Runs on IO dispatcher and returns results on the calling context.
     *
     * Words are cached after first load for performance.
     *
     * @return List of all English words from the asset file
     * @throws Exception if the asset file cannot be read
     */
    suspend fun getAllWords(): List<String> = withContext(Dispatchers.IO) {
        // Return cached words if already loaded
        cachedWords?.let { return@withContext it }

        // Load words from asset file
        val words = try {
            context.assets.open("words.txt").bufferedReader().use { reader ->
                reader.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load words from asset file", e)
        }

        // Cache the loaded words
        cachedWords = words
        words
    }

    /**
     * Returns a random selection of words without duplicates.
     *
     * The returned list will contain up to [count] unique words, randomly selected
     * and shuffled. If the requested count exceeds available words, returns all
     * available words.
     *
     * Thread-safe and uses Dispatchers.IO for file operations.
     * Caching ensures subsequent calls are very fast.
     *
     * @param count Number of random words to retrieve
     * @return List of randomly selected words (size <= count)
     * @throws IllegalArgumentException if count <= 0
     * @throws Exception if words cannot be loaded from asset file
     */
    suspend fun getRandomWords(count: Int): List<String> = withContext(Dispatchers.Default) {
        require(count > 0) { "Count must be greater than 0, got: $count" }

        // Load all words (cached after first call)
        val allWords = getAllWords()

        // Handle case where requested count exceeds available words
        val actualCount = minOf(count, allWords.size)

        // Randomly shuffle and take the requested count
        allWords.shuffled().take(actualCount)
    }

    /**
     * Returns a random word from the collection.
     *
     * @return A single randomly selected word
     * @throws Exception if words cannot be loaded from asset file
     * @throws IllegalStateException if the word list is empty
     */
    suspend fun getRandomWord(): String = withContext(Dispatchers.Default) {
        val allWords = getAllWords()

        if (allWords.isEmpty()) {
            throw IllegalStateException("No words available in asset file")
        }

        allWords.random()
    }

    /**
     * Returns total number of words available.
     * Useful for validation and testing.
     *
     * @return Total count of words in the asset file
     */
    suspend fun getWordsCount(): Int = withContext(Dispatchers.IO) {
        getAllWords().size
    }

    /**
     * Clears the cached words, forcing reload on next access.
     * Useful for testing or if asset file might change.
     */
    fun clearCache() {
        cachedWords = null
    }
}
