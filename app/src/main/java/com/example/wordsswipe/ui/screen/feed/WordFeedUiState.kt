package com.example.wordsswipe.ui.screen.feed

/**
 * UI state for the word feed screen.
 * Represents different states of the feed as a whole.
 */
sealed class WordFeedUiState {
    /**
     * Initial state - loading the first batch of words.
     */
    data object Loading : WordFeedUiState()

    /**
     * Successfully loaded and ready to display words.
     * Pages are available and can be displayed.
     */
    data object Success : WordFeedUiState()

    /**
     * Error occurred while loading words.
     * @param message Error message to display to user
     */
    data class Error(val message: String) : WordFeedUiState()
}
