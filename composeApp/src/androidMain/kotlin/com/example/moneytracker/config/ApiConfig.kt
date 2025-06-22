package com.example.moneytracker.config

import com.example.moneytracker.BuildConfig

internal actual object ApiConfig {
    // This will pull the API key from your build.gradle file (via BuildConfig) for Android
    actual val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY
}
