package com.example.moneytracker.config

internal expect object ApiConfig {
    val GEMINI_API_KEY: String // Para Android (via BuildConfig)
    val GEMINI_API_KEY_IOS: String // Para iOS (placeholder ou configurado nativamente)
}
