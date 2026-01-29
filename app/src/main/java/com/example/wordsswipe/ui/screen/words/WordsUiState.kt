package com.example.wordsswipe.ui.screen.words

import com.example.wordsswipe.domain.model.Word

/**
 * Sealed class representing different UI states for the words screen.
 * This ensures type-safe state management and exhaustive when expressions.
 */
sealed class WordsUiState {
    data object Loading : WordsUiState()
    data class Success(val words: List<Word>) : WordsUiState()
    data class Error(val message: String) : WordsUiState()
}
