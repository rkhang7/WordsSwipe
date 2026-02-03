package com.example.wordsswipe.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Complete word detail response from Dictionary API.
 * Represents all information available for a word.
 */
data class WordDetailResponse(
    @SerializedName("word")
    val word: String,

    @SerializedName("phonetic")
    val phonetic: String? = null,

    @SerializedName("phonetics")
    val phonetics: List<PhoneticDto> = emptyList(),

    @SerializedName("meanings")
    val meanings: List<MeaningDto> = emptyList(),

    @SerializedName("license")
    val license: LicenseDto? = null,

    @SerializedName("sourceUrls")
    val sourceUrls: List<String> = emptyList()
)

/**
 * Phonetic information for a word.
 * Contains text representation and optional audio URL.
 */
data class PhoneticDto(
    @SerializedName("text")
    val text: String? = null,

    @SerializedName("audio")
    val audio: String? = null
)

/**
 * Meaning of a word for a specific part of speech.
 * Contains definitions, examples, synonyms, and antonyms.
 */
data class MeaningDto(
    @SerializedName("partOfSpeech")
    val partOfSpeech: String,

    @SerializedName("definitions")
    val definitions: List<DefinitionDto> = emptyList(),

    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList(),

    @SerializedName("antonyms")
    val antonyms: List<String> = emptyList()
)

/**
 * Definition of a word with optional example and synonyms.
 */
data class DefinitionDto(
    @SerializedName("definition")
    val definition: String,

    @SerializedName("example")
    val example: String? = null,

    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList()
)

/**
 * License information for the word data.
 */
data class LicenseDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String
)
