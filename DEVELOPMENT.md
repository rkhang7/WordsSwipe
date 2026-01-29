# Development Guide - WordsSwipe

This guide helps developers understand how to extend and maintain the WordsSwipe app.

## Quick Start

### Prerequisites
- Android Studio Giraffe or later
- JDK 11+
- Android SDK 28+ (min), 35+ (target)
- Physical device or emulator with API 28+

### Building the App

```bash
# Clone/navigate to project
cd /path/to/WordsSwipe

# Build debug APK
./gradlew assembleDebug

# Build and run on device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Project Structure Deep Dive

### `app/build.gradle.kts`
**What it does**: Defines app-level build configuration, dependencies, and compilation options

**Key sections**:
- `plugins`: Hilt, Kotlin Compose, Kapt for annotation processing
- `android`: SDK versions, app ID, build flavors
- `dependencies`: All external libraries with BOM for versioning

**Important Gradle Features Used**:
- `libs.versions.toml` - Centralized version management
- `kapt` - Annotation processing for Hilt
- BOM (Bill of Materials) - Consistent Compose versions

### Data Layer Structure

```
data/
├── source/
│   └── LocalDataSource.kt
│       - getMockData(): List<Word>
│       - Injected into repository
│       - Can be replaced with API client
│
└── repository/
    └── WordRepositoryImpl.kt
        - Implements WordRepository interface
        - Wraps LocalDataSource
        - Adds Flow and error handling
        - Simulates network delay with delay(500)
```

**Extending Data Layer**:

To add API support:
```kotlin
// 1. Create API client
interface WordsApi {
    @GET("words")
    suspend fun getWords(): List<Word>
}

// 2. Update repository
class WordRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val api: WordsApi  // Add API
) : WordRepository {
    override fun getAllWords(): Flow<List<Word>> = flow {
        try {
            emit(api.getWords())  // Try remote first
        } catch (e: Exception) {
            emit(localDataSource.getWords())  // Fallback to local
        }
    }.flowOn(Dispatchers.IO)
}
```

### Domain Layer Structure

```
domain/
├── model/
│   └── Word.kt
│       - data class with word properties
│       - Pure domain model
│       - No framework dependencies
│
├── repository/
│   └── WordRepository.kt
│       - Interface (contract only)
│       - Defined by domain
│       - Implemented by data layer
│
└── usecase/
    └── GetWordsUseCase.kt
        - @Inject constructor injection
        - operator fun invoke() for clean syntax
        - Delegates to repository
```

**Extending Domain Layer**:

To add search functionality:
```kotlin
// 1. Add to repository interface
interface WordRepository {
    fun getAllWords(): Flow<List<Word>>
    fun searchWords(query: String): Flow<List<Word>>  // New
}

// 2. Create UseCase
class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(query: String): Flow<List<Word>> =
        wordRepository.searchWords(query)
}

