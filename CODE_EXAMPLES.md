# WordFeedScreen Code Examples

## Basic Usage

### 1. In MainActivity (Complete Setup)

```kotlin
package com.example.wordsswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wordsswipe.ui.screen.feed.WordFeedScreen
import com.example.wordsswipe.ui.screen.feed.WordFeedViewModel
import com.example.wordsswipe.ui.theme.WordsSwipeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordsSwipeTheme {
                // ViewModel is automatically created and managed by Hilt
                val wordFeedViewModel: WordFeedViewModel = hiltViewModel()
                
                WordFeedScreen(
                    viewModel = wordFeedViewModel,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

## State Observation Examples

### 2. Observing UI State

```kotlin
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel,
    modifier: Modifier = Modifier
) {
    // Observe different state flows
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()

    // uiState can be:
    // - WordFeedUiState.Loading
    // - WordFeedUiState.Success
    // - WordFeedUiState.Error(message)
    
    when (uiState) {
        WordFeedUiState.Loading -> {
            Text("Loading words...")
        }
        WordFeedUiState.Success -> {
            Text("Pages loaded: ${pages.size}")
            Text("Currently viewing: ${currentIndex + 1}/${pages.size}")
        }
        is WordFeedUiState.Error -> {
            val error = uiState as WordFeedUiState.Error
            Text("Error: ${error.message}")
        }
    }
}
```

### 3. Observing Pages

```kotlin
@Composable
fun DisplayWordList(viewModel: WordFeedViewModel) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    
    Column {
        Text("Total words loaded: ${pages.size}")
        
        pages.forEach { page ->
            Text("Word: ${page.word}")
            if (page.wordDetail != null) {
                Text("Meaning: ${page.wordDetail.meanings.firstOrNull()?.partOfSpeech}")
            }
            if (page.error != null) {
                Text("Error: ${page.error}")
            }
        }
    }
}
```

### 4. Observing Progress

```kotlin
@Composable
fun ShowProgress(viewModel: WordFeedViewModel) {
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    
    val (current, total) = viewModel.getProgress()
    
    LinearProgressIndicator(
        progress = { current.toFloat() / total.toFloat() },
        modifier = Modifier.fillMaxWidth()
    )
    
    Text("$current / $total")
}
```

## Navigation Control Examples

### 5. Programmatic Navigation

```kotlin
@Composable
fun NavigationButtons(viewModel: WordFeedViewModel) {
    Row {
        Button(onClick = { viewModel.swipeUp() }) {
            Text("← Previous")
        }
        
        Button(onClick = { viewModel.swipeDown() }) {
            Text("Next →")
        }
    }
}
```

### 6. Error Recovery

```kotlin
@Composable
fun ErrorRecovery(viewModel: WordFeedViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is WordFeedUiState.Error -> {
            Column {
                Text("Something went wrong!")
                
                Button(onClick = { viewModel.retryLoading() }) {
                    Text("Retry Loading Feed")
                }
            }
        }
        else -> { }
    }
}
```

### 7. Per-Page Error Recovery

```kotlin
@Composable
fun WordCard(
    page: WordPage,
    pageIndex: Int,
    onRetry: (Int) -> Unit
) {
    if (page.error != null) {
        Column {
            Text("Failed to load ${page.word}")
            
            Button(onClick = { onRetry(pageIndex) }) {
                Text("Retry")
            }
        }
    }
}
```

## Event Listening Examples

### 8. Listening to Error Events

```kotlin
@Composable
fun ErrorNotifications(viewModel: WordFeedViewModel) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
```

### 9. Listening to Retry Events

```kotlin
@Composable
fun RetryNotifications(viewModel: WordFeedViewModel) {
    LaunchedEffect(Unit) {
        viewModel.retryEvent.collect {
            // Handle retry event
            // Could show notification, log analytics, etc.
        }
    }
}
```

## Advanced State Management Examples

### 10. Combining Multiple State Flows

```kotlin
@Composable
fun WordDetails(viewModel: WordFeedViewModel) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    
    // Combine to get current page
    val currentPage = pages.getOrNull(currentIndex)
    
    currentPage?.let { page ->
        Column {
            Text("Word: ${page.word}")
            
            when {
                page.isLoading -> {
                    CircularProgressIndicator()
                }
                page.error != null -> {
                    Text("Error: ${page.error}")
                }
                page.wordDetail != null -> {
                    // Display full word detail
                    page.wordDetail.meanings.forEach { meaning ->
                        Text("${meaning.partOfSpeech}:")
                        meaning.definitions.forEach { def ->
                            Text("- ${def.definition}")
                        }
                    }
                }
            }
        }
    }
}
```

### 11. Custom WordDetailContent

```kotlin
@Composable
private fun WordDetailContent(
    word: String,
    wordDetail: WordDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Word
        Text(
            text = word.uppercase(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        // All phonetics (not just first)
        wordDetail.phonetics.forEach { phonetic ->
            if (!phonetic.text.isNullOrBlank()) {
                Text(
                    text = phonetic.text,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // All meanings (not just first)
        wordDetail.meanings.forEach { meaning ->
            Text(
                text = meaning.partOfSpeech,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp)
            )

            meaning.definitions.forEach { definition ->
                Text(
                    text = definition.definition,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
                
                if (!definition.example.isNullOrBlank()) {
                    Text(
                        text = "Example: ${definition.example}",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

## Testing Examples

### 12. Unit Test for State Management

```kotlin
@RunWith(RobolectricTestRunner::class)
class WordFeedViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var viewModel: WordFeedViewModel
    
    @Before
    fun setup() {
        viewModel = WordFeedViewModel(
            mockWordsRepository,
            mockGetWordDetailUseCase
        )
    }
    
    @Test
    fun testInitialState() = runTest {
        // Should start loading
        assertTrue(viewModel.uiState.value is WordFeedUiState.Loading)
    }
    
    @Test
    fun testSwipeDown() = runTest {
        // Setup
        viewModel.pages.value = listOf(
            WordPage("word1"),
            WordPage("word2"),
            WordPage("word3")
        )
        viewModel.currentIndex.value = 0
        
        // Action
        viewModel.swipeDown()
        
        // Assert
        assertEquals(1, viewModel.currentIndex.value)
    }
    
    @Test
    fun testSwipeUpBoundary() = runTest {
        // Setup
        viewModel.currentIndex.value = 0
        
        // Action
        viewModel.swipeUp()
        
        // Assert (should not go below 0)
        assertEquals(0, viewModel.currentIndex.value)
    }
    
    @Test
    fun testPreloadTriggered() = runTest {
        // Setup
        viewModel.pages.value = (0..5).map { WordPage("word$it") }
        viewModel.currentIndex.value = 4 // At threshold
        
        // Action
        viewModel.swipeDown()
        
        // Assert
        assertTrue(viewModel.pages.value.size > 6)
    }
}
```

### 13. Compose UI Test

```kotlin
@RunWith(AndroidJUnit4::class)
class WordFeedScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testLoadingStateDisplayed() {
        composeTestRule.setContent {
            WordFeedScreen(mockViewModel)
        }
        
        composeTestRule
            .onNodeWithText("Loading words...")
            .assertIsDisplayed()
    }
    
    @Test
    fun testErrorStateWithRetry() {
        composeTestRule.setContent {
            WordFeedScreen(mockViewModel)
        }
        
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()
        
        verify(mockViewModel).retryLoading()
    }
    
    @Test
    fun testWordDetailDisplayed() {
        val testDetail = WordDetail(
            word = "phonetic",
            phonetic = "/fə'netɪk/",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "adjective",
                    definitions = listOf(
                        Definition("relating to speech sounds")
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            WordFeedScreen(mockViewModel)
        }
        
        composeTestRule
            .onNodeWithText("PHONETIC")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("/fə'netɪk/")
            .assertIsDisplayed()
    }
}
```

## Performance Optimization Examples

### 14. Memoization for Expensive Computations

```kotlin
@Composable
fun OptimizedWordCardPage(
    page: WordPage,
    pageIndex: Int,
    onRetry: () -> Unit
) {
    // Memoize the detail content to prevent unnecessary recompositions
    val detailContent = remember(page.wordDetail) {
        if (page.wordDetail != null) {
            page.wordDetail.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition
        } else {
            null
        }
    }
    
    // Only recompose if memoized value changes
    if (detailContent != null) {
        Text(detailContent)
    }
}
```

### 15. Lazy Column for Large Lists

```kotlin
@Composable
fun WordListPreview(viewModel: WordFeedViewModel) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(
            count = pages.size,
            key = { index -> pages[index].word }
        ) { index ->
            val page = pages[index]
            WordListItem(
                word = page.word,
                hasDetail = page.wordDetail != null,
                hasError = page.error != null
            )
        }
    }
}
```

## Custom Theme Examples

### 16. Dark Mode Support

```kotlin
@Composable
fun WordDetailContent(
    word: String,
    wordDetail: WordDetail,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val wordColor = if (isDarkMode) {
        Color.White
    } else {
        Color.Black
    }
    
    Column(modifier = modifier) {
        Text(
            text = word.uppercase(),
            color = wordColor,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### 17. Custom Colors

```kotlin
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.Blue
) {
    CompositionLocalProvider(
        LocalContentColor provides accentColor
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // WordFeedScreen content
        }
    }
}
```

## Summary of Examples

This file provides 17 practical code examples covering:

1. ✅ Basic setup in MainActivity
2. ✅ State observation
3. ✅ Navigation control
4. ✅ Error handling
5. ✅ Event listening
6. ✅ Advanced state management
7. ✅ Custom implementations
8. ✅ Unit testing
9. ✅ UI testing
10. ✅ Performance optimization
11. ✅ Theme customization

All examples follow best practices and can be copied directly into your project!
