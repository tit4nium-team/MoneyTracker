package com.example.moneytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.data.ConfigurationRepository
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigurationViewModel(
    private val configurationRepository: ConfigurationRepository
) : ViewModel() {

    val settings: StateFlow<ConfigurationSettings> = configurationRepository.getConfigurationSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConfigurationSettings() // Default initial value
        )

    fun updateTheme(theme: com.example.moneytracker.model.AppTheme) {
        viewModelScope.launch {
            configurationRepository.saveConfigurationSettings(
                settings.value.copy(theme = theme)
            )
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            configurationRepository.saveConfigurationSettings(
                settings.value.copy(notificationsEnabled = enabled)
            )
        }
    }
}
