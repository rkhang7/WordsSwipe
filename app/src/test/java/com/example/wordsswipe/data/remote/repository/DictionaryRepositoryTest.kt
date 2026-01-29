package com.example.wordsswipe.data.remote.repository

import com.example.wordsswipe.data.remote.api.DictionaryApi
import com.example.wordsswipe.data.remote.model.DefinitionDto
import com.example.wordsswipe.data.remote.model.MeaningDto
import com.example.wordsswipe.data.remote.model.PhoneticDto
import com.example.wordsswipe.data.remote.model.WordDetailResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

/**
 * Unit tests for DictionaryRepository.
 *
 * Tests:
 * - Successful API responses
 * - Error handling (404, 5xx, network)
 * - Empty responses
 * - DTO to domain model mapping
 * - Input validation
 */
class DictionaryRepositoryTest {

    @Mock
    private lateinit var mockApi: DictionaryApi

    private lateinit var repository: DictionaryRepository

    private val testWord = "hello"

    private val testWordDetail = WordDetailResponse(
        word = "hello",
        phonetic = "/həˈloʊ/",
        phonetics = listOf(
            PhoneticDto(
                text = "/həˈloʊ/",
                audio = "https://example.com/audio.mp3"
            )
        ),
        meanings = listOf(
            MeaningDto(
                partOfSpeech = "interjection",
                definitions = listOf(
                    DefinitionDto(
                        definition = "Used as a greeting",
                        example = "Hello, how are you?",
                        synonyms = listOf("hi", "hey")
                    )
                ),
                synonyms = listOf("hi"),
                antonyms = emptyList()
            )
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = DictionaryRepository(mockApi)
    }

    /**
     * Test successful word lookup.
     */
    @Test
    fun getWordDetail_Success_ReturnsWordDetail() = runTest {
        // Arrange
        val response = Response.success(listOf(testWordDetail))
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act
        val result = repository.getWordDetail(testWord)

        // Assert
        assertNotNull(result)
        assertEquals("hello", result.word)
        assertEquals("/həˈloʊ/", result.phonetic)
        assertEquals(1, result.phonetics.size)
        assertEquals(1, result.meanings.size)
    }

    /**
     * Test phonetics mapping.
     */
    @Test
    fun getWordDetail_MapsPhoneticsProperly() = runTest {
        // Arrange
        val response = Response.success(listOf(testWordDetail))
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act
        val result = repository.getWordDetail(testWord)

        // Assert
        assertEquals(1, result.phonetics.size)
        val phonetic = result.phonetics[0]
        assertEquals("/həˈloʊ/", phonetic.text)
        assertEquals("https://example.com/audio.mp3", phonetic.audio)
    }

    /**
     * Test meanings and definitions mapping.
     */
    @Test
    fun getWordDetail_MapsMeaningsAndDefinitions() = runTest {
        // Arrange
        val response = Response.success(listOf(testWordDetail))
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act
        val result = repository.getWordDetail(testWord)

        // Assert
        assertEquals(1, result.meanings.size)
        val meaning = result.meanings[0]
        assertEquals("interjection", meaning.partOfSpeech)
        assertEquals(1, meaning.definitions.size)

        val definition = meaning.definitions[0]
        assertEquals("Used as a greeting", definition.definition)
        assertEquals("Hello, how are you?", definition.example)
        assertTrue(definition.synonyms.contains("hi"))
    }

    /**
     * Test word not found error (404).
     */
    @Test(expected = WordNotFoundException::class)
    fun getWordDetail_NotFound_ThrowsWordNotFoundException() = runTest {
        // Arrange
        val response: Response<List<WordDetailResponse>> = Response.error(
            404,
            "".toResponseBody()
        )
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act & Assert
        repository.getWordDetail(testWord)
    }

    /**
     * Test HTTP error (5xx).
     */
    @Test(expected = DictionaryApiException::class)
    fun getWordDetail_ServerError_ThrowsDictionaryApiException() = runTest {
        // Arrange
        val response: Response<List<WordDetailResponse>> = Response.error(
            500,
            "Server error".toResponseBody()
        )
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act & Assert
        repository.getWordDetail(testWord)
    }

    /**
     * Test empty response.
     */
    @Test(expected = DictionaryApiException::class)
    fun getWordDetail_EmptyResponse_ThrowsDictionaryApiException() = runTest {
        // Arrange
        val response = Response.success(emptyList<WordDetailResponse>())
        whenever(mockApi.getWordDetail(testWord)).thenReturn(response)

        // Act & Assert
        repository.getWordDetail(testWord)
    }

    /**
     * Test blank word validation.
     */
    @Test(expected = IllegalArgumentException::class)
    fun getWordDetail_BlankWord_ThrowsIllegalArgumentException() = runTest {
        // Act & Assert
        repository.getWordDetail("   ")
    }

    /**
     * Test network error handling.
     */
    @Test(expected = DictionaryApiException::class)
    fun getWordDetail_NetworkError_ThrowsDictionaryApiException() = runTest {
        // Arrange
        whenever(mockApi.getWordDetail(testWord)).thenThrow(
            RuntimeException("Network error")
        )

        // Act & Assert
        repository.getWordDetail(testWord)
    }

    /**
     * Test word trimming and lowercasing.
     */
    @Test
    fun getWordDetail_TrimsAndLowercasesWord() = runTest {
        // Arrange
        val response = Response.success(listOf(testWordDetail))
        whenever(mockApi.getWordDetail("hello")).thenReturn(response)

        // Act
        repository.getWordDetail("  HELLO  ")

        // Assert - verify lowercase was passed
        org.mockito.kotlin.verify(mockApi).getWordDetail("hello")
    }
}
