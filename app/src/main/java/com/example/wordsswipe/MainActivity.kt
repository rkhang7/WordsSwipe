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

/**
 * Main Activity for the Words Swipe application.
 * Uses Hilt for dependency injection and displays the WordFeedScreen composable.
 * Properly integrated with edge-to-edge display support.
 *
 * Features:
 * - TikTok-style vertical swipe UI
 * - Full-screen word cards
 * - Seamless preloading of words
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordsSwipeTheme {
                // Inject the ViewModel using Hilt
                val wordFeedViewModel: WordFeedViewModel = hiltViewModel()

                WordFeedScreen(
                    viewModel = wordFeedViewModel,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                )
            }
        }
    }
}