package com.example.wordsswipe.ui.screen.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordsswipe.data.local.WordsRepository
import com.example.wordsswipe.data.remote.repository.DictionaryApiException
import com.example.wordsswipe.data.remote.repository.WordNotFoundException
import com.example.wordsswipe.domain.model.WordPage
import com.example.wordsswipe.domain.usecase.GetWordDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the word feed screen.
 *
 * Responsibilities:
 * - Manage word feed state (pages, current index, UI state)
 * - Handle navigation (swipe up/down)
 * - Preload words in parallel when approaching end of list
 * - Manage error states and retry logic
 * - Handle app lifecycle (clear history on launch)
 */
@HiltViewModel
class WordFeedViewModel @Inject constructor(
    private val wordsRepository: WordsRepository,
    private val getWordDetailUseCase: GetWordDetailUseCase
) : ViewModel() {

    // ============================================================================
    // STATE FLOWS (Observable State)
    // ============================================================================

    private val _pages = MutableStateFlow<List<WordPage>>(emptyList())
    val pages: StateFlow<List<WordPage>> = _pages.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _uiState = MutableStateFlow<WordFeedUiState>(WordFeedUiState.Loading)
    val uiState: StateFlow<WordFeedUiState> = _uiState.asStateFlow()

    // ============================================================================
    // SHARED FLOWS (One-time Events)
    // ============================================================================

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _retryEvent = MutableSharedFlow<Unit>()
    val retryEvent: SharedFlow<Unit> = _retryEvent.asSharedFlow()

    // ============================================================================
    // PRELOAD CONFIGURATION
    // ============================================================================

    companion object {
        private const val INITIAL_WORDS_COUNT = 5
        private const val PRELOAD_BATCH_SIZE = 5
        private const val PRELOAD_THRESHOLD = 2  // Load more when 2 items from end
    }

    // ============================================================================
    // INITIALIZATION
    // ============================================================================

    init {
        initializeFeed()
    }

    /**
     * Initializes the word feed on app launch.
     *
     * - Clears any existing history
     * - Loads initial batch of random words
     * - Fetches API details for each word in parallel
     * - Sets UI state based on result
     */
    private fun initializeFeed() {
        viewModelScope.launch {
            try {
                _uiState.value = WordFeedUiState.Loading

                // Clear history
                _currentIndex.value = 0
                _pages.value = emptyList()

                // Load initial batch
                val randomWords = wordsRepository.getRandomWords(INITIAL_WORDS_COUNT)
                val wordPages = createWordPages(randomWords)

                _pages.value = wordPages
                _uiState.value = if (wordPages.isEmpty()) {
                    WordFeedUiState.Error("No words available")
                } else {
                    WordFeedUiState.Success
                }

                // Fetch API details for all initial pages
                fetchDetailsForPages(wordPages.indices.toList())

            } catch (e: Exception) {
                handleError("Failed to initialize feed: ${e.message}")
                _uiState.value = WordFeedUiState.Error("Failed to load words")
            }
        }
    }

    // ============================================================================
    // NAVIGATION
    // ============================================================================

    /**
     * Swipe down to next word page.
     * Triggers preload when approaching end of list.
     */
    fun swipeDown() {
        val currentPages = _pages.value
        val nextIndex = _currentIndex.value + 1

        if (nextIndex < currentPages.size) {
            _currentIndex.value = nextIndex

            // Check if we need to preload more words
            if (shouldPreload(nextIndex)) {
                preloadNextBatch()
            }
        }
    }

    /**
     * Swipe up to previous word page.
     * Only allowed if currentIndex > 0.
     */
    fun swipeUp() {
        val previousIndex = _currentIndex.value - 1
        if (previousIndex >= 0) {
            _currentIndex.value = previousIndex
        }
    }

    // ============================================================================
    // PRELOAD LOGIC
    // ============================================================================

    /**
     * Determines if preloading should be triggered.
     * Triggers when user is within PRELOAD_THRESHOLD items from end of list.
     */
    private fun shouldPreload(index: Int): Boolean {
        val currentPages = _pages.value
        val remainingItems = currentPages.size - index
        return remainingItems <= PRELOAD_THRESHOLD
    }

    /**
     * Preloads the next batch of random words.
     *
     * Process:
     * 1. Load PRELOAD_BATCH_SIZE random words from repository
     * 2. Create WordPage objects (with loading state)
     * 3. Add to pages list
     * 4. Fetch API details for new pages in parallel
     */
    private fun preloadNextBatch() {
        viewModelScope.launch {
            try {
                // Load random words
                val newRandomWords = wordsRepository.getRandomWords(PRELOAD_BATCH_SIZE)
                val newPages = createWordPages(newRandomWords)

                // Add to existing pages
                val currentPages = _pages.value.toMutableList()
                val insertIndex = currentPages.size
                currentPages.addAll(newPages)
                _pages.value = currentPages

                // Fetch details for new pages in parallel
                val newPageIndices = (insertIndex until currentPages.size).toList()
                fetchDetailsForPages(newPageIndices)

            } catch (e: Exception) {
                handleError("Failed to preload words: ${e.message}")
            }
        }
    }

    // ============================================================================
    // DATA FETCHING
    // ============================================================================

    /**
     * Fetches API details for specified pages in parallel.
     *
     * For each page:
     * - Launches a coroutine to fetch from API
     * - Updates page with result or error
     * - Does NOT block other pages
     */
    private suspend fun fetchDetailsForPages(indices: List<Int>) {
        indices.forEach { index ->
            viewModelScope.launch {
                try {
                    val page = _pages.value.getOrNull(index) ?: return@launch
                    val wordDetail = getWordDetailUseCase(page.word)

                    // Update page with fetched details
                    updatePage(index) { currentPage ->
                        currentPage.copy(
                            wordDetail = wordDetail,
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (e: WordNotFoundException) {
                    handlePageError(index, "Word not found: ${e.message}")
                } catch (e: DictionaryApiException) {
                    handlePageError(index, "Failed to fetch: ${e.message}")
                } catch (e: IllegalArgumentException) {
                    handlePageError(index, "Invalid word: ${e.message}")
                } catch (e: Exception) {
                    handlePageError(index, "Error: ${e.message}")
                }
            }
        }
    }

    /**
     * Updates a specific page with transformation.
     */
    private fun updatePage(index: Int, transform: (WordPage) -> WordPage) {
        val currentPages = _pages.value.toMutableList()
        if (index in currentPages.indices) {
            currentPages[index] = transform(currentPages[index])
            _pages.value = currentPages
        }
    }

    /**
     * Handles error for a specific page.
     */
    private fun handlePageError(index: Int, errorMessage: String) {
        updatePage(index) { page ->
            page.copy(
                isLoading = false,
                error = errorMessage
            )
        }
        // Emit error event for UI to show
        viewModelScope.launch {
            _errorEvent.emit(errorMessage)
        }
    }

    // ============================================================================
    // ERROR HANDLING
    // ============================================================================

    /**
     * Handles generic error and emits event.
     */
    private fun handleError(message: String) {
        viewModelScope.launch {
            _errorEvent.emit(message)
        }
    }

    /**
     * Retry loading initial feed or preload.
     */
    fun retryLoading() {
        val uiState = _uiState.value
        if (uiState is WordFeedUiState.Error) {
            // Retry from beginning
            initializeFeed()
        }

        viewModelScope.launch {
            _retryEvent.emit(Unit)
        }
    }

    /**
     * Retry fetching details for a specific page.
     */
    fun retryPageLoading(index: Int) {
        viewModelScope.launch {
            try {
                val page = _pages.value.getOrNull(index) ?: return@launch
                updatePage(index) { it.copy(isLoading = true, error = null) }
                fetchDetailsForPages(listOf(index))
            } catch (e: Exception) {
                handlePageError(index, "Failed to retry: ${e.message}")
            }
        }
    }

    // ============================================================================
    // HELPER FUNCTIONS
    // ============================================================================

    /**
     * Creates WordPage objects from word strings.
     * Pages start in loading state until API is fetched.
     */
    private fun createWordPages(words: List<String>): List<WordPage> {
        return words.map { word ->
            WordPage(
                word = word,
                wordDetail = null,
                isLoading = true,
                error = null
            )
        }
    }

    /**
     * Gets the current word page being displayed.
     */
    fun getCurrentPage(): WordPage? {
        val index = _currentIndex.value
        return _pages.value.getOrNull(index)
    }

    /**
     * Gets progress information for the feed.
     */
    fun getProgress(): Pair<Int, Int> {
        return Pair(_currentIndex.value + 1, _pages.value.size)
    }
}
