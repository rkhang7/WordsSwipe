## Architecture & Best Practices Guide

This document explains the architectural decisions and best practices implemented in the WordsSwipe app.

### 1. Clean Architecture Layers

#### UI Layer (Presentation)
**Responsibility**: Display data and handle user interactions

**Components**:
- `MainActivity` - Activity with Hilt integration
- `WordsScreen` - Composable that observes ViewModel state
- `WordsPager` - VerticalPager wrapper for navigation
- `WordCard` - Individual word display card
- `WordsViewModel` - State holder and business logic coordinator

**Key Principles**:
- Composables are **pure functions** (no side effects)
- State is managed in ViewModel, not in Composables
- UI reacts to StateFlow changes via `collectAsStateWithLifecycle()`

#### Domain Layer (Business Logic)
**Responsibility**: Contain pure business logic independent of UI or data source

**Components**:
- `Word` - Domain model representing a word
- `WordRepository` - Interface defining data contract
- `GetWordsUseCase` - Business logic for fetching words

**Key Principles**:
- No Android framework dependencies
- Interfaces define contracts, implementations come later
- UseCases are thin, focused, and testable

#### Data Layer (Data Access)
**Responsibility**: Fetch and provide data

**Components**:
- `WordRepositoryImpl` - Repository implementation using Flow
- `LocalDataSource` - Mock data provider

**Key Principles**:
- Decouples from domain layer via repository pattern
- Easy to swap implementations (local, remote, hybrid)
- Uses Flow for reactive data streaming

### 2. State Management Pattern (Unidirectional Data Flow)

```
┌──────────────────────────────────────────────┐
│                 UI Layer                      │
│                                              │
│  WordsScreen observes uiState via            │
│  StateFlow<WordsUiState>.collectAsStateWithLifecycle()
│                                              │
│  When state changes → Recomposition happens  │
└──────────────────────────────────────────────┘
                      ↑
                      │ (reads)
                      │
┌──────────────────────────────────────────────┐
│            WordsViewModel                     │
│                                              │
│  - Holds mutable state (_uiState)            │
│  - Exposes immutable state (uiState)         │
│  - Collects from UseCase via Flow            │
│  - Maintains state across config changes     │
└──────────────────────────────────────────────┘
                      ↑
                      │ (collects)
                      │
┌──────────────────────────────────────────────┐
│          Domain & Data Layers                 │
│                                              │
│  GetWordsUseCase → WordRepository            │
│                                              │
│  Returns: Flow<List<Word>>                   │
└──────────────────────────────────────────────┘
```

**Benefits**:
- Single source of truth (ViewModel)
- Predictable state changes
- Easy to debug (follow the flow)
- Testable at each layer
- Survives configuration changes

### 3. ViewModel Lifecycle Management

```kotlin
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WordsUiState>(WordsUiState.Loading)
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()
    
    init {
        loadWords()  // Automatically called when ViewModel is created
    }
    
    private fun loadWords() {
        viewModelScope.launch {  // Respects ViewModel lifecycle
            getWordsUseCase()
                .onStart { _uiState.value = WordsUiState.Loading }
                .catch { error -> handleError(error) }
                .collect { words -> _uiState.value = WordsUiState.Success(words) }
        }
    }
}
```

**Why `viewModelScope`?**
- Automatically cancels when ViewModel is cleared
- Prevents memory leaks
- Bound to Activity/Fragment lifecycle

### 4. Sealed Class for Type-Safe State

```kotlin
sealed class WordsUiState {
    data object Loading : WordsUiState()
    data class Success(val words: List<Word>) : WordsUiState()
    data class Error(val message: String) : WordsUiState()
}
```

**Benefits**:
- Exhaustive `when` expressions (compiler ensures all cases handled)
- Type-safe property access
- No null checks needed
- Clear state transitions

### 5. Flow vs StateFlow vs SharedFlow

| Type | Purpose | Multicast | Replay | Use Case |
|------|---------|-----------|--------|----------|
| **Flow** | Async data stream | ❌ | ❌ | One-shot operations |
| **StateFlow** | Current state | ✅ | ✅ | UI state management |
| **SharedFlow** | Events | ✅ | ❌ | Notifications, events |

**In WordsSwipe**:
- `StateFlow<WordsUiState>` - Words list state (everyone gets current state)
- `SharedFlow<String>` - Error events (one-time notifications)

### 6. Dependency Injection with Hilt

**Benefits of Hilt**:
- Automatic dependency graph generation
- Compile-time safety
- Scoped bindings (Singleton, Activity, etc.)
- Easy to test (can provide mocks)

**DI Flow in WordsSwipe**:

