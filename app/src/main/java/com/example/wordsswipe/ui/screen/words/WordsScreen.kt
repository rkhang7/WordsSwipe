package com.example.wordsswipe.ui.screen.words

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordsswipe.domain.model.Word

/**
 * Main screen for displaying words with vertical swipe navigation.
 * This composable observes the ViewModel's state and reacts to changes.
 * Pure composable - no business logic, only UI rendering.
 */
@Composable
fun WordsScreen(
    viewModel: WordsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is WordsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WordsUiState.Success -> {
                val words = (uiState as WordsUiState.Success).words
                WordsPager(words = words)
            }
            is WordsUiState.Error -> {
                val errorMessage = (uiState as WordsUiState.Error).message
                ErrorMessage(message = errorMessage)
            }
        }
    }
}

/**
 * VerticalPager for displaying words with swipe navigation.
 * Each page shows one complete word card without internal scrolling.
 */
@Composable
fun WordsPager(
    words: List<Word>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { words.size })

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { pageIndex ->
        WordCard(
            word = words[pageIndex],
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

/**
 * Displays a single word with its definition, part of speech, example, and difficulty level.
 * Fullscreen card with no internal scrolling - content fits within the page.
 */
@Composable
fun WordCard(
    word: Word,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Difficulty indicator
        DifficultyBadge(
            level = word.difficultyLevel,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Main word text
        Text(
            text = word.text,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Part of speech
        Text(
            text = word.partOfSpeech,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Divider line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .padding(vertical = 16.dp)
                .background(MaterialTheme.colorScheme.outline)
                .padding(vertical = 0.5.dp)
        )

        // Definition
        Text(
            text = "Definition",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        Text(
            text = word.definition,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Example
        Text(
            text = "Example",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "\"${word.example}\"",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

/**
 * Displays difficulty level as a visual badge (1-5 stars).
 */
@Composable
fun DifficultyBadge(
    level: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    level <= 2 -> MaterialTheme.colorScheme.primaryContainer
                    level <= 3 -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Difficulty: ${"★".repeat(level)}${"☆".repeat(5 - level)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Error message display when loading fails.
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oops! Something went wrong",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
