package com.example.moneytracker.config

import com.example.moneytracker.BuildConfig

internal actual fun getGeminiApiKey(): String {
    return BuildConfig.GEMINI_API_KEY
}
