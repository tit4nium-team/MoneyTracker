package com.example.moneytracker.data

import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    fun getConfigurationSettings(): Flow<ConfigurationSettings>
    suspend fun saveConfigurationSettings(settings: ConfigurationSettings)
}
