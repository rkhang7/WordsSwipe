package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.data.local.WordsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for word-related UseCases.
 *
 * Tests verify:
 * - UseCases properly delegate to repository
 * - Operator function invoke() works correctly
 * - Suspend functions work with coroutines
 * - Error handling is proper
 */
class WordsUseCasesTest {

    @Mock
    private lateinit var mockRepository: WordsRepository

    private lateinit var getRandomWordsUseCase: GetRandomWordsUseCase
    private lateinit var getAllWordsUseCase: GetAllWordsUseCase
    private lateinit var getRandomWordUseCase: GetRandomWordUseCase

    private val testWords = listOf(
        "apple", "banana", "cherry", "date", "elderberry",
        "fig", "grape", "honeydew", "kiwi", "lemon"
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getRandomWordsUseCase = GetRandomWordsUseCase(mockRepository)
        getAllWordsUseCase = GetAllWordsUseCase(mockRepository)
        getRandomWordUseCase = GetRandomWordUseCase(mockRepository)
    }

    // GetRandomWordsUseCase Tests

    /**
     * Test GetRandomWordsUseCase returns correct count.
     */
    @Test
    fun getRandomWordsUseCase_ReturnsCorrectCount() = runTest {
        // Arrange
        val expectedWords = testWords.shuffled().take(5)
        whenever(mockRepository.getRandomWords(5)).thenReturn(expectedWords)

        // Act
        val result = getRandomWordsUseCase(5)

        // Assert
        assertEquals(5, result.size)
        assertEquals(expectedWords, result)
    }

    /**
     * Test GetRandomWordsUseCase with count of 1.
     */
    @Test
    fun getRandomWordsUseCase_WithCountOne_ReturnsSingleWord() = runTest {
        // Arrange
        val expectedWords = listOf("apple")
        whenever(mockRepository.getRandomWords(1)).thenReturn(expectedWords)

        // Act
        val result = getRandomWordsUseCase(1)

        // Assert
        assertEquals(1, result.size)
        assertEquals("apple", result[0])
    }

    /**
     * Test GetRandomWordsUseCase with large count.
     */
    @Test
    fun getRandomWordsUseCase_WithLargeCount_ReturnsAllAvailable() = runTest {
        // Arrange
        whenever(mockRepository.getRandomWords(100)).thenReturn(testWords)

        // Act
        val result = getRandomWordsUseCase(100)

        // Assert
        assertEquals(testWords.size, result.size)
    }

    /**
     * Test GetRandomWordsUseCase returns no duplicates.
     */
    @Test
    fun getRandomWordsUseCase_ReturnedWordsAreUnique() = runTest {
        // Arrange
        val expectedWords = testWords.shuffled().take(7)
        whenever(mockRepository.getRandomWords(7)).thenReturn(expectedWords)

        // Act
        val result = getRandomWordsUseCase(7)

        // Assert
        assertEquals(result.size, result.distinct().size)
    }

    // GetAllWordsUseCase Tests

    /**
     * Test GetAllWordsUseCase returns all words.
     */
    @Test
    fun getAllWordsUseCase_ReturnsAllWords() = runTest {
        // Arrange
        whenever(mockRepository.getAllWords()).thenReturn(testWords)

        // Act
        val result = getAllWordsUseCase()

        // Assert
        assertEquals(testWords.size, result.size)
        assertEquals(testWords, result)
    }

    /**
     * Test GetAllWordsUseCase preserves order.
     */
    @Test
    fun getAllWordsUseCase_PreservesWordOrder() = runTest {
        // Arrange
        whenever(mockRepository.getAllWords()).thenReturn(testWords)

        // Act
        val result = getAllWordsUseCase()

        // Assert
        assertTrue(result.containsAll(testWords))
        for (i in testWords.indices) {
            assertEquals(testWords[i], result[i])
        }
    }

    // GetRandomWordUseCase Tests

    /**
     * Test GetRandomWordUseCase returns single word.
     */
    @Test
    fun getRandomWordUseCase_ReturnsSingleWord() = runTest {
        // Arrange
        whenever(mockRepository.getRandomWord()).thenReturn("apple")

        // Act
        val result = getRandomWordUseCase()

        // Assert
        assertNotNull(result)
        assertEquals("apple", result)
    }

    /**
     * Test GetRandomWordUseCase returns valid word.
     */
    @Test
    fun getRandomWordUseCase_ReturnsValidWord() = runTest {
        // Arrange
        whenever(mockRepository.getRandomWord()).thenReturn("banana")

        // Act
        val result = getRandomWordUseCase()

        // Assert
        assertTrue(testWords.contains(result))
    }

    /**
     * Test multiple calls to GetRandomWordUseCase.
     */
    @Test
    fun getRandomWordUseCase_MultipleCalls_ReturnDifferentWords() = runTest {
        // Arrange
        whenever(mockRepository.getRandomWord())
            .thenReturn("apple")
            .thenReturn("banana")
            .thenReturn("cherry")

        // Act
        val word1 = getRandomWordUseCase()
        val word2 = getRandomWordUseCase()
        val word3 = getRandomWordUseCase()

        // Assert
        assertEquals("apple", word1)
        assertEquals("banana", word2)
        assertEquals("cherry", word3)
    }
}
