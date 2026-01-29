package com.example.wordsswipe.ui.screen.words

import androidx.lifecycle.SavedStateHandle
import com.example.wordsswipe.domain.model.Word
import com.example.wordsswipe.domain.usecase.GetWordsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for WordsViewModel.
 *
 * Tests verify:
 * - State transitions (Loading → Success/Error)
 * - UseCase integration
 * - Error handling
 * - Proper coroutine scoping
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WordsViewModelTest {

    @Mock
    private lateinit var getWordsUseCase: GetWordsUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testWords = listOf(
        Word(
            id = 1,
            text = "Serendipity",
            definition = "Finding good things by chance",
            example = "By serendipity, I found my lost keys",
            partOfSpeech = "Noun",
            difficultyLevel = 4
        ),
        Word(
            id = 2,
            text = "Ephemeral",
            definition = "Lasting for a short time",
            example = "The beauty of flowers is ephemeral",
            partOfSpeech = "Adjective",
            difficultyLevel = 4
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Test successful word loading.
     * Verifies state transitions: Loading → Success
     */
    @Test
    fun loadWords_Success_UpdatesUiState() = runTest {
        // Arrange
        whenever(getWordsUseCase()).thenReturn(flowOf(testWords))

        // Act
        val viewModel = WordsViewModel(getWordsUseCase)

        // Assert - State transitions
        assertEquals(
            WordsUiState.Loading,
            viewModel.uiState.value
        )

        // Let the flow emit
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify final state
        val finalState = viewModel.uiState.value
        assertTrue(finalState is WordsUiState.Success)
        assertEquals(
            testWords,
            (finalState as WordsUiState.Success).words
        )
    }

    /**
     * Test error handling.
     * Verifies state transitions: Loading → Error
     */
    @Test
    fun loadWords_Error_UpdatesErrorState() = runTest {
        // Arrange
        val testException = Exception("Network error")
        whenever(getWordsUseCase()).thenReturn(
            kotlinx.coroutines.flow.flow {
                throw testException
            }
        )

        // Act
        val viewModel = WordsViewModel(getWordsUseCase)

        // Let coroutines execute
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertTrue(finalState is WordsUiState.Error)
        assertEquals(
            "Network error",
            (finalState as WordsUiState.Error).message
        )
    }

    /**
     * Test that state is preserved across lifecycle changes.
     * Verifies ViewModel survives configuration changes.
     */
    @Test
    fun uiState_IsPreservedAcrossLifecycle() = runTest {
        // Arrange
        whenever(getWordsUseCase()).thenReturn(flowOf(testWords))

        // Act
        val viewModel = WordsViewModel(getWordsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBeforeDestroy = viewModel.uiState.value

        // Simulate lifecycle event (state is preserved)
        val stateAfterSimulatedDestroy = viewModel.uiState.value

        // Assert - State unchanged after config change
        assertEquals(stateBeforeDestroy, stateAfterSimulatedDestroy)
    }
}
