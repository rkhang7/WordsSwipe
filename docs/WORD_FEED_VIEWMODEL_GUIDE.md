# WordFeedViewModel - Implementation Guide

## Overview

The `WordFeedViewModel` manages the word feed UI with sophisticated state management, preloading logic, and error handling. It implements a TikTok-style infinite scroll experience with proper resource management.

---

## 📊 Architecture

### State Model

```
WordFeedViewModel
├─ StateFlow<List<WordPage>>     ← pages
├─ StateFlow<Int>                ← currentIndex
├─ StateFlow<WordFeedUiState>    ← uiState
├─ SharedFlow<String>            ← errorEvent
└─ SharedFlow<Unit>              ← retryEvent
```

### Data Models

**WordPage**: Represents a single word in the feed
```kotlin
data class WordPage(
    val word: String,                    // The word
    val wordDetail: WordDetail? = null,  // API details (null while loading)
    val isLoading: Boolean = false,      // Loading state
    val error: String? = null            // Error message if fetch failed
)
```

**WordFeedUiState**: Overall feed state
```kotlin
sealed class WordFeedUiState {
    data object Loading : WordFeedUiState()
    data object Success : WordFeedUiState()
    data class Error(val message: String) : WordFeedUiState()
}
```

---

## 🎯 Features

### 1. Initialization (App Launch)
- **Clear History**: Starts fresh on every app launch
- **Load Initial Batch**: Fetches 5 random words via WordsRepository
- **Parallel API Calls**: Fetches API details for all 5 words simultaneously
- **State Transition**: Loading → Success (or Error)

### 2. Navigation (Swipe Gestures)
- **Swipe Down**: Moves to next page (currentIndex++)
  - Blocked at end of list
  - Triggers preload when near end
- **Swipe Up**: Moves to previous page (currentIndex--)
  - Blocked at index 0
  - No preload needed

### 3. Preloading (Intelligent Batching)
- **Threshold**: When currentIndex >= pages.size - 2
- **Batch Size**: 5 new words per preload
- **Parallel Loading**: All 5 new words fetched simultaneously
- **Non-blocking**: Doesn't pause user interaction

### 4. Error Handling
- **Page-Level Errors**: Individual pages can fail to load
- **Batch Errors**: Preload failures don't stop feed
- **Retry Logic**: Users can retry failed pages
- **Error Events**: SharedFlow emits for UI toast/snackbar

---

## 💻 Usage Examples

### Basic Setup (Compose)

```kotlin
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel = hiltViewModel()
) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            // Show error toast
            showToast(message)
        }
    }
    
    when (uiState) {
        WordFeedUiState.Loading -> {
            LoadingScreen()
        }
        WordFeedUiState.Success -> {
            if (pages.isNotEmpty()) {
                val currentPage = pages[currentIndex]
                WordPagerScreen(
                    page = currentPage,
                    onSwipeDown = { viewModel.swipeDown() },
                    onSwipeUp = { viewModel.swipeUp() }
                )
            }
        }
        is WordFeedUiState.Error -> {
            ErrorScreen(
                message = (uiState as WordFeedUiState.Error).message,
                onRetry = { viewModel.retryLoading() }
            )
        }
    }
}
```

### Gesture Handling

```kotlin
@Composable
fun WordPagerScreen(
    page: WordPage,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit
) {
    Gesture {
        onVerticalDrag { offset ->
            when {
                offset > SWIPE_THRESHOLD -> onSwipeDown()
                offset < -SWIPE_THRESHOLD -> onSwipeUp()
            }
        }
    }
    
    Column {
        Text(page.word, style = Typography.headlineLarge)
        
        if (page.isLoading) {
            CircularProgressIndicator()
        } else if (page.error != null) {
            ErrorMessage(
                message = page.error,
                onRetry = { /* call viewModel.retryPageLoading(index) */ }
            )
        } else if (page.wordDetail != null) {
            WordDetails(page.wordDetail)
        }
    }
}
```

### Progress Display

```kotlin
@Composable
fun ProgressBar(viewModel: WordFeedViewModel) {
    val (current, total) = viewModel.getProgress()
    
    Text("$current of $total")
    LinearProgressIndicator(
        progress = { current.toFloat() / total }
    )
}
```

---

## 📈 State Flow

### Initialization Flow

```
App Launch
    ↓
init {} block executes
    ↓
uiState = Loading
pages = []
currentIndex = 0
    ↓
Load 5 random words from WordsRepository
    ↓
Create WordPage objects (isLoading=true)
pages = [5 loading pages]
    ↓
Fetch API details in parallel (5 coroutines)
    ↓
As each completes:
  pages[i] = pages[i].copy(wordDetail=result, isLoading=false)
    ↓
All complete:
uiState = Success
```

### Preload Flow

```
User swipes to index 3 (in list of 5)
    ↓
currentIndex becomes 4
    ↓
shouldPreload(4) checks: 5 - 4 = 1 item remaining
    ↓
1 <= PRELOAD_THRESHOLD (2) → YES
    ↓
Load 5 more random words
    ↓
pages = [original 5 + 5 new loading pages]
    ↓
Fetch API details for new pages in parallel
    ↓
As each completes, update the page
```

### Error Flow

