# WordsSwipe - Quick Reference Card

## 🎯 What is WordsSwipe?

A production-ready English learning app with TikTok-style vertical swipe UI, built using Jetpack Compose and Clean Architecture.

---

## 📱 Key Features

| Feature | Details |
|---------|---------|
| **UI Style** | Fullscreen vertical swipe (VerticalPager) |
| **Content** | English words with definitions, examples |
| **Navigation** | Smooth vertical paging |
| **Metadata** | Word, definition, example, part of speech, difficulty |
| **State** | Loading, Success, Error states |
| **Data** | 10 sample words (LocalDataSource) |

---

## 🏗️ Architecture Overview

### Three Layers

```
┌─────────────────┐
│   UI Layer      │  ← WordsScreen, Composables, ViewModel
├─────────────────┤
│  Domain Layer   │  ← UseCase, Repository interface, Models
├─────────────────┤
│   Data Layer    │  ← Repository impl, DataSources
└─────────────────┘
```

### Data Flow

```
User Action → ViewModel → UseCase → Repository → DataSource → Flow → UI
```

---

## 🛠️ Tech Stack

| Component | Library | Version |
|-----------|---------|---------|
| UI Framework | Jetpack Compose | Latest BOM |
| DI Framework | Hilt | 2.51.1 |
| State Management | StateFlow/SharedFlow | Coroutines 1.8.1 |
| Navigation | VerticalPager | Foundation |
| Architecture | MVVM + Clean | Custom pattern |
| Language | Kotlin | 2.0.21 |
| Build System | Gradle | 8.9.1 |

---

## 📂 Important Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Entry point, Hilt-enabled |
| `WordsViewModel.kt` | State management, business logic |
| `WordsScreen.kt` | Main UI composables |
| `GetWordsUseCase.kt` | Business logic |
| `WordRepository.kt` | Data contract |
| `WordRepositoryImpl.kt` | Data implementation |
| `LocalDataSource.kt` | Mock data |
| `RepositoryModule.kt` | DI configuration |

---

## 🚀 Quick Commands

```bash
# Build
./gradlew clean build

# Run tests
./gradlew test

# Install on device
./gradlew installDebug

# Create release APK
./gradlew assembleRelease
```

---

## 📊 State Management

### ViewModel Pattern
```kotlin
@HiltViewModel
class WordsViewModel @Inject constructor(
    useCase: GetWordsUseCase
) : ViewModel() {
    val uiState: StateFlow<WordsUiState>  // Observable state
    val errorEvent: SharedFlow<String>    // One-time events
}
```

### State Types
- `Loading` - Fetching data
- `Success(words)` - Data ready
- `Error(message)` - Something went wrong

### UI Observer
```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

---

## 🎨 UI Components

| Component | Purpose |
|-----------|---------|
| `WordsScreen` | Main container, observes state |
| `WordsPager` | VerticalPager wrapper |
| `WordCard` | Single word fullscreen display |
| `DifficultyBadge` | Difficulty indicator (1-5 stars) |
| `ErrorMessage` | Error state display |

---

## 🧪 Testing

### Test Files
- `WordsViewModelTest.kt` - ViewModel logic
- `GetWordsUseCaseTest.kt` - UseCase functionality
- `WordRepositoryImplTest.kt` - Repository implementation

### Run Tests
```bash
./gradlew test

# Specific test
./gradlew test --tests WordsViewModelTest

# With coverage
./gradlew testDebugUnitTestCoverage
```

---

## 📚 Documentation

| File | Contains |
|------|----------|
| `README.md` | Features, setup, quick start |
| `ARCHITECTURE.md` | Deep architecture explanation |
| `DEVELOPMENT.md` | Development guide, examples |
| `IMPLEMENTATION_SUMMARY.md` | Completion status, overview |

---

## 💡 Key Concepts

### Unidirectional Data Flow
```
One-way flow from ViewModel → UI
Never from UI → ViewModel
Events handled through callbacks
```

### Pure Composables
```kotlin
// ✅ Good - Pure function
@Composable
fun WordCard(word: Word) { ... }

// ❌ Bad - Side effect
@Composable
fun WordCard(word: Word) {
    LaunchedEffect(Unit) { loadData() }  // NO!
}
```

### Sealed Classes
```kotlin
// Type-safe states
sealed class WordsUiState {
    data object Loading : WordsUiState()
    data class Success(val words: List<Word>) : WordsUiState()
    data class Error(val message: String) : WordsUiState()
}

