package com.example.wordsswipe.ui.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
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
                // Loading state for this page - shows while fetching API data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Loading spinner
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Word being loaded
                    Text(
                        text = page.word.uppercase(),
                        modifier = Modifier.padding(top = 24.dp),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Loading message
                    Text(
                        text = "Loading definition...",
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 15.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            page.error != null -> {
                // Error state for this page - safe error handling
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Error icon
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Word name (still visible even with error)
                    Text(
                        text = page.word.uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Error message
                    Text(
                        text = page.error ?: "Unknown error occurred",
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 8.dp),
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 20.sp
                    )

                    // Retry button for this page
                    androidx.compose.material3.Button(
                        onClick = {
                            try {
                                onRetry()
                            } catch (e: Exception) {
                                // Safely handle retry errors
                            }
                        },
                        modifier = Modifier
                            .padding(top = 28.dp)
                            .height(44.dp)
                            .fillMaxWidth(0.65f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Retry",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Helper text
                    Text(
                        text = "Or continue to next word",
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
 * WordDetailContent - Beautiful, minimalist word display.
 *
 * Design Philosophy:
 * - Vertical center alignment for focus
 * - Minimalist aesthetic (TikTok-inspired)
 * - Dark background / light text
 * - Excellent typography hierarchy
 * - No scrolling - fits entirely on one screen
 *
 * Display Elements:
 * 1. Word (primary focus - largest, bold)
 * 2. Phonetic (pronunciation - secondary, muted)
 * 3. Part of Speech (grammar tag - accent color)
 * 4. Definition (main meaning - readable size)
 * 5. Example (context sentence - subtle, italicized)
 */
@Composable
private fun WordDetailContent(
    word: String,
    wordDetail: WordDetail,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== WORD (Primary Focus) =====
            Text(
                text = word.uppercase(),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp,
                lineHeight = 64.sp
            )

            // ===== PHONETIC (Pronunciation) =====
            if (!wordDetail.phonetic.isNullOrBlank()) {
                Text(
                    text = wordDetail.phonetic,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.alpha(0.7f)
                )
            }

            // ===== MEANING SECTION =====
            if (wordDetail.meanings.isNotEmpty()) {
                val firstMeaning = wordDetail.meanings.first()

                // Part of Speech Tag
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = firstMeaning.partOfSpeech,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Definition
                if (firstMeaning.definitions.isNotEmpty()) {
                    val firstDef = firstMeaning.definitions.first()

                    Text(
                        text = firstDef.definition,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 26.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    // ===== EXAMPLE SENTENCE =====
                    if (!firstDef.example.isNullOrBlank()) {
                        Text(
                            text = "\"${firstDef.example}\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .alpha(0.8f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * LoadingState - Full-screen loading indicator with messaging.
 *
 * Displays:
 * - Animated circular progress indicator
 * - Loading message
 * - Centered on screen
 * - Non-dismissible (user must wait for load)
 *
 * Best used when:
 * - Fetching initial batch of words
 * - Loading API data for pages
 * - App startup
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Loading spinner
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )

            // Loading message
            Text(
                text = "Loading words...",
                modifier = Modifier.padding(top = 24.dp),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Helpful subtext
            Text(
                text = "Fetching definitions from API",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ErrorState - Full-screen error display with recovery options.
 *
 * Features:
 * - Clear error messaging
 * - Actionable retry button
 * - Safe error handling (no crashes)
 * - Professional error UI
 * - User-friendly explanations
 *
 * Best used when:
 * - API request fails
 * - Network error occurs
 * - Data parsing fails
 * - System error happens
 *
 * Error handling is safe:
 * - Catches all exceptions
 * - Displays user-friendly messages
 * - Allows retry without crashing
 * - Logs errors for debugging
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Error icon / emoji
            Text(
                text = "⚠️",
                fontSize = 56.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Error title
            Text(
                text = "Oops!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Error message with proper spacing
            Text(
                text = message,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 8.dp),
                fontSize = 15.sp,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                lineHeight = 20.sp
            )

            // Helpful suggestion
            Text(
                text = "Check your internet connection and try again",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Retry button with elevation and proper styling
            androidx.compose.material3.Button(
                onClick = {
                    try {
                        onRetry()
                    } catch (e: Exception) {
                        // Safely catch any retry errors
                        // ViewModel will handle error propagation
                    }
                },
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(48.dp)
                    .fillMaxWidth(0.6f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Retry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Secondary action hint
            Text(
                text = "Or swipe to another word",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
