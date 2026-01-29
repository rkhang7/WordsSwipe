# WordsRepository - Code Reference & Examples

## Core Implementation

### Repository Class Structure

```kotlin
@Singleton
class WordsRepository @Inject constructor(
    private val context: Context
) {
    // Cached words list
    private var cachedWords: List<String>? = null
    
    // Main API functions
    suspend fun getAllWords(): List<String>
    suspend fun getRandomWords(count: Int): List<String>
    suspend fun getRandomWord(): String
    suspend fun getWordsCount(): Int
    fun clearCache()
}
```

---

## Usage Patterns

### Pattern 1: Simple Random Selection

```kotlin
// Get 5 random words
val words = wordsRepository.getRandomWords(5)
// Result: ["apple", "zebra", "crystal", "mountain", "butterfly"]
```

### Pattern 2: Using in ViewModel

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val wordsRepository: WordsRepository
) : ViewModel() {
    
    private val _words = MutableStateFlow<List<String>>(emptyList())
    val words: StateFlow<List<String>> = _words.asStateFlow()
    
    fun loadWords() {
        viewModelScope.launch {
            val randomWords = wordsRepository.getRandomWords(10)
            _words.value = randomWords
        }
    }
}
```

### Pattern 3: Using UseCase Layer

```kotlin
// Domain UseCase
class GetRandomWordsUseCase @Inject constructor(
    private val wordsRepository: WordsRepository
) {
    suspend operator fun invoke(count: Int): List<String> =
        wordsRepository.getRandomWords(count)
}

// ViewModel using UseCase
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getRandomWordsUseCase: GetRandomWordsUseCase
) : ViewModel() {
    
    fun loadWords() {
        viewModelScope.launch {
            val words = getRandomWordsUseCase(10)
            // Use words
        }
    }
}
```

### Pattern 4: Error Handling

```kotlin
viewModelScope.launch {
    try {
        val words = wordsRepository.getRandomWords(5)
        _words.value = words
    } catch (e: IllegalArgumentException) {
        _error.value = "Invalid count: ${e.message}"
    } catch (e: Exception) {
        _error.value = "Failed to load words: ${e.message}"
    }
}
```

### Pattern 5: Single Word Retrieval

```kotlin
viewModelScope.launch {
    val word = wordsRepository.getRandomWord()
    _currentWord.value = word
}
```

### Pattern 6: Batch Operations

```kotlin
viewModelScope.launch {
    // Load all words for analysis
    val allWords = wordsRepository.getAllWords()
    val count = wordsRepository.getWordsCount()
    
    println("Total words: $count")
    println("All words: $allWords")
}
```

---

## Dispatcher Understanding

### Why Two Dispatchers?

```kotlin
// IO Dispatcher - For blocking file operations
suspend fun getAllWords(): List<String> = withContext(Dispatchers.IO) {
    // File I/O is blocking
    context.assets.open("words.txt").bufferedReader().use { reader ->
        reader.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
    }
    // Results cached after first load
}

// Default Dispatcher - For CPU-intensive operations
suspend fun getRandomWords(count: Int) = withContext(Dispatchers.Default) {
    // Shuffling is CPU-intensive, not I/O bound
    allWords.shuffled().take(count)
}
```

### Execution Flow

```
Main Thread (UI)
    ↓ (viewModelScope.launch)
Main Dispatcher
    ↓ (withContext(Dispatchers.IO))
IO Thread Pool
    ↓ (Read file)
Cached Words
    ↓ (withContext(Dispatchers.Default))
Default Thread Pool
    ↓ (Shuffle)
Result
    ↓ (Return to Caller)
Main Thread (UI Update)
```

---

## Real-World Integration Examples

### Example 1: Quiz App

```kotlin
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val wordsRepository: WordsRepository
) : ViewModel() {
    
    private val _questions = MutableStateFlow<List<String>>(emptyList())
    val questions: StateFlow<List<String>> = _questions.asStateFlow()
    
    private val _currentQuestion = MutableStateFlow(0)
    val currentQuestion: StateFlow<Int> = _currentQuestion.asStateFlow()
    
    fun initializeQuiz() {
        viewModelScope.launch {
            // Load 10 random words for quiz
            val questions = wordsRepository.getRandomWords(10)
            _questions.value = questions
        }
    }
    
    fun nextQuestion() {
        _currentQuestion.value = (_currentQuestion.value + 1) % _questions.value.size
    }
}
```

### Example 2: Learning App

```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val getRandomWordsUseCase: GetRandomWordsUseCase
) : ViewModel() {
    
    private val _lesson = MutableStateFlow<List<String>>(emptyList())
    val lesson: StateFlow<List<String>> = _lesson.asStateFlow()
    
    fun startLesson(wordCount: Int) {
        viewModelScope.launch {
            val words = getRandomWordsUseCase(wordCount)
            _lesson.value = words
        }
    }
}
```

### Example 3: Flash Card App

```kotlin
@HiltViewModel
class FlashCardViewModel @Inject constructor(
    private val getRandomWordUseCase: GetRandomWordUseCase,
    private val getAllWordsUseCase: GetAllWordsUseCase
) : ViewModel() {
    
    private val _currentWord = MutableStateFlow("")
    val currentWord: StateFlow<String> = _currentWord.asStateFlow()
    
    private val _stats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val stats: StateFlow<Map<String, Int>> = _stats.asStateFlow()
    
    fun nextCard() {
        viewModelScope.launch {
            val word = getRandomWordUseCase()
            _currentWord.value = word
        }
    }
    
    fun showStatistics() {
        viewModelScope.launch {
            val allWords = getAllWordsUseCase()
            val stats = mapOf(
                "total" to allWords.size,
                "learned" to 0  // Would be from database
            )
            _stats.value = stats
        }
    }
}
```

---

## Testing Patterns

### Mocking the Repository

```kotlin
@Mock
private lateinit var mockRepository: WordsRepository