```
1. @HiltAndroidApp
   WordsSwipeApplication
   
   ↓
   
2. WordRepositoryModule
   Binds WordRepository to WordRepositoryImpl
   
   ↓
   
3. @HiltViewModel WordsViewModel
   Receives GetWordsUseCase via constructor
   
   ↓
   
4. GetWordsUseCase @Inject constructor
   Receives WordRepository via constructor
   
   ↓
   
5. WordRepositoryImpl @Inject constructor
   Receives LocalDataSource via constructor
```

### 7. VerticalPager for Swipe Navigation

```kotlin
val pagerState = rememberPagerState(pageCount = { words.size })

VerticalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize()
) { pageIndex ->
    WordCard(word = words[pageIndex])
}
```

**Why VerticalPager**?
- Native Compose support
- Smooth animations
- Efficient memory usage (reuses pages)
- Built-in gesture handling
- No internal scrolling per page (fullscreen design)

### 8. Composable Function Rules

**✅ DO**:
```kotlin
@Composable
fun WordCard(word: Word) {
    // Pure function - no side effects
    Column { ... }
}
```

**❌ DON'T**:
```kotlin
@Composable
fun WordCard(word: Word) {
    // Side effect in composable
    LaunchedEffect(Unit) {
        viewModel.loadWords()  // WRONG!
    }
}
```

**Why**:
- Composables can be called many times
- Can be skipped or reordered by compiler
- Side effects belong in ViewModel

### 9. Error Handling Strategy

```kotlin
getWordsUseCase()
    .onStart {
        // Before emission
        _uiState.value = WordsUiState.Loading
    }
    .catch { throwable ->
        // When exception occurs
        _uiState.value = WordsUiState.Error(throwable.message)
        _errorEvent.emit(throwable.message)
    }
    .collect { words ->
        // On successful emission
        _uiState.value = WordsUiState.Success(words)
    }
```

**Error Recovery**:
- UI shows error message via sealed state
- SharedFlow emits error event for toast/notification
- No crashes - exceptions handled gracefully

### 10. Testing Strategy

This architecture enables:

**Unit Tests**:
```kotlin
@Test
fun testLoadWords_Success() {
    val mockRepo = mockk<WordRepository>()
    every { mockRepo.getAllWords() } returns flowOf(testWords)
    
    val useCase = GetWordsUseCase(mockRepo)
    val viewModel = WordsViewModel(useCase)
    
    // Verify state
    assert(viewModel.uiState.value is WordsUiState.Success)
}
```

**Integration Tests**:
- Test full flow from UseCase to ViewModel
- Mock repository, test business logic

**UI Tests**:
- Mock ViewModel
- Test Composables in isolation
- Verify correct rendering

### 11. Scalability Considerations

**Current Design Supports**:
- ✅ Switching data sources (Room, API, etc.)
- ✅ Multiple repositories
- ✅ Complex business logic
- ✅ Offline functionality
- ✅ Real-time updates

**How**:
- Repository interface is agnostic to implementation
- UseCase can compose multiple repositories
- Flow-based architecture supports WebSocket/real-time updates
- StateFlow maintains consistency

### 12. Performance Optimizations

**Memory Efficient**:
- ViewModels survive configuration changes (no re-fetching)
- VerticalPager recycles page content
- Flow operators (map, filter, etc.) are lazy

**CPU Efficient**:
- Composables only recompose when their inputs change
- State objects are data classes (smart recomposition)
- Flow backpressure handling

**Network Efficient**:
- Delay(500ms) simulates real latency
- Easy to add caching layer in repository
- Flow supports debounce/throttle operators

### 13. Code Quality Checklist

- ✅ No business logic in Composables
- ✅ All state in ViewModel via StateFlow
- ✅ Repository pattern for data access
- ✅ UseCase for business logic
- ✅ Sealed classes for type-safe states
- ✅ Dependency injection for all major classes
- ✅ Proper error handling
- ✅ Immutable UI state
- ✅ Pure composable functions
- ✅ Comprehensive documentation

### 14. Common Pitfalls & Solutions

| Pitfall | Issue | Solution |
|---------|-------|----------|
| Composable side effects | Non-deterministic | Use LaunchedEffect/ViewModel |
| ViewModel injection issues | Compile errors | Use `@HiltViewModel` annotation |
| State leaks | Memory leaks | Use `viewModelScope.launch` |
| Multiple recompositions | Performance | Make state data classes immutable |
| Missing error handling | Crashes | Use Flow catch operators |
| Nested state updates | Race conditions | Central state in ViewModel |

### References

- [Official Hilt Documentation](https://dagger.dev/hilt/)
- [Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Clean Architecture Principles](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architectural Patterns](https://developer.android.com/topic/architecture)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