// 3. Implement in repository
override fun searchWords(query: String): Flow<List<Word>> = flow {
    val allWords = localDataSource.getWords()
    emit(allWords.filter { word ->
        word.text.contains(query, ignoreCase = true)
    })
}.flowOn(Dispatchers.IO)
```

### UI Layer Structure

#### ViewModel Pattern

```kotlin
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase
) : ViewModel() {
    // 1. Private mutable state
    private val _uiState = MutableStateFlow<WordsUiState>(WordsUiState.Loading)
    
    // 2. Public immutable state
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()
    
    // 3. Init block - data loading
    init { loadWords() }
    
    // 4. Business logic methods
    private fun loadWords() { ... }
}
```

**Key Points**:
- `@HiltViewModel` enables constructor injection
- `MutableStateFlow` for internal mutations
- `StateFlow` (immutable) exposed to UI
- `viewModelScope.launch` respects lifecycle
- State changes trigger recomposition

#### Composable Functions

```kotlin
// Pure function - no side effects
@Composable
fun WordsScreen(
    viewModel: WordsViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Observe state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 2. Render based on state
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is WordsUiState.Loading -> LoadingUI()
            is WordsUiState.Success -> SuccessUI(uiState.words)
            is WordsUiState.Error -> ErrorUI(uiState.message)
        }
    }
}
```

**Composable Best Practices**:
- Accept data as parameters (not from ViewModel directly)
- Return UI, never null
- No side effects
- Reusable across different screens
- Preview-friendly

#### State Definition

```kotlin
sealed class WordsUiState {
    data object Loading : WordsUiState()
    data class Success(val words: List<Word>) : WordsUiState()
    data class Error(val message: String) : WordsUiState()
}
```

**Why Sealed Classes?**
- Compiler ensures all cases are handled
- Type-safe property access
- Clear intent: "These are all possible states"
- Easy to add new states later

## Dependency Injection Deep Dive

### Hilt Module Structure

```kotlin
@Module
@InstallIn(SingletonComponent::class)  // Scope
abstract class RepositoryModule {
    @Binds  // Tell Hilt to use impl for interface
    @Singleton  // Same instance everywhere
    abstract fun bindWordRepository(
        implementation: WordRepositoryImpl
    ): WordRepository
}
```

**Scopes**:
- `SingletonComponent` - App lifetime
- `ActivityComponent` - Activity lifetime
- `FragmentComponent` - Fragment lifetime
- `ViewModelComponent` - ViewModel lifetime

### Adding New Dependencies

Example: Adding Room Database

```kotlin
// 1. Add to libs.versions.toml
[versions]
room = "2.6.1"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

// 2. Add to app/build.gradle.kts
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
}

// 3. Create Hilt module for Room
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "words.db").build()
    
    @Provides
    @Singleton
    fun provideWordDao(db: AppDatabase): WordDao = db.wordDao()
}

// 4. Update repository to use Room
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {
    override fun getAllWords(): Flow<List<Word>> =
        wordDao.getAllWords()
}
```

## Common Development Tasks

### Adding a New Feature

Example: Bookmark Favorites

**Step 1: Domain Layer**
```kotlin
// Add to Word model
data class Word(
    val id: Int,
    val text: String,
    // ... existing fields
    val isBookmarked: Boolean = false  // New
)

// Create usecase
class ToggleBookmarkUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(wordId: Int): Flow<Unit> =
        repository.toggleBookmark(wordId)
}

// Update repository interface
interface WordRepository {
    fun getAllWords(): Flow<List<Word>>
    fun toggleBookmark(wordId: Int): Flow<Unit>  // New
}
```

**Step 2: Data Layer**
```kotlin
// Update repository implementation
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao  // Database
) : WordRepository {
    override fun toggleBookmark(wordId: Int): Flow<Unit> = flow {
        wordDao.toggleBookmark(wordId)  // Database operation
        emit(Unit)
    }.flowOn(Dispatchers.IO)
}
```

**Step 3: UI Layer**
```kotlin
// Update ViewModel
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase  // Inject
) : ViewModel() {
    // ... existing code
    
    fun toggleBookmark(wordId: Int) {
        viewModelScope.launch {
            toggleBookmarkUseCase(wordId).collect()
        }
    }
}

// Update Composable
@Composable
fun WordCard(
    word: Word,
    onBookmarkClick: (Int) -> Unit = {},  // New callback
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // ... existing content
        
        // Add bookmark button
        Button(onClick = { onBookmarkClick(word.id) }) {
            Text(if (word.isBookmarked) "★" else "☆")
        }
    }
}
```

### Running Tests

```bash
# Unit tests (local JVM)
./gradlew test

# Specific test class
./gradlew test --tests WordsViewModelTest

# Instrumented tests (on device)
./gradlew connectedAndroidTest

# With code coverage
./gradlew testDebugUnitTestCoverage
```

### Debugging

**Logcat Filtering**:
```bash
# Filter by tag
adb logcat | grep "WordsSwipe"

