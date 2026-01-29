package com.example.wordsswipe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for initializing Hilt dependency injection framework.
 * All dependency injection in this app is managed by Hilt.
 */
@HiltAndroidApp
class WordsSwipeApplication : Application()
