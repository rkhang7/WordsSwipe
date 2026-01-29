package com.example.wordsswipe.data.source

import com.example.wordsswipe.domain.model.Word
import javax.inject.Inject

/**
 * Local data source providing mock English words for learning.
 * In a production app, this could be replaced with Room Database or API calls.
 */
class LocalDataSource @Inject constructor() {

    fun getWords(): List<Word> = listOf(
        Word(
            id = 1,
            text = "Serendipity",
            definition = "The occurrence of events by chance in a happy or beneficial way",
            example = "Finding that old friend by serendipity made my day",
            partOfSpeech = "Noun",
            difficultyLevel = 4
        ),
        Word(
            id = 2,
            text = "Ephemeral",
            definition = "Lasting for a very short time",
            example = "The beauty of cherry blossoms is ephemeral, lasting only a few weeks",
            partOfSpeech = "Adjective",
            difficultyLevel = 4
        ),
        Word(
            id = 3,
            text = "Ubiquitous",
            definition = "Present, appearing, or found everywhere",
            example = "Smartphones have become ubiquitous in modern society",
            partOfSpeech = "Adjective",
            difficultyLevel = 4
        ),
        Word(
            id = 4,
            text = "Melancholy",
            definition = "A feeling of pensive sadness, typically with no obvious cause",
            example = "A sense of melancholy overcame her as she looked at old photographs",
            partOfSpeech = "Noun/Adjective",
            difficultyLevel = 3
        ),
        Word(
            id = 5,
            text = "Pragmatic",
            definition = "Dealing with things in a practical, realistic way based on actual circumstances",
            example = "We need a pragmatic approach to solve this complex problem",
            partOfSpeech = "Adjective",
            difficultyLevel = 3
        ),
        Word(
            id = 6,
            text = "Obfuscate",
            definition = "To deliberately make something unclear or hard to understand",
            example = "The company tried to obfuscate the truth with confusing statements",
            partOfSpeech = "Verb",
            difficultyLevel = 4
        ),
        Word(
            id = 7,
            text = "Languish",
            definition = "To be or remain in a state of deprivation or weakness",
            example = "The prisoner began to languish in solitary confinement",
            partOfSpeech = "Verb",
            difficultyLevel = 3
        ),
        Word(
            id = 8,
            text = "Perspicacious",
            definition = "Having keen insight, discernment, or understanding",
            example = "Her perspicacious analysis of the situation impressed everyone",
            partOfSpeech = "Adjective",
            difficultyLevel = 5
        ),
        Word(
            id = 9,
            text = "Benevolent",
            definition = "Characterized by or expressing goodwill or kindly feelings",
            example = "The benevolent donor funded the entire community center",
            partOfSpeech = "Adjective",
            difficultyLevel = 3
        ),
        Word(
            id = 10,
            text = "Eloquent",
            definition = "Fluent, persuasive, and expressive in speaking or writing",
            example = "The speaker delivered an eloquent speech that moved the audience",
            partOfSpeech = "Adjective",
            difficultyLevel = 3
        )
    )
}
