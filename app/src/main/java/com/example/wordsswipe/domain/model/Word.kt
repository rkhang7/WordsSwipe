package com.example.wordsswipe.domain.model

/**
 * Domain model for an English word with its definition and example usage.
 * This represents the core business entity of the application.
 */
data class Word(
    val id: Int,
    val text: String,
    val definition: String,
    val example: String,
    val partOfSpeech: String,
    val difficultyLevel: Int // 1-5, where 1 is easiest
)
