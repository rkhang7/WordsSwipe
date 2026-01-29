package com.example.wordsswipe.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Complete word detail response from Dictionary API.
 * Represents all information available for a word.
 */
@JsonClass(generateAdapter = true)
data class WordDetailResponse(
    @Json(name = "word")
    val word: String,

    @Json(name = "phonetic")
    val phonetic: String? = null,

    @Json(name = "phonetics")
    val phonetics: List<PhoneticDto> = emptyList(),

    @Json(name = "meanings")
    val meanings: List<MeaningDto> = emptyList(),

    @Json(name = "license")
    val license: LicenseDto? = null,

    @Json(name = "sourceUrls")
    val sourceUrls: List<String> = emptyList()
)

/**
 * Phonetic information for a word.
 * Contains text representation and optional audio URL.
 */
@JsonClass(generateAdapter = true)
data class PhoneticDto(
    @Json(name = "text")
    val text: String? = null,

    @Json(name = "audio")
    val audio: String? = null
)

/**
 * Meaning of a word for a specific part of speech.
 * Contains definitions, examples, synonyms, and antonyms.
 */
@JsonClass(generateAdapter = true)
data class MeaningDto(
    @Json(name = "partOfSpeech")
    val partOfSpeech: String,

    @Json(name = "definitions")
    val definitions: List<DefinitionDto> = emptyList(),

    @Json(name = "synonyms")
    val synonyms: List<String> = emptyList(),

    @Json(name = "antonyms")
    val antonyms: List<String> = emptyList()
)

/**
 * Definition of a word with optional example and synonyms.
 */
@JsonClass(generateAdapter = true)
data class DefinitionDto(
    @Json(name = "definition")
    val definition: String,

    @Json(name = "example")
    val example: String? = null,

    @Json(name = "synonyms")
    val synonyms: List<String> = emptyList()
)

/**
 * License information for the word data.
 */
@JsonClass(generateAdapter = true)
data class LicenseDto(
    @Json(name = "name")
    val name: String,

    @Json(name = "url")
    val url: String
)