# Filter by package
adb logcat | grep "com.example.wordsswipe"
```

**Debug Breakpoints**:
1. Click line number to set breakpoint
2. Run app in debug mode
3. Android Studio pauses at breakpoint
4. Inspect variables in Variables panel

**Flow Debugging**:
```kotlin
flow {
    emit(1)
    emit(2)
}
    .onStart { println("Starting") }
    .onEach { println("Emitting: $it") }
    .catch { println("Error: $it") }
    .collect()
```

## Performance Optimization

### Memory Profiling

```bash
# Start memory profiler
1. Open Android Studio
2. Android Profilers tab → Memory
3. Record allocations
4. Look for memory leaks
```

**Common Leaks**:
- ViewModel holding Activity reference
- Coroutine not cancelled in viewModelScope
- Static references to Context

### Recomposition Analysis

```kotlin
@Composable
fun WordCard(word: Word) {
    // This re-traces on every recomposition
    println("WordCard recomposing: ${word.text}")
    
    // Stable state (data class) - skips recomposition if equal
    Column { ... }
}
```

**Optimize**:
- Keep state data classes (immutable)
- Use `remember` for expensive computations
- Use `derivedStateOf` for derived state
- Avoid unnecessary recompositions with `key` in Lists

### Network Optimization

```kotlin
// Add caching to repository
class WordRepositoryImpl @Inject constructor(
    private val api: WordsApi
) : WordRepository {
    private var cachedWords: List<Word>? = null
    
    override fun getAllWords(): Flow<List<Word>> = flow {
        cachedWords?.let { emit(it); return@flow }
        
        val words = api.getWords()
        cachedWords = words
        emit(words)
    }.flowOn(Dispatchers.IO)
}
```

## Code Style Guide

### Naming Conventions

```kotlin
// Classes: PascalCase
class WordsViewModel { }

// Functions/variables: camelCase
fun loadWords() { }
val uiState: StateFlow<WordsUiState>

// Private fields: _camelCase or camelCase
private val _uiState = MutableStateFlow()

// Constants: UPPER_SNAKE_CASE
const val MAX_WORDS = 100
```

### File Organization

```kotlin
// Order in class:
1. Properties (val, var)
2. Constructor/init
3. Public methods
4. Private methods
5. Companion object
```

### Documentation

```kotlin
/**
 * Loads words from repository and updates UI state.
 * Uses coroutines for proper lifecycle management.
 *
 * @see GetWordsUseCase
 * @see WordsUiState
 */
private fun loadWords() { ... }
```

## Troubleshooting

### Build Errors

**Kapt errors**:
```
Solution: Clean build
./gradlew clean build
```

**Hilt errors**:
```
Solution: Check @HiltAndroidApp is in Application
Check all @HiltViewModel classes are in correct scope
```

**Dependency conflicts**:
```
Solution: Use ./gradlew dependencyInsight --dependency <name>
to find conflicting versions
```

### Runtime Errors

**StateFlow not updating UI**:
```kotlin
// Wrong - StateFlow doesn't automatically update
val state = viewModel.uiState

// Right - Use collectAsStateWithLifecycle()
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

**Coroutine not cancelling**:
```kotlin
// Wrong - Job not cancelled
GlobalScope.launch { ... }

// Right - Respects lifecycle
viewModelScope.launch { ... }
```

**Memory leak on configuration change**:
```kotlin
// Wrong - holds Activity reference
val activity: Activity = this

// Right - Only context needed (by ViewModel)
val context: Context = this
```

## Resources

- **Official Docs**: https://developer.android.com/jetpack
- **Hilt**: https://dagger.dev/hilt/
- **Compose**: https://developer.android.com/jetpack/compose
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **Clean Architecture**: https://blog.cleancoder.com/

## Contributing

When adding features:
1. Follow clean architecture (data → domain → ui)
2. Add unit tests
3. Document public APIs
4. Update this guide if adding new patterns
5. Ensure no business logic in Composables
6. Test on multiple API levels
