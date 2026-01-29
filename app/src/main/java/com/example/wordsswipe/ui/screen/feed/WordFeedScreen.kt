package com.example.wordsswipe.ui.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordsswipe.domain.model.WordDetail
import com.example.wordsswipe.domain.model.WordPage
import kotlinx.coroutines.launch

/**
 * WordFeedScreen - Main feed screen with TikTok-style vertical swiping.
 *
 * Features:
 * - Fullscreen layout with no internal scrolling
 * - VerticalPager for smooth swipe animations
 * - Swipe UP to go to previous word (only if index > 0)
 * - Swipe DOWN to go to next word
 * - Automatic preload of words as user approaches end of list
 * - Loading, Success, and Error states
 *
 * @param viewModel The WordFeedViewModel that manages state and logic
 * @param modifier Optional modifier for the screen
 */
@Composable
fun WordFeedScreen(
    viewModel: WordFeedViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            WordFeedUiState.Loading -> {
                LoadingState()
            }
            WordFeedUiState.Success -> {
                if (pages.isEmpty()) {
                    ErrorState(
                        message = "No words available",
                        onRetry = { viewModel.retryLoading() }
                    )
                } else {
                    WordFeedPager(
                        pages = pages,
                        currentIndex = currentIndex,
                        onIndexChanged = { newIndex ->
                            // This handles swipe events through pager state changes
                        },
                        onSwipeUp = { viewModel.swipeUp() },
                        onSwipeDown = { viewModel.swipeDown() },
                        onRetryPage = { index -> viewModel.retryPageLoading(index) }
                    )
                }
            }
            is WordFeedUiState.Error -> {
                val error = uiState as WordFeedUiState.Error
                ErrorState(
                    message = error.message,
                    onRetry = { viewModel.retryLoading() }
                )
            }
        }
    }
}

/**
 * WordFeedPager - Handles the vertical paging behavior.
 *
 * Manages:
 * - VerticalPager state persistence across recompositions
 * - Synchronization between pager state and ViewModel currentIndex
 * - Swipe gesture interpretation (up/down)
 * - Automatic page scrolling when pages list grows
 */
@Composable
private fun WordFeedPager(
    pages: List<WordPage>,
    currentIndex: Int,
    onIndexChanged: (Int) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onRetryPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remember pager state - persists across recompositions
    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { pages.size }
    )

    val coroutineScope = rememberCoroutineScope()

    // Synchronize pager state with ViewModel's currentIndex
    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(currentIndex)
            }
        }
    }

    // Create the VerticalPager with custom swipe handling
    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true
    ) { pageIndex ->
        // Render each word page
        if (pageIndex < pages.size) {
            WordCardPage(
                page = pages[pageIndex],
                pageIndex = pageIndex,
                onRetry = { onRetryPage(pageIndex) }
            )
        }
    }

    // Monitor pager state changes and translate to ViewModel actions
    LaunchedEffect(pagerState.currentPage) {
        val newIndex = pagerState.currentPage
        if (newIndex != currentIndex) {
            if (newIndex > currentIndex) {
                // User swiped down (moved to next page)
                onSwipeDown()
            } else {
                // User swiped up (moved to previous page)
                onSwipeUp()
            }
        }
    }
}

/**
 * WordCardPage - Individual word card displayed in the pager.
 *
 * Displays:
 * - Word
 * - Pronunciation/Phonetic
 * - Meaning (part of speech + definitions)
 * - Example sentences
 * - Loading/Error states
 *
 * No scrolling - fullscreen content only.
 */
@Composable
private fun WordCardPage(
    page: WordPage,
    pageIndex: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            page.isLoading -> {
                // Loading state for this page
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading: ${page.word}",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            page.error != null -> {
                // Error state for this page
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = page.word.uppercase(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "Error: ${page.error}",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                    androidx.compose.material3.Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
            page.wordDetail != null -> {
                // Success state - display word details
                WordDetailContent(
                    word = page.word,
                    wordDetail = page.wordDetail,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }
            else -> {
                // Fallback state - should not occur in normal flow
                Text(
                    text = page.word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * WordDetailContent - Displays complete word information.
 *
 * Layout (vertical, fullscreen):
 * 1. Word (large, bold)
 * 2. Phonetic (pronunciation)
 * 3. Meaning (part of speech + definitions)
 * 4. Example sentences
 *
 * All content fits on one page without scrolling.
 */
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
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        // Phonetic
        if (!wordDetail.phonetic.isNullOrBlank()) {
            Text(
                text = wordDetail.phonetic,
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // First meaning and definition
        if (wordDetail.meanings.isNotEmpty()) {
            val firstMeaning = wordDetail.meanings.first()

            // Part of speech (e.g., "noun", "verb")
            Text(
                text = firstMeaning.partOfSpeech,
                fontSize = 16.sp,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 32.dp)
            )

            // First definition
            if (firstMeaning.definitions.isNotEmpty()) {
                val firstDef = firstMeaning.definitions.first()
                Text(
                    text = firstDef.definition,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Example sentence if available
                if (!firstDef.example.isNullOrBlank()) {
                    Text(
                        text = "\"${firstDef.example}\"",
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * LoadingState - Full-screen loading indicator.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading words...",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * ErrorState - Full-screen error display with retry option.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Oops!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = message,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Retry")
            }
        }
    }
}
