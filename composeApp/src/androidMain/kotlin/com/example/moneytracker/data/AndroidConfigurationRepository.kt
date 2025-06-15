package com.example.moneytracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.moneytracker.model.AppTheme
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AndroidConfigurationRepository(private val context: Context) : ConfigurationRepository {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    override fun getConfigurationSettings(): Flow<ConfigurationSettings> {
        return context.dataStore.data.map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            ConfigurationSettings(
                theme = AppTheme.valueOf(themeName),
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    override suspend fun saveConfigurationSettings(settings: ConfigurationSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = settings.theme.name
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
        }
    }
}
