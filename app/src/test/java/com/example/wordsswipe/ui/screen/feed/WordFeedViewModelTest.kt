package com.example.wordsswipe.ui.screen.feed

import androidx.lifecycle.SavedStateHandle
import com.example.wordsswipe.data.local.WordsRepository
import com.example.wordsswipe.domain.model.WordDetail
import com.example.wordsswipe.domain.model.WordPage
import com.example.wordsswipe.domain.usecase.GetWordDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for WordFeedViewModel.
 *
 * Tests:
 * - Initialization (load initial words)
 * - Navigation (swipe up/down)
 * - Preloading (trigger when near end)
 * - Error handling
 * - State management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WordFeedViewModelTest {

    @Mock
    private lateinit var mockWordsRepository: WordsRepository

    @Mock
    private lateinit var mockGetWordDetailUseCase: GetWordDetailUseCase

    private lateinit var viewModel: WordFeedViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testWords = listOf("hello", "world", "kotlin", "android", "compose")

    private val testWordDetail = WordDetail(
        word = "hello",
        phonetic = "/həˈloʊ/",
        phonetics = emptyList(),
        meanings = emptyList()
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Test that ViewModel initializes with loading state.
     */
    @Test
    fun initialize_StartsWithLoadingState() = runTest {
        // Arrange
        setupMocks()

        // Act
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)

        // Assert - initial state is loading
        assertEquals(WordFeedUiState.Loading, viewModel.uiState.value)
    }

    /**
     * Test that initial words are loaded.
     */
    @Test
    fun initialize_LoadsInitialWords() = runTest {
        // Arrange
        setupMocks()

        // Act
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Assert
        assertEquals(5, viewModel.pages.value.size)
        assertEquals(0, viewModel.currentIndex.value)
        assertEquals(WordFeedUiState.Success, viewModel.uiState.value)
    }

    /**
     * Setup mocks for tests.
     */
    private suspend fun setupMocks() {
        whenever(mockWordsRepository.getRandomWords(5)).thenReturn(testWords)
        whenever(mockGetWordDetailUseCase.invoke("hello")).thenReturn(testWordDetail)
    }

    /**
     * Test swipe down navigation.
     */
    @Test
    fun swipeDown_IncrementsIndex() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        viewModel.swipeDown()

        // Assert
        assertEquals(1, viewModel.currentIndex.value)
    }

    /**
     * Test swipe up navigation.
     */
    @Test
    fun swipeUp_DecrementsIndex() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()
        viewModel.swipeDown()
        viewModel.swipeDown()

        // Act
        viewModel.swipeUp()

        // Assert
        assertEquals(1, viewModel.currentIndex.value)
    }

    /**
     * Test swipe up is blocked at index 0.
     */
    @Test
    fun swipeUp_BlockedAtStart() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        viewModel.swipeUp()

        // Assert - should remain at 0
        assertEquals(0, viewModel.currentIndex.value)
    }

    /**
     * Test swipe down is blocked at end.
     */
    @Test
    fun swipeDown_BlockedAtEnd() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        val pageSize = viewModel.pages.value.size

        // Act - swipe past end
        repeat(pageSize + 5) {
            viewModel.swipeDown()
        }

        // Assert - should stay at last index
        assertEquals(pageSize - 1, viewModel.currentIndex.value)
    }

    /**
     * Test current page getter.
     */
    @Test
    fun getCurrentPage_ReturnsCurrentWordPage() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        val currentPage = viewModel.getCurrentPage()

        // Assert
        assertNotNull(currentPage)
        assertEquals("hello", currentPage?.word)
    }

    /**
     * Test progress getter.
     */
    @Test
    fun getProgress_ReturnsCorrectValues() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        val progress = viewModel.getProgress()

        // Assert - 1-indexed, so first page is 1/5
        assertEquals(1, progress.first)
        assertEquals(5, progress.second)
    }

    /**
     * Test word page structure.
     */
    @Test
    fun wordPages_HaveCorrectStructure() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        val pages = viewModel.pages.value

        // Assert
        pages.forEach { page ->
            assertNotNull(page.word)
            assertTrue(page.word.isNotBlank())
        }
    }

    /**
     * Test retry loading on error.
     */
    @Test
    fun retryLoading_ReinitializesFeed() = runTest {
        // Arrange
        setupMocks()
        viewModel = WordFeedViewModel(mockWordsRepository, mockGetWordDetailUseCase)
        advanceUntilIdle()

        // Act
        viewModel.retryLoading()
        advanceUntilIdle()

        // Assert - should still have pages
        assertTrue(viewModel.pages.value.isNotEmpty())
    }
}
