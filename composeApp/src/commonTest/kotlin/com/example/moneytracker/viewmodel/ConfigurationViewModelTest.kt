package com.example.moneytracker.viewmodel

import com.example.moneytracker.data.ConfigurationRepository
import com.example.moneytracker.model.AppTheme
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Mock Repository for testing
class MockConfigurationRepository : ConfigurationRepository {
    private val currentSettings = MutableStateFlow(ConfigurationSettings())

    override fun getConfigurationSettings() = currentSettings
    override suspend fun saveConfigurationSettings(settings: ConfigurationSettings) {
        currentSettings.value = settings
    }

    // Helper for tests to directly manipulate settings if needed
    fun emit(settings: ConfigurationSettings) {
        currentSettings.value = settings
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigurationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockConfigurationRepository
    private lateinit var viewModel: ConfigurationViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockConfigurationRepository()
        viewModel = ConfigurationViewModel(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial settings are loaded correctly`() = runTest {
        val initialSettings = ConfigurationSettings(theme = AppTheme.DARK, notificationsEnabled = false)
        mockRepository.emit(initialSettings) // Simulate repository emitting initial settings

        val collectedSettings = viewModel.settings.first() // Collect the first emitted value

        assertEquals(initialSettings.theme, collectedSettings.theme)
        assertEquals(initialSettings.notificationsEnabled, collectedSettings.notificationsEnabled)
    }

    @Test
    fun `updateTheme saves new theme and updates state`() = runTest {
        val newTheme = AppTheme.LIGHT
        viewModel.updateTheme(newTheme)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

        val updatedSettings = viewModel.settings.first()
        assertEquals(newTheme, updatedSettings.theme)

        // Verify repository was called
        val repoSettings = mockRepository.getConfigurationSettings().first()
        assertEquals(newTheme, repoSettings.theme)
    }

    @Test
    fun `setNotificationsEnabled saves new preference and updates state`() = runTest {
        val newPreference = false
        viewModel.setNotificationsEnabled(newPreference)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutines complete

        val updatedSettings = viewModel.settings.first()
        assertEquals(newPreference, updatedSettings.notificationsEnabled)

        // Verify repository was called
        val repoSettings = mockRepository.getConfigurationSettings().first()
        assertEquals(newPreference, repoSettings.notificationsEnabled)
    }

    @Test
    fun `settings flow reflects repository changes`() = runTest {
        // Initial state
        var currentSettings = viewModel.settings.value
        assertEquals(AppTheme.SYSTEM, currentSettings.theme) // Default
        assertTrue(currentSettings.notificationsEnabled) // Default

        // Simulate repository update from another source
        val newRepoSettings = ConfigurationSettings(theme = AppTheme.DARK, notificationsEnabled = false)
        mockRepository.emit(newRepoSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        currentSettings = viewModel.settings.value // Re-check after repo update
        assertEquals(AppTheme.DARK, currentSettings.theme)
        assertEquals(false, currentSettings.notificationsEnabled)
    }
}
