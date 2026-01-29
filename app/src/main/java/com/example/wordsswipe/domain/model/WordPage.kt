package com.example.wordsswipe.domain.model

/**
 * Represents a single word page in the feed.
 * Contains both the word and its complete API details.
 */
data class WordPage(
    val word: String,
    val wordDetail: WordDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
