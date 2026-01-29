# WordFeedScreen Implementation Guide

## Overview

`WordFeedScreen` is the main UI component for the WordsSwipe app, implementing a TikTok-style vertical swipe interface for learning English words. It uses Jetpack Compose with `VerticalPager` to provide smooth, fullscreen word cards without internal scrolling.

## Architecture

### Component Hierarchy

```
WordFeedScreen (main composable)
│
├── Observes:
│   ├── WordFeedViewModel.uiState (StateFlow<WordFeedUiState>)
│   ├── WordFeedViewModel.pages (StateFlow<List<WordPage>>)
│   └── WordFeedViewModel.currentIndex (StateFlow<Int>)
│
└── Renders:
    ├── LoadingState (initial load)
    ├── ErrorState (error handling)
    └── WordFeedPager (success state)
        └── WordCardPage (per word)
            ├── WordDetailContent (success)
            ├── Loading state (per page)
            └── Error state (per page)
```

## Key Features

### 1. Fullscreen Layout
- No scrollable content within a page
- Each word occupies the entire screen
- Clean, distraction-free learning experience

### 2. VerticalPager Integration
- **Smooth animations** - Built-in swipe transitions
- **Persistent state** - `rememberPagerState()` prevents resets on recomposition
- **Dynamic page count** - Accepts new pages as they're preloaded
- **User-controlled scrolling** - `userScrollEnabled = true`

### 3. Swipe Navigation
- **Swipe DOWN** → Move to next word (index increases)
- **Swipe UP** → Move to previous word (index decreases)
- **Boundary protection** → Cannot swipe up from first page

### 4. State Management
```
ViewModel State Flow:
┌─────────────────────────────────────┐
│ WordFeedViewModel                   │
│ ┌─────────────────────────────────┐ │
│ │ uiState: StateFlow<UiState>     │ │
│ │ - Loading                       │ │
│ │ - Success                       │ │
│ │ - Error                         │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ pages: StateFlow<List<WordPage>>│ │
│ │ - Contains all loaded pages     │ │
│ │ - Updated on preload            │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ currentIndex: StateFlow<Int>    │ │
│ │ - Current page being displayed  │ │
│ │ - Synced with pager             │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘

UI Observation:
WordFeedScreen collects state via collectAsStateWithLifecycle()
This ensures:
- State updates trigger recomposition
- Lifecycle-aware collection (pauses when app is backgrounded)
- Memory efficient
```

## Component Breakdown

### `WordFeedScreen`
**Main composable that manages overall state**

```kotlin
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel,
    modifier: Modifier = Modifier
)
```

**Responsibilities:**
- Collect ViewModel state flows
- Route to appropriate UI state (Loading/Success/Error)
- Pass state to WordFeedPager when ready

**State Flow:**
```
Loading → CircularProgressIndicator + "Loading words..." text
Success → WordFeedPager (if pages not empty) OR ErrorState
Error → ErrorState with retry button
```

### `WordFeedPager`
**Manages VerticalPager and swipe synchronization**

```kotlin
@Composable
private fun WordFeedPager(
    pages: List<WordPage>,
    currentIndex: Int,
    onIndexChanged: (Int) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onRetryPage: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**Key Implementation Details:**

1. **PagerState Persistence**
   ```kotlin
   val pagerState = rememberPagerState(
       initialPage = currentIndex,
       pageCount = { pages.size }
   )
   ```
   - `rememberPagerState()` - Survives recompositions
   - `pageCount = { pages.size }` - Dynamic page count
   - Updates when pages list grows

2. **Synchronization with ViewModel**
   ```kotlin
   LaunchedEffect(currentIndex) {
       if (pagerState.currentPage != currentIndex) {
           coroutineScope.launch {
               pagerState.animateScrollToPage(currentIndex)
           }
       }
   }
   ```
   - Programmatic navigation works smoothly
   - Animations enabled via `animateScrollToPage()`

3. **Swipe Event Handling**
   ```kotlin
   LaunchedEffect(pagerState.currentPage) {
       val newIndex = pagerState.currentPage
       if (newIndex != currentIndex) {
           if (newIndex > currentIndex) {
               onSwipeDown()  // Index increased = swiped down
           } else {
               onSwipeUp()    // Index decreased = swiped up
           }
       }
   }
   ```
   - Detects pager index changes
   - Translates to semantically meaningful actions
   - Calls ViewModel methods which validate bounds

### `WordCardPage`
**Individual word card with loading/error/success states**

```kotlin
@Composable
private fun WordCardPage(
    page: WordPage,
    pageIndex: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)