// Exhaustive when
when (state) {
    is Loading -> LoadingUI()
    is Success -> SuccessUI(state.words)
    is Error -> ErrorUI(state.message)
}  // Compiler ensures all cases handled
```

### Hilt Injection
```kotlin
// 1. Mark app with @HiltAndroidApp
@HiltAndroidApp
class WordsSwipeApplication : Application()

// 2. Mark activity/VM with appropriate annotations
@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class WordsViewModel @Inject constructor(useCase: GetWordsUseCase)

// 3. Create DI modules for bindings
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule { ... }
```

---

## 🔧 Common Tasks

### Adding a New Feature

**Step 1: Domain Layer**
```kotlin
// Add UseCase
class MyNewUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<Result> = repository.getData()
}
```

**Step 2: Data Layer**
```kotlin
// Update repository interface and implementation
interface WordRepository {
    fun getData(): Flow<Result>
}

class WordRepositoryImpl @Inject constructor(
    private val dataSource: LocalDataSource
) : WordRepository {
    override fun getData(): Flow<Result> = flow {
        emit(dataSource.data)
    }.flowOn(Dispatchers.IO)
}
```

**Step 3: UI Layer**
```kotlin
// Update ViewModel
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val myNewUseCase: MyNewUseCase
) : ViewModel() {
    val newState = MutableStateFlow<Result>()
    
    fun loadNewData() {
        viewModelScope.launch {
            myNewUseCase().collect { result ->
                newState.value = result
            }
        }
    }
}

// Update Composable
@Composable
fun WordsScreen(viewModel: WordsViewModel) {
    val result by viewModel.newState.collectAsStateWithLifecycle()
    // Use result
}
```

---

## ⚠️ Common Mistakes to Avoid

| ❌ Wrong | ✅ Right |
|---------|---------|
| Business logic in Composable | Business logic in ViewModel/UseCase |
| Multiple state holders | Single StateFlow source of truth |
| GlobalScope.launch | viewModelScope.launch |
| Null-safe operators everywhere | Sealed classes for states |
| Side effects in Composable | Side effects in ViewModel init |
| Mutable state exposed | Immutable StateFlow exposed |

---

## 📖 Learning Path

1. **Start with** `README.md` - Understand what the app does
2. **Read** `ARCHITECTURE.md` - Learn the architecture decisions
3. **Study** `MainActivity.kt` → `WordsViewModel.kt` → `WordsScreen.kt` - UI flow
4. **Explore** `GetWordsUseCase.kt` → `WordRepository.kt` → `LocalDataSource.kt` - Data flow
5. **Review** `DEVELOPMENT.md` - Learn how to extend
6. **Check** Unit tests - See how to test each layer

---

## 🎯 Architecture Checklist

When adding new features, ensure:
- [ ] Business logic in UseCase/ViewModel, not Composable
- [ ] State in StateFlow, not in Composable
- [ ] ViewModels use @HiltViewModel
- [ ] Repositories injected via Hilt
- [ ] No Android imports in domain layer
- [ ] Sealed classes for state types
- [ ] Immutable data classes
- [ ] Proper error handling
- [ ] Unit tests for new logic
- [ ] Documentation updated

---

## 📞 Quick Help

| Problem | Solution |
|---------|----------|
| Build fails | `./gradlew clean build` |
| ViewModel injection error | Add `@HiltViewModel` to class |
| State not updating | Use `collectAsStateWithLifecycle()` |
| Recomposition issues | Make state immutable data classes |
| Coroutine leak | Use `viewModelScope.launch` |
| Repository missing | Add Hilt binding in RepositoryModule |

---

## 🎓 Key Takeaways

✅ **Clean Architecture** - Scalable, testable, maintainable
✅ **MVVM + Unidirectional Flow** - Predictable state management
✅ **Hilt DI** - Type-safe, compile-time verification
✅ **Sealed Classes** - Type-safe state transitions
✅ **Flow/Coroutines** - Reactive, efficient data handling
✅ **Pure Composables** - Reusable, testable UI components
✅ **Well Documented** - Easy for team onboarding

---

## 🚀 Next Steps

1. **Build & Run** - `./gradlew installDebug`
2. **Explore Code** - Start with MainActivity.kt
3. **Run Tests** - `./gradlew test`
4. **Add Feature** - Follow DEVELOPMENT.md example
5. **Deploy** - `./gradlew assembleRelease`

---

**Happy coding! 🎉**
