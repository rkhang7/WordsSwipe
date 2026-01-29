package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.domain.model.Word
import com.example.wordsswipe.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving all English words.
 * This encapsulates business logic and is used by ViewModels.
 * Following the single responsibility principle - this class has one reason to change.
 */
class GetWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<List<Word>> = wordRepository.getAllWords()
}
