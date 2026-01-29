## WordsSwipe - English Learning App

A production-ready Android app built with **Jetpack Compose** featuring a TikTok-style vertical swipe UI for learning English vocabulary.

### Architecture Overview

The app follows **Clean Architecture** principles with strict separation of concerns:

```
┌─────────────────────────────────────────────┐
│          UI Layer (Presentation)            │
│  ┌──────────────────────────────────────┐  │
│  │ MainActivity → WordsScreen           │  │
│  │ WordsViewModel (MVVM Pattern)        │  │
│  │ WordsUiState (Sealed Class)          │  │
│  │ Composables (Pure UI, No Logic)      │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│         Domain Layer (Business Logic)       │
│  ┌──────────────────────────────────────┐  │
│  │ GetWordsUseCase                      │  │
│  │ WordRepository (Interface)           │  │
│  │ Word (Data Model)                    │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│           Data Layer (Implementation)       │
│  ┌──────────────────────────────────────┐  │
│  │ WordRepositoryImpl                    │  │
│  │ LocalDataSource (Mock Words)          │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

### Key Features

✅ **Fullscreen TikTok-Style UI**
- Vertical swipe navigation using `VerticalPager`
- One word per page
- No internal scrolling within a page
- Smooth animations

✅ **Complete English Word Metadata**
- English word
- Definition
- Example sentence with context
- Part of speech
- Difficulty level (1-5 stars)

✅ **Production-Grade Architecture**
- **Hilt** for dependency injection
- **StateFlow** for state management (unidirectional data flow)
- **SharedFlow** for one-time UI events
- **Coroutines** for asynchronous operations
- **ViewModel** for lifecycle-aware state
- **Jetpack Compose** for modern declarative UI

✅ **Best Practices**
- No business logic in Composables
- Pure, reusable composable functions
- Immutable UI state
- Proper error handling with sealed classes
- Flow-based reactive architecture

### Project Structure

```
app/src/main/java/com/example/wordsswipe/
├── MainActivity.kt                      # Entry point, Hilt-enabled
├── WordsSwipeApplication.kt             # Hilt app initialization
├── data/
│   ├── repository/
│   │   └── WordRepositoryImpl.kt        # Repository implementation
│   └── source/
│       └── LocalDataSource.kt           # Mock data source
├── domain/
│   ├── model/
│   │   └── Word.kt                      # Word data model
│   ├── repository/
│   │   └── WordRepository.kt            # Repository interface
│   └── usecase/
│       └── GetWordsUseCase.kt           # Business logic
├── di/
│   └── RepositoryModule.kt              # Hilt DI configuration
└── ui/
    ├── screen/words/
    │   ├── WordsScreen.kt               # Main composables
    │   ├── WordsViewModel.kt            # State management
    │   └── WordsUiState.kt              # UI state definition
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### State Management Pattern

**Unidirectional Data Flow:**

```
User Interaction
       ↓
   ViewModel (GetWordsUseCase)
       ↓
Repository & UseCase Layer
       ↓
StateFlow<WordsUiState> (UI State)
       ↓
Composables (Recomposition)
       ↓
Screen Update
```

### ViewModel and StateFlow

The `WordsViewModel` manages all state for the words screen:

```kotlin
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val getWordsUseCase: GetWordsUseCase
) : ViewModel() {
    
    // State exposure (immutable)
    private val _uiState = MutableStateFlow<WordsUiState>(WordsUiState.Loading)
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()
    
    // Event exposure
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()
}
```

**UI State Types:**
- `Loading` - Initial load state
- `Success(words)` - Words loaded successfully
- `Error(message)` - Error occurred

### Composables Architecture

All composables are **pure functions** with no side effects:

1. **WordsScreen** - Main container composable
2. **WordsPager** - VerticalPager wrapper for page navigation
3. **WordCard** - Full-screen card displaying word details
4. **DifficultyBadge** - Visual difficulty indicator
5. **ErrorMessage** - Error state UI

### Dependency Injection (Hilt)

**Module Configuration** (`RepositoryModule.kt`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWordRepository(
        implementation: WordRepositoryImpl
    ): WordRepository
}
```

**ViewModel Injection:**
```kotlin
val wordsViewModel: WordsViewModel = hiltViewModel()
```

### Data Layer

**Mock Data** in `LocalDataSource`:
- 10 English words
- Each with definition, example, part of speech, difficulty level
- Uses dependency injection for flexibility

**Repository Pattern**:
- Decouples data sources from domain logic
- Enables easy switching between local/remote data
- Proper error handling with Flow operators

### Building & Running

```bash
# Clean build
./gradlew clean build

# Run on device/emulator
./gradlew installDebug
```

### Dependencies Used

- **androidx.lifecycle:lifecycle-viewmodel-compose** - ViewModel in Compose
- **androidx.foundation** - VerticalPager and layout foundation
- **androidx.navigation:navigation-compose** - Navigation support
- **com.google.dagger:hilt-android** - Dependency injection
- **androidx.hilt:hilt-navigation-compose** - Hilt-Compose integration
- **kotlinx.coroutines** - Asynchronous programming
- **androidx.compose** - Modern Compose UI toolkit
- **androidx.material3** - Material Design 3 components

### Future Enhancements

- [ ] Database integration (Room) for persistent storage
- [ ] Remote API integration for word data
- [ ] User progress tracking
- [ ] Search functionality
- [ ] Bookmarking/favorites
- [ ] Multiple vocabulary lists
- [ ] Audio pronunciation
- [ ] Quiz mode
- [ ] Statistics dashboard

### Design Principles Applied

✅ **SOLID Principles**
- Single Responsibility: Each class has one reason to change
- Open/Closed: Open for extension, closed for modification
- Liskov Substitution: Repository implementations are interchangeable
- Interface Segregation: Focused, minimal interfaces
- Dependency Inversion: Depends on abstractions, not implementations

✅ **Clean Code**
- Meaningful names
- Small, focused functions
- No magic numbers
- Comprehensive comments
- Well-structured hierarchy

✅ **Reactive Programming**
- State management via Flow
- One-way data binding
- Immutable state
- Proper lifecycle handling

### Testing Ready

The architecture supports easy unit and UI testing:
- ViewModels can be tested without UI
- Repositories can be mocked
- UseCases are pure functions
- Composables are stateless and testable

### License

This is a learning project. Feel free to use and modify as needed.
