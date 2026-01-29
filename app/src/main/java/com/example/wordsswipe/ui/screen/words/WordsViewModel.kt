package com.example.wordsswipe.ui.screen.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordsswipe.domain.usecase.GetWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Words Screen using unidirectional data flow pattern.
 * - State is exposed via StateFlow (immutable to UI)
 * - Events are exposed via SharedFlow (one-time notifications)
 * - All business logic is kept in UseCase layer
 * - Composables remain pure and testable
 */
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase
) : ViewModel() {

    // State management - StateFlow for continuous state updates
    private val _uiState = MutableStateFlow<WordsUiState>(WordsUiState.Loading)
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    // Event management - SharedFlow for one-time events
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        loadWords()
    }

    /**
     * Loads words from the use case and updates UI state.
     * Uses coroutines for proper lifecycle management.
     */
    private fun loadWords() {
        viewModelScope.launch {
            getWordsUseCase()
                .onStart {
                    _uiState.value = WordsUiState.Loading
                }
                .catch { throwable ->
                    val errorMessage = throwable.message ?: "Unknown error occurred"
                    _uiState.value = WordsUiState.Error(errorMessage)
                    _errorEvent.emit(errorMessage)
                }
                .collect { words ->
                    _uiState.value = WordsUiState.Success(words)
                }
        }
    }
}
