package com.example.wordsswipe.domain.usecase

import com.example.wordsswipe.data.remote.repository.DictionaryRepository
import com.example.wordsswipe.domain.model.Definition
import com.example.wordsswipe.domain.model.Meaning
import com.example.wordsswipe.domain.model.WordDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for GetWordDetailUseCase.
 *
 * Tests:
 * - Successful word lookup
 * - Operator invoke syntax
 * - Proper delegation to repository
 */
class GetWordDetailUseCaseTest {

    @Mock
    private lateinit var mockRepository: DictionaryRepository

    private lateinit var useCase: GetWordDetailUseCase

    private val testWordDetail = WordDetail(
        word = "test",
        phonetic = "/test/",
        phonetics = emptyList(),
        meanings = listOf(
            Meaning(
                partOfSpeech = "noun",
                definitions = listOf(
                    Definition(
                        definition = "a test definition",
                        example = "This is a test"
                    )
                )
            )
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        useCase = GetWordDetailUseCase(mockRepository)
    }

    /**
     * Test successful word detail retrieval.
     */
    @Test
    fun invoke_Success_ReturnsWordDetail() = runTest {
        // Arrange
        whenever(mockRepository.getWordDetail("test")).thenReturn(testWordDetail)

        // Act
        val result = useCase("test")

        // Assert
        assertNotNull(result)
        assertEquals("test", result.word)
        assertEquals(1, result.meanings.size)
    }

    /**
     * Test operator invoke syntax.
     */
    @Test
    fun invoke_Operator_Works() = runTest {
        // Arrange
        whenever(mockRepository.getWordDetail("test")).thenReturn(testWordDetail)

        // Act
        val result = useCase.invoke("test")

        // Assert
        assertEquals("test", result.word)
    }

    /**
     * Test repository delegation.
     */
    @Test
    fun invoke_DelegatesProperlyToRepository() = runTest {
        // Arrange
        whenever(mockRepository.getWordDetail("test")).thenReturn(testWordDetail)

        // Act
        val result = useCase("test")

        // Assert
        assertEquals(testWordDetail, result)
        org.mockito.kotlin.verify(mockRepository).getWordDetail("test")
    }
}
