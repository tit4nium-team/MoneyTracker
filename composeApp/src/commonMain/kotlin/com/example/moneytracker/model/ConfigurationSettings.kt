package com.example.moneytracker.model

data class ConfigurationSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationsEnabled: Boolean = true
    // Add more settings as needed
)

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}
