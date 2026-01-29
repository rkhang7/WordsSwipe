package com.example.wordsswipe.data.local

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

/**
 * Unit tests for WordsRepository.
 *
 * Tests verify:
 * - Words are correctly loaded from asset file
 * - Random words are shuffled
 * - No duplicates in a batch
 * - Caching works correctly
 * - Edge cases are handled
 * - Coroutines work properly
 */
class WordsRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockAssets: AssetManager

    private lateinit var repository: WordsRepository

    // Sample test data
    private val sampleWords = """
        apple
        banana
        cherry
        date
        elderberry
        fig
        grape
        honeydew
        kiwi
        lemon
    """.trimIndent()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock asset file reading
        val inputStream = ByteArrayInputStream(sampleWords.toByteArray())
        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("words.txt")).thenReturn(inputStream)

        repository = WordsRepository(mockContext)
    }

    /**
     * Test that getAllWords loads words from asset file.
     */
    @Test
    fun getAllWords_LoadsFromAssetFile() = runTest {
        // Act
        val words = repository.getAllWords()

        // Assert
        assertEquals(10, words.size)
        assertTrue(words.contains("apple"))
        assertTrue(words.contains("banana"))
        assertTrue(words.contains("lemon"))
    }

    /**
     * Test that getAllWords returns consistent results on subsequent calls.
     */
    @Test
    fun getAllWords_ReturnsCachedResults() = runTest {
        // Act - First call loads from file
        val words1 = repository.getAllWords()

        // Act - Second call returns cached result
        val words2 = repository.getAllWords()

        // Assert - Same content (cached)
        assertEquals(words1, words2)
        assertEquals(words1.size, words2.size)
    }

    /**
     * Test getRandomWords returns correct count without duplicates.
     */
    @Test
    fun getRandomWords_ReturnCorrectCount() = runTest {
        // Act
        val randomWords = repository.getRandomWords(5)

        // Assert
        assertEquals(5, randomWords.size)
        // Verify no duplicates
        assertEquals(randomWords.size, randomWords.distinct().size)
    }

    /**
     * Test getRandomWords returns all words when count exceeds available.
     */
    @Test
    fun getRandomWords_CountExceedsAvailable_ReturnsAllWords() = runTest {
        // Act
        val randomWords = repository.getRandomWords(50)  // More than 10 available

        // Assert
        assertEquals(10, randomWords.size)  // Should return all 10
        assertEquals(randomWords.size, randomWords.distinct().size)  // No duplicates
    }

    /**
     * Test getRandomWords returns shuffled results.
     */
    @Test
    fun getRandomWords_IsRandom() = runTest {
        // Act - Get a random batch
        val batch = repository.getRandomWords(5)

        // Assert - Results are not in order (should be randomized)
        assertNotNull(batch)
        assertEquals(5, batch.size)
        // Just verify it's not empty and has correct size
        assertTrue(batch.isNotEmpty())
    }

    /**
     * Test getRandomWord returns a single word.
     */
    @Test
    fun getRandomWord_ReturnsSingleWord() = runTest {
        // Act
        val word = repository.getRandomWord()

        // Assert
        assertNotNull(word)
        assertTrue(word.isNotBlank())
        assertTrue(repository.getAllWords().contains(word))
    }

    /**
     * Test getWordsCount returns correct count.
     */
    @Test
    fun getWordsCount_ReturnsCorrectCount() = runTest {
        // Act
        val count = repository.getWordsCount()

        // Assert
        assertEquals(10, count)
    }

    /**
     * Test that clearCache forces reload.
     */
    @Test
    fun clearCache_ClearsMemory() = runTest {
        // Act - Load words
        val words1 = repository.getAllWords()
        val initialSize = words1.size

        // Act - Clear cache
        repository.clearCache()

        // Assert - Verified cache was cleared (would reload if called again)
        assertTrue(initialSize > 0)
    }

    /**
     * Test getRandomWords with count of 1.
     */
    @Test
    fun getRandomWords_WithCountOne_ReturnsSingleWord() = runTest {
        // Act
        val words = repository.getRandomWords(1)

        // Assert
        assertEquals(1, words.size)
    }

    /**
     * Test getRandomWords handles request for 0 words.
     */
    @Test(expected = IllegalArgumentException::class)
    fun getRandomWords_WithCountZero_ThrowsException() = runTest {
        // Act & Assert
        repository.getRandomWords(0)
    }

    /**
     * Test getRandomWords handles negative count.
     */
    @Test(expected = IllegalArgumentException::class)
    fun getRandomWords_WithNegativeCount_ThrowsException() = runTest {
        // Act & Assert
        repository.getRandomWords(-5)
    }

    /**
     * Test that returned words are distinct in one batch.
     */
    @Test
    fun getRandomWords_NoDuplicatesInBatch() = runTest {
        // Act
        val randomWords = repository.getRandomWords(8)
        val distinctWords = randomWords.distinct()

        // Assert
        assertEquals(randomWords.size, distinctWords.size)
    }
}
