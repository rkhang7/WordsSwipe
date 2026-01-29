package com.example.wordsswipe.domain.repository

import com.example.wordsswipe.domain.model.Word
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining the contract for word data operations.
 * This is part of the domain layer and doesn't depend on any specific data source implementation.
 */
interface WordRepository {
    /**
     * Retrieves all words as a stream.
     * Using Flow for reactive updates and efficient data handling.
     */
    fun getAllWords(): Flow<List<Word>>
}
