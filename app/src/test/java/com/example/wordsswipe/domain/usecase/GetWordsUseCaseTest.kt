package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.domain.model.Word
import com.example.wordsswipe.domain.repository.WordRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for GetWordsUseCase.
 *
 * Tests verify:
 * - UseCase properly delegates to repository
 * - Flow is properly returned
 * - No additional transformations occur
 */
class GetWordsUseCaseTest {

    @Mock
    private lateinit var wordRepository: WordRepository

    private lateinit var getWordsUseCase: GetWordsUseCase

    private val testWords = listOf(
        Word(
            id = 1,
            text = "Test Word",
            definition = "Test definition",
            example = "Test example",
            partOfSpeech = "Noun",
            difficultyLevel = 1
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getWordsUseCase = GetWordsUseCase(wordRepository)
    }

    /**
     * Test that UseCase correctly invokes repository.
     */
    @Test
    fun invoke_CallsRepository_ReturnsFlow() = runTest {
        // Arrange
        whenever(wordRepository.getAllWords()).thenReturn(flowOf(testWords))

        // Act
        val result = getWordsUseCase()

        // Assert
        var emittedValue: List<Word> = emptyList()
        result.collect { words ->
            emittedValue = words
        }

        assertEquals(testWords, emittedValue)
    }

    /**
     * Test operator invoke() syntax works correctly.
     */
    @Test
    fun operatorInvoke_Works() = runTest {
        // Arrange
        whenever(wordRepository.getAllWords()).thenReturn(flowOf(testWords))

        // Act - Use operator syntax
        val result = getWordsUseCase()  // Equivalent to invoke()

        // Assert
        var emittedValue: List<Word> = emptyList()
        result.collect { words ->
            emittedValue = words
        }

        assertEquals(testWords, emittedValue)
    }

    /**
     * Test empty list handling.
     */
    @Test
    fun invoke_EmptyList_ReturnsEmptyFlow() = runTest {
        // Arrange
        whenever(wordRepository.getAllWords()).thenReturn(flowOf(emptyList()))

        // Act
        val result = getWordsUseCase()

        // Assert
        var emittedValue: List<Word> = emptyList()
        result.collect { words ->
            emittedValue = words
        }

        assertEquals(emptyList<Word>(), emittedValue)
    }
}
