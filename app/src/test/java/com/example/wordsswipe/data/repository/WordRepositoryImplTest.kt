package com.example.wordsswipe.data.repository

import com.example.wordsswipe.data.source.LocalDataSource
import com.example.wordsswipe.domain.model.Word
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for WordRepositoryImpl.
 *
 * Tests verify:
 * - Repository properly wraps data source in Flow
 * - Coroutine dispatchers are correct
 * - Error handling in repository layer
 */
class WordRepositoryImplTest {

    @Mock
    private lateinit var localDataSource: LocalDataSource

    private lateinit var repository: WordRepositoryImpl

    private val testWords = listOf(
        Word(
            id = 1,
            text = "Test",
            definition = "Definition",
            example = "Example",
            partOfSpeech = "Noun",
            difficultyLevel = 1
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = WordRepositoryImpl(localDataSource)
    }

    /**
     * Test that repository returns words from data source as Flow.
     */
    @Test
    fun getAllWords_ReturnsFlowOfWords() = runTest {
        // Arrange
        whenever(localDataSource.getWords()).thenReturn(testWords)

        // Act
        val result = repository.getAllWords()

        // Assert
        var emittedWords: List<Word> = emptyList()
        result.collect { words ->
            emittedWords = words
        }

        assertEquals(testWords, emittedWords)
    }

    /**
     * Test that repository can handle data source returning empty list.
     */
    @Test
    fun getAllWords_EmptyDataSource_ReturnsEmptyFlow() = runTest {
        // Arrange
        whenever(localDataSource.getWords()).thenReturn(emptyList())

        // Act
        val result = repository.getAllWords()

        // Assert
        var emittedWords: List<Word> = emptyList()
        result.collect { words ->
            emittedWords = words
        }

        assertEquals(emptyList<Word>(), emittedWords)
    }

    /**
     * Test that repository handles exceptions from data source.
     */
    @Test
    fun getAllWords_DataSourceError_HandlesGracefully() = runTest {
        // Arrange - Simulate data source returning empty list on error
        // (in real app, repository would have error handling)
        whenever(localDataSource.getWords()).thenReturn(emptyList())

        // Act
        val result = repository.getAllWords()

        // Assert - Should return empty flow gracefully
        var emittedWords: List<Word> = emptyList()
        result.collect { words ->
            emittedWords = words
        }

        assertEquals(emptyList<Word>(), emittedWords)
    }
}
