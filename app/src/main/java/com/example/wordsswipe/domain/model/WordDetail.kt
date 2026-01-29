package com.example.wordsswipe.domain.model

/**
 * Domain model for word details from the Dictionary API.
 * Clean model independent of API response structure.
 */
data class WordDetail(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<Phonetic> = emptyList(),
    val meanings: List<Meaning> = emptyList()
)

/**
 * Phonetic information for pronunciation.
 */
data class Phonetic(
    val text: String? = null,
    val audio: String? = null
)

/**
 * Meaning of a word for a specific part of speech.
 */
data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)

/**
 * Definition with optional example and synonyms.
 */
data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList()
)
