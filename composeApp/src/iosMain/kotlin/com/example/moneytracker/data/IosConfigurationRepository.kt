package com.example.moneytracker.data

import com.example.moneytracker.model.AppTheme
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults
import platform.Foundation.setValue

class IosConfigurationRepository(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : ConfigurationRepository {

    private object UserDefaultsKeys {
        const val APP_THEME = "app_theme"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private val _settingsFlow = MutableStateFlow(loadSettings())
    override fun getConfigurationSettings(): Flow<ConfigurationSettings> = _settingsFlow

    override suspend fun saveConfigurationSettings(settings: ConfigurationSettings) {
        userDefaults.setValue(settings.theme.name, forKey = UserDefaultsKeys.APP_THEME)
        userDefaults.setValue(settings.notificationsEnabled, forKey = UserDefaultsKeys.NOTIFICATIONS_ENABLED)
        _settingsFlow.value = settings // Update the flow after saving
    }

    private fun loadSettings(): ConfigurationSettings {
        val themeName = userDefaults.stringForKey(UserDefaultsKeys.APP_THEME) ?: AppTheme.SYSTEM.name
        val notificationsEnabled = if (userDefaults.objectForKey(UserDefaultsKeys.NOTIFICATIONS_ENABLED) != null) {
            userDefaults.boolForKey(UserDefaultsKeys.NOTIFICATIONS_ENABLED)
        } else {
            true // Default value if key doesn't exist
        }
        return ConfigurationSettings(
            theme = AppTheme.valueOf(themeName),
            notificationsEnabled = notificationsEnabled
        )
    }
}
