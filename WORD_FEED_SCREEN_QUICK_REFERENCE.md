# WordFeedScreen Quick Reference

## Overview
Production-ready TikTok-style vertical swipe screen for the WordsSwipe app using Jetpack Compose and VerticalPager.

## File Location
```
app/src/main/java/com/example/wordsswipe/ui/screen/feed/WordFeedScreen.kt
```

## Public API

### Main Composable
```kotlin
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel,
    modifier: Modifier = Modifier
)
```

**Usage in MainActivity:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordsSwipeTheme {
                val wordFeedViewModel: WordFeedViewModel = hiltViewModel()
                WordFeedScreen(
                    viewModel = wordFeedViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

## Component Hierarchy

```
WordFeedScreen
├── Loading State
│   └── CircularProgressIndicator + Text
├── Error State
│   └── Error Message + Retry Button
└── Success State
    └── WordFeedPager
        └── WordCardPage (repeated for each page)
            ├── Loading Page
            ├── Error Page
            └── Success Page (WordDetailContent)
                ├── Word
                ├── Phonetic
                ├── Meaning (Part of Speech)
                ├── Definition
                └── Example
```

## State Management

### Observable States (from ViewModel)

1. **uiState: StateFlow<WordFeedUiState>**
   - `Loading` - Initial load
   - `Success` - Ready to display pages
   - `Error(message)` - Feed-level error

2. **pages: StateFlow<List<WordPage>>**
   - List of word pages
   - Each page contains word + optional details
   - Updated when preloading occurs

3. **currentIndex: StateFlow<Int>**
   - Current page index being displayed
   - Synced with VerticalPager
   - Managed by ViewModel

### Events (from ViewModel)

```kotlin
viewModel.swipeDown()      // Move to next page
viewModel.swipeUp()        // Move to previous page
viewModel.retryLoading()   // Retry feed load
viewModel.retryPageLoading(index)  // Retry single page
```

## User Interactions

| Gesture | Action | Behavior |
|---------|--------|----------|
| Swipe DOWN | Next word | Triggers `viewModel.swipeDown()` |
| Swipe UP | Previous word | Triggers `viewModel.swipeUp()` |
| At first page, swipe UP | No-op | ViewModel prevents going below index 0 |
| Tap "Retry" (error) | Retry | Calls `viewModel.retryLoading()` or `viewModel.retryPageLoading(index)` |

## Screen States

### 1. Loading State
```
┌─────────────────────────┐
│                         │
│   ⏳ (spinner)          │
│   Loading words...      │
│                         │
└─────────────────────────┘
```
- Shown while initial 5 words are being fetched
- Displays during app startup

### 2. Success State - Normal Word Display
```
┌─────────────────────────┐
│                         │
│   PHONETIC              │  ← 48.sp, bold
│   /fə'netɪk/            │  ← 18.sp, gray
│                         │
│   noun                  │  ← 16.sp, primary
│                         │
│   The sound structure   │  ← 18.sp, body
│   of a word...          │
│                         │
│   "She studied the      │  ← 14.sp, italic
│    phonetic proper..."  │
│                         │
└─────────────────────────┘
```
- Full word detail displayed
- No scrolling within page

### 3. Loading State Per-Page
```
┌─────────────────────────┐
│                         │
│   ⏳ (spinner)          │
│   Loading: phonetic     │
│                         │
└─────────────────────────┘
```
- While API data is being fetched for specific page
- Happens as user swipes to new pages

### 4. Error State - Feed Level
```
┌─────────────────────────┐
│                         │
│   Oops!                 │
│                         │
│   Failed to load words  │
│   [Retry Button]        │
│                         │
└─────────────────────────┘
```
- Shown if initial load fails
- User must retry to continue

### 5. Error State - Page Level
```
┌─────────────────────────┐
│                         │
│   PHONETIC              │
│                         │
│   Error: Failed to      │
│   fetch: Connection     │
│   timeout               │
│   [Retry Button]        │
│                         │
└─────────────────────────┘
```
- Single page failed
- User can retry just that page
- Can swipe to other pages

## Key Features

### ✅ Fullscreen Layout
- Each word occupies entire screen
- No internal scrolling
- Clean, distraction-free UX

### ✅ Smooth Animations
- VerticalPager handles swipe animations
- `animateScrollToPage()` for programmatic navigation
- Gesture-driven transitions

### ✅ Persistent Pager State
```kotlin
val pagerState = rememberPagerState(
    initialPage = currentIndex,
    pageCount = { pages.size }
)
```
- Survives recomposition
- Doesn't reset when pages list grows
- Syncs with ViewModel

### ✅ Preload Synchronization
- Pager automatically accepts new pages
- No jumping back to previous page
- Seamless infinite scroll

### ✅ Lifecycle-Aware
- Uses `collectAsStateWithLifecycle()`
- Pauses collection when app backgrounded
- Memory efficient

### ✅ Error Recovery
- Feed-level errors with retry
- Page-level errors with recovery
- Graceful degradation

## Detailed Data Flow

### Initial Load
```
1. MainActivity launches
   ↓
2. WordFeedViewModel created (via Hilt)
   ↓
3. ViewModel.init {} calls initializeFeed()
   ├── Load 5 random words from repository
   ├── Create WordPage objects (loading state)
   ├── Update pages StateFlow
   ├── Launch API fetches (parallel)
   └── Update each page with result
   ↓
4. WordFeedScreen mounts
   ├── Collects uiState → Loading
   ├── Collects pages → [5 WordPages]
   ├── Collects currentIndex → 0
   ↓
5. Renders WordFeedPager
   ├── Creates rememberPagerState (initialPage: 0)
   ├── VerticalPager renders pages[0]
   ├── Shows loading spinner for pages[0]
   ↓
6. API fetches complete
   ├── ViewModel updates pages[0].wordDetail
   ├── pages StateFlow updated
   ↓
7. WordFeedPager recomposes
   ├── pagerState persists (still at 0)
   ├── Renders WordDetailContent for pages[0]
   ↓
8. User sees word definition
```

### User Swipes Down
```
1. User swipes down on pagerState
   ↓
2. pagerState.currentPage changes from 0 → 1
   ↓
3. LaunchedEffect(pagerState.currentPage) triggers
   ├── Detects change from 0 → 1
   ├── Calls onSwipeDown()
   ↓
4. onSwipeDown() calls ViewModel.swipeDown()
   ├── ViewModel validates index
   ├── Updates currentIndex: 0 → 1
   ├── Checks if preload needed
   ├── Emits preload if at threshold
   ↓
5. currentIndex StateFlow updates
   ↓
6. WordFeedPager recomposes (sees new currentIndex)
   ├── pagerState still at page 1 (state remembered)
   ├── VerticalPager renders pages[1]
   ↓
7. User sees next word
```

### Preload Triggered
```
1. User swipes to index 3 (at threshold)
   ↓
2. ViewModel.swipeDown() detects: 5 - 3 = 2 (at threshold)
   ↓
3. Preload triggered
   ├── Load 5 more random words
   ├── Create WordPage objects
   ├── pages list: [5] → [10]
   ├── Launch API fetches for new pages
   ↓
4. pages StateFlow updates [5] → [10]
   ↓
5. WordFeedPager recomposes
   ├── pageCount lambda returns 10
   ├── pagerState still at page 3 (state remembered)
   ├── Accepts new page range
   ↓
6. User can continue swiping without interruption
```

## VerticalPager Parameters

```kotlin
VerticalPager(
    state = pagerState,              // Persistent state
    modifier = modifier.fillMaxSize(), // Full screen
    userScrollEnabled = true          // Allow swipe gestures
)
```

**Removed Parameters:**
- ~~pageNestedScrollConnection~~ - Not needed for fullscreen
- ~~flingBehavior~~ - Uses defaults
- ~~pageSize~~ - Uses fill (default)
- ~~contentPadding~~ - None (fullscreen)

## Performance Optimizations

1. **State Persistence**
   - PagerState not recreated
   - Only recomposes when state changes

2. **Lazy Rendering**
   - Only current + adjacent pages composed
   - VerticalPager handles efficiently

3. **Efficient State Updates**
   - StateFlow batches updates
   - Lifecycle-aware collection

4. **Parallel API Fetches**
   - New pages fetched in parallel
   - Non-blocking UI

## Common Issues & Solutions

### Pager Resets on Preload
**Problem:** User is at page 3, preload adds pages 6-10, user jumps back to page 0

**Solution:** Already implemented
- `pageCount = { pages.size }` - Dynamic lambda
- `rememberPagerState()` - State preservation
- No manual page index management

### Swipe Doesn't Work
**Problem:** User can't swipe vertically

**Cause:** `userScrollEnabled = false`

**Solution:** Verify `userScrollEnabled = true` in VerticalPager parameters

### State Not Updating
**Problem:** Screen doesn't update when ViewModel state changes

**Cause:** Not collecting state properly

**Solution:**
```kotlin
// ✅ Correct
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ❌ Wrong
LaunchedEffect(Unit) {
    viewModel.uiState.collect { ... }
}
```

### Loading State Stuck
**Problem:** Loading spinner never goes away

**Cause:** API fetch error not being caught

**Solution:**
1. Check ViewModel error handling
2. Verify API endpoint is reachable
3. Check internet connectivity
4. Use retry button

## Testing

### Unit Tests
- ViewModel swipe logic
- State transitions
- Preload triggering

### Compose UI Tests
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testWordDisplayed() {
    composeTestRule.setContent {
        WordFeedScreen(mockViewModel)
    }
    
    composeTestRule
        .onNodeWithText("PHONETIC")
        .assertIsDisplayed()
}
```

### Manual Testing Checklist
- [ ] App launches - shows loading state
- [ ] After ~2 seconds - shows first word
- [ ] Swipe down - shows next word smoothly
- [ ] Swipe up - goes back to previous word
- [ ] At first page, swipe up - nothing happens
- [ ] Swipe to end of loaded words - preload triggers
- [ ] Continue swiping - preloaded words appear
- [ ] Tap retry on error - recovers gracefully

## Related Files

```
ui/screen/feed/
├── WordFeedScreen.kt (415 lines)      ← Main implementation
├── WordFeedViewModel.kt (338 lines)   ← State & logic
└── WordFeedUiState.kt (25 lines)      ← UI state sealed class

domain/model/
├── WordPage.kt                        ← Page model
├── WordDetail.kt                      ← Word detail model
└── Word.kt                            ← Word model

data/
├── local/WordsRepository.kt           ← Random words source
└── remote/                            ← API integration
```

## Summary

**WordFeedScreen** provides a production-ready TikTok-style learning interface:

✅ Fullscreen word cards (no scrolling)
✅ Smooth vertical swipe navigation  
✅ Persistent pager state (handles preload)
✅ Lifecycle-aware state collection
✅ Robust error handling
✅ Seamless preload synchronization
✅ Clean unidirectional data flow
✅ No business logic in UI layer

**Time to Implement:** ~415 lines
**Complexity:** Medium (handles edge cases well)
**Best Practices:** All followed ✓