```

**States:**

1. **Loading** - While API data is being fetched
   ```
   CircularProgressIndicator
   + "Loading: {word}" text
   ```

2. **Error** - If API call failed
   ```
   Word (large display)
   + Error message
   + Retry button
   ```

3. **Success** - Full word detail
   ```
   → WordDetailContent
   ```

### `WordDetailContent`
**Displays complete word information without scrolling**

```kotlin
@Composable
private fun WordDetailContent(
    word: String,
    wordDetail: WordDetail,
    modifier: Modifier = Modifier
)
```

**Layout (vertical, fullscreen):**
```
┌─────────────────────────────┐
│     WORD (uppercase)        │  48.sp, Bold
├─────────────────────────────┤
│        /fə'netɪk/            │  18.sp, Gray (if available)
├─────────────────────────────┤
│            noun              │  16.sp, Primary color
├─────────────────────────────┤
│  The sound structure of      │  18.sp, Body
│  a word or language.         │
├─────────────────────────────┤
│ "She studied the phonetic   │  14.sp, Italic, Gray
│  properties of English."    │
└─────────────────────────────┘
```

**Displayed Information:**
1. **Word** - Uppercase, large (48.sp)
2. **Phonetic** - Pronunciation (18.sp), if available
3. **Part of Speech** - First meaning only (16.sp, primary color)
4. **Definition** - First definition (18.sp)
5. **Example** - First example sentence (14.sp, italic)

**Design Philosophy:**
- Only first definition shown (no overwhelming detail)
- Fits entirely on screen (no scrolling)
- Clean typography hierarchy
- Focus on learning key concepts

## Pager State Management

### Problem Solved
Without proper state management, `VerticalPager` would reset when:
- Pages list size changes (preload adds items)
- Composable recomposes
- ViewModel state updates

### Solution
```kotlin
val pagerState = rememberPagerState(
    initialPage = currentIndex,
    pageCount = { pages.size }  // Dynamic function
)
```

**Why This Works:**
1. `rememberPagerState()` uses `saveable` internally
2. State persists across recompositions
3. `pageCount = { ... }` is a lambda, evaluated each recomposition
4. Pager dynamically accepts new pages without resetting
5. Current page preserved even if list grows

### Example Scenario
```
Initial: [word1, word2, word3, word4, word5]
pagerState.currentPage = 0
User at word1

Preload triggered at word3:
[word1, word2, word3, word4, word5] + [word6, word7, word8, word9, word10]

pagerState still remembers index 0
pagerState accepts extended pageCount (10)
User remains on word1, can continue swiping
```

## Navigation Flow

```
User Swipe DOWN
    ↓
VerticalPager detects scroll
    ↓
pagerState.currentPage increases
    ↓
LaunchedEffect detects pagerState change
    ↓
onSwipeDown() called
    ↓
ViewModel.swipeDown()
    ├── currentIndex increased (if not at end)
    ├── Check if preload needed
    └── Emit preload if at threshold
    ↓
ViewModel.currentIndex StateFlow updates
    ↓
WordFeedScreen recomposes
    ↓
Pager still at same page (state remembered)
User sees next word
```

## Error Handling

### Page-Level Errors
When a single word's API fetch fails:
```kotlin
page.error = "Failed to fetch: Connection timeout"
```

UI shows:
1. Word name (can still display)
2. Error message
3. Retry button for that page only

### Feed-Level Errors
When initial load or preload fails entirely:
```kotlin
_uiState = WordFeedUiState.Error("Failed to load words")
```

UI shows:
1. Full-screen error message
2. Retry button to reload entire feed
3. Clears history and restarts

## Best Practices Implemented

### ✅ Unidirectional Data Flow
- UI observes ViewModel state
- UI events call ViewModel methods
- ViewModel manages business logic
- No state in composables

### ✅ State Stability
- `rememberPagerState()` persists across recompositions
- StateFlow used for observable state
- Only collect state once per composable level
- No state recreation

### ✅ Lifecycle Awareness
- `collectAsStateWithLifecycle()` pauses when app backgrounded
- Avoids wasted state updates when not visible
- Proper coroutine scoping

### ✅ Composition Safety
- No side effects in composable body
- `LaunchedEffect` blocks handle side effects
- Pure functions (Composables)
- Predictable recomposition

### ✅ Performance
- No recomposition of unchanged subtrees
- PagerState not recreated
- Only necessary items rendered in Pager
- Efficient state updates

## Integration with MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordsSwipeTheme {
                // Hilt injects WordFeedViewModel
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

**Flow:**
1. Activity starts
2. Hilt creates WordFeedViewModel
3. ViewModel's `init {}` initializes feed
4. WordFeedScreen mounts
5. Collects state
6. Displays words

## Testing Considerations

### Unit Tests
- ViewModel state transitions
- Swipe logic (up/down validation)
- Preload triggering
- Error handling

### UI Tests
- Pager renders correct pages
- Swipe gestures work
- State syncs between ViewModel and UI
- Loading/Error states display correctly

### Integration Tests
- Full flow: load → display → swipe → preload
- Error recovery with retry
- Page boundary conditions

## Potential Enhancements

1. **Haptic Feedback** - Vibration on swipe
2. **Scroll Progress Indicator** - Show position in list
3. **Bookmarks** - Save favorite words
4. **Audio Pronunciation** - Play phonetic audio
5. **Swipe Velocity** - Skip multiple pages on fast swipe
6. **Infinite Scroll** - Load more words when preload exhausted
7. **Keyboard Navigation** - Arrow keys for desktop testing

## Troubleshooting

### Issue: Pager resets on preload
**Cause:** `rememberPagerState()` created with wrong key
**Solution:** Use dynamic `pageCount = { pages.size }`

### Issue: Swipe doesn't work
**Cause:** `userScrollEnabled = false`
**Solution:** Ensure `userScrollEnabled = true` in VerticalPager

### Issue: State not updating on screen
**Cause:** Not collecting state with lifecycle awareness
**Solution:** Use `collectAsStateWithLifecycle()` instead of `collect()`

### Issue: Loading state stuck
**Cause:** Error in API fetch, exception not caught
**Solution:** Check ViewModel error handling, check API response

## Files Modified
- `MainActivity.kt` - Updated to use WordFeedScreen
- Created `WordFeedScreen.kt` - Main UI implementation
- `WordFeedViewModel.kt` - No changes (already implemented)
- `WordFeedUiState.kt` - No changes (already implemented)