@Before
fun setUp() {
    MockitoAnnotations.openMocks(this)
    whenever(mockRepository.getRandomWords(5))
        .thenReturn(listOf("apple", "banana", "cherry", "date", "fig"))
}

@Test
fun viewModel_LoadsWords_UpdatesState() = runTest {
    // Arrange
    val viewModel = MyViewModel(mockRepository)
    
    // Act
    viewModel.loadWords()
    
    // Assert
    assertEquals(5, viewModel.words.value.size)
}
```

### Integration Test

```kotlin
@Test
fun repository_LoadsFromAsset_ReturnsWords() = runTest {
    // Create real repository with mocked context
    val inputStream = ByteArrayInputStream(testWords.toByteArray())
    whenever(mockContext.assets.open("words.txt")).thenReturn(inputStream)
    
    val repository = WordsRepository(mockContext)
    
    // Load words
    val words = repository.getAllWords()
    
    // Verify
    assertEquals(10, words.size)
    assertTrue(words.contains("apple"))
}
```

---

## Common Patterns & Anti-patterns

### ✅ Good: Using Coroutines

```kotlin
viewModelScope.launch {
    val words = wordsRepository.getRandomWords(5)
    _words.value = words
}
```

### ❌ Bad: Blocking Main Thread

```kotlin
// DON'T DO THIS!
val words = runBlocking {
    wordsRepository.getRandomWords(5)
}
```

### ✅ Good: Error Handling

```kotlin
viewModelScope.launch {
    try {
        val words = wordsRepository.getRandomWords(count)
        _words.value = words
    } catch (e: IllegalArgumentException) {
        _error.value = "Invalid count"
    }
}
```

### ❌ Bad: Ignoring Errors

```kotlin
// DON'T DO THIS!
viewModelScope.launch {
    val words = wordsRepository.getRandomWords(count)  // No error handling
    _words.value = words
}
```

### ✅ Good: Dependency Injection

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val wordsRepository: WordsRepository
) : ViewModel()
```

### ❌ Bad: Direct Instantiation

```kotlin
// DON'T DO THIS!
val repository = WordsRepository(context)
```

---

## Performance Tips

### Tip 1: Reuse Random Words

```kotlin
// Load once
viewModelScope.launch {
    val words = wordsRepository.getRandomWords(100)
    // Reuse words multiple times
    // Don't call getRandomWords repeatedly
}
```

### Tip 2: Batch Large Requests

```kotlin
// Good - single call for 50 words
val words = wordsRepository.getRandomWords(50)

// Avoid - multiple calls
val words = mutableListOf<String>()
repeat(10) {
    words.addAll(wordsRepository.getRandomWords(5))
}
```

### Tip 3: Cache Results When Needed

```kotlin
class MyViewModel {
    private var cachedWords: List<String>? = null
    
    fun getOrLoadWords() {
        viewModelScope.launch {
            if (cachedWords == null) {
                cachedWords = wordsRepository.getRandomWords(100)
            }
            _words.value = cachedWords!!
        }
    }
}
```

---

## Coroutines Best Practices

### Proper Scope

```kotlin
// ✅ Use viewModelScope (recommended)
@HiltViewModel
class MyViewModel : ViewModel() {
    fun loadWords() {
        viewModelScope.launch {
            val words = wordsRepository.getRandomWords(5)
        }
    }
}

// ❌ Avoid GlobalScope
GlobalScope.launch {  // DON'T USE!
    val words = wordsRepository.getRandomWords(5)
}
```

### Cancellation Safety

```kotlin
// Coroutines launched in viewModelScope are auto-cancelled
// when ViewModel is cleared
viewModelScope.launch {
    val words = wordsRepository.getRandomWords(5)
    // Automatically cancelled if ViewModel destroyed
}
```

---

## Summary Table

| Task | Function | Dispatcher | Speed |
|------|----------|-----------|-------|
| Load all words | `getAllWords()` | IO | 100-200ms (first), <5ms (cached) |
| Get random batch | `getRandomWords(n)` | Default | <1ms |
| Get single word | `getRandomWord()` | Default | <1ms |
| Get word count | `getWordsCount()` | IO | 100-200ms (first), <5ms (cached) |
| Clear cache | `clearCache()` | Main | <1ms |

---

**This reference covers all common usage patterns and best practices for the WordsRepository!**
