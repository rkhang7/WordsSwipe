package com.example.wordsswipe.data.repository

import com.example.wordsswipe.data.source.LocalDataSource
import com.example.wordsswipe.domain.model.Word
import com.example.wordsswipe.domain.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Implementation of WordRepository that uses LocalDataSource.
 * This connects the data layer with the domain layer.
 * Using Flow for reactive data handling and proper coroutine context switching.
 */
class WordRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : WordRepository {

    override fun getAllWords(): Flow<List<Word>> = flow {
        // Simulate network/database delay
        delay(500)
        emit(localDataSource.getWords())
    }.flowOn(Dispatchers.IO)
}