```
API fetch fails for page[2]
    ↓
catch block executes
    ↓
updatePage(2) with error message
pages[2].error = "Failed to fetch: 404"
pages[2].isLoading = false
    ↓
Emit error event
    ↓
UI displays error state for this page
    ↓
User taps retry
    ↓
viewModel.retryPageLoading(2)
    ↓
Repeat fetch for page[2]
```

---

## 🔧 Configuration

### Constants

```kotlin
private const val INITIAL_WORDS_COUNT = 5    // First load
private const val PRELOAD_BATCH_SIZE = 5     // Per preload
private const val PRELOAD_THRESHOLD = 2      // Trigger distance from end
```

### Customization Examples

```kotlin
// Increase initial load
private const val INITIAL_WORDS_COUNT = 10

// Load more words per preload
private const val PRELOAD_BATCH_SIZE = 10

// Preload earlier (more aggressive)
private const val PRELOAD_THRESHOLD = 5

// Preload later (more lazy)
private const val PRELOAD_THRESHOLD = 1
```

---

## 🧪 Testing

### Test Coverage

| Test | Purpose |
|------|---------|
| initialize_StartsWithLoadingState | Initial state is Loading |
| initialize_LoadsInitialWords | 5 words loaded on init |
| swipeDown_IncrementsIndex | Navigation works down |
| swipeUp_DecrementsIndex | Navigation works up |
| swipeUp_BlockedAtStart | Can't go before index 0 |
| swipeDown_BlockedAtEnd | Can't go past last page |
| getCurrentPage_Returns | Get current page method |
| getProgress_ReturnsCorrect | Progress calculation |
| wordPages_HaveCorrect | Page structure validation |
| retryLoading_Reinitializes | Retry logic works |

### Running Tests

```bash
./gradlew test
```

---

## 📊 Parallel Loading Details

### Initial Load (5 words)

```kotlin
viewModelScope.launch { // Main coroutine
    val words = getRandomWords(5)           // Sequential - returns list
    val pages = createWordPages(words)      // Sequential - create objects
    pages.forEach { index ->
        viewModelScope.launch {              // Parallel coroutine
            val detail = getWordDetailUseCase(word)
            updatePage(index) { it.copy(...) }
        }
    }
}
// 5 parallel API calls, NOT blocking each other
```

### Preload (5 more words)

```kotlin
viewModelScope.launch {
    val newWords = getRandomWords(5)       // Load 5 more words
    val newPages = createWordPages(newWords)
    pages.addAll(newPages)
    
    // Fetch all 5 in parallel
    newPages.forEachIndexed { index, page ->
        viewModelScope.launch {
            val detail = getWordDetailUseCase(page.word)
            updatePage(currentPages.size + index) { ... }
        }
    }
}
// Again: 5 parallel calls
```

---

## 🎯 Best Practices

### Do's
✅ Use StateFlow for observable state
✅ Use SharedFlow for one-time events
✅ Launch API calls in separate coroutines
✅ Update UI state immutably
✅ Handle errors per-page and globally
✅ Test state transitions

### Don'ts
❌ Don't block on API calls
❌ Don't update state inside `try-finally`
❌ Don't use GlobalScope
❌ Don't emit error events without handling
❌ Don't allow index out of bounds

---

## 📱 Integration Checklist

- [ ] Inject WordFeedViewModel in Composable
- [ ] Collect pages, currentIndex, uiState
- [ ] Handle errorEvent with toast/snackbar
- [ ] Implement swipe gestures
- [ ] Call swipeDown() on down swipe
- [ ] Call swipeUp() on up swipe
- [ ] Display CurrentPage content
- [ ] Show loading spinner for page.isLoading
- [ ] Show error message for page.error
- [ ] Show retry button with viewModel.retryPageLoading(index)
- [ ] Display progress with viewModel.getProgress()

---

## 🚀 Performance

### Memory
- Pages cached in memory (~50KB per page)
- Preload doesn't load all at once
- Old pages can be GC'd if pageable list implemented

### Network
- Parallel API calls minimize latency
- Preloading hides network delay
- Failed pages don't block others

### CPU
- Minimal processing per page
- JSON parsing is fast
- State updates are efficient

---

## 🔍 Debugging

### Check Current State

```kotlin
val pages = viewModel.pages.value
val index = viewModel.currentIndex.value
val state = viewModel.uiState.value

println("Pages: ${pages.size}")
println("Index: $index")
println("State: $state")
```

### Monitor Preloads

```kotlin
// Add logging in preloadNextBatch()
Log.d("WordFeed", "Preload triggered at index $nextIndex")
Log.d("WordFeed", "Now have ${newPages.size} pages")
```

### Error Investigation

```kotlin
viewModel.errorEvent.collect { message ->
    Log.e("WordFeed", "Error: $message")
}
```

---

## 📞 Troubleshooting

| Issue | Solution |
|-------|----------|
| Pages not loading | Check uiState is Success |
| Infinite loop on preload | Verify threshold logic |
| Swipe not working | Verify currentIndex changes |
| Errors not showing | Check errorEvent collection |
| Memory leak | Verify collect cancellation |
| Stale data | Check cache invalidation |

---

## Summary

The WordFeedViewModel provides:
- ✅ Sophisticated state management (StateFlow)
- ✅ Event handling (SharedFlow)
- ✅ Parallel API loading
- ✅ Intelligent preloading
- ✅ Error recovery
- ✅ Memory efficient design
- ✅ Clean API for UI integration

Ready for production use! 🚀

