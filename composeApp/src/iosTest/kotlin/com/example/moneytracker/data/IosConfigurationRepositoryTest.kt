package com.example.moneytracker.data

import com.example.moneytracker.model.AppTheme
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSMutableDictionary
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


// Mock NSUserDefaults by replacing its behavior for the test duration
class MockNSUserDefaults : NSUserDefaults(suiteName = "testSuite") {
    val storage = mutableMapOf<String, Any?>()

    override fun stringForKey(defaultName: String): String? {
        return storage[defaultName] as? String
    }

    override fun boolForKey(defaultName: String): Boolean {
        return storage[defaultName] as? Boolean ?: false
    }

    override fun objectForKey(defaultName: String): Any? {
        return storage[defaultName]
    }

    override fun setObject(value: Any?, forKey: String) {
        storage[forKey] = value
    }

    // Required override for init, though not directly used by the mock's logic here
    override fun init(): NSUserDefaults = this
    override fun initWithSuiteName(suiteName: String?): NSUserDefaults = this


    fun clear() {
        storage.clear()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class IosConfigurationRepositoryTest {

    private lateinit var repository: IosConfigurationRepository
    private lateinit var mockUserDefaults: MockNSUserDefaults

    // Keys used by IosConfigurationRepository
    private val APP_THEME_KEY = "app_theme"
    private val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"

    @BeforeTest
    fun setup() {
        mockUserDefaults = MockNSUserDefaults()
        // Instantiate repository with the mock NSUserDefaults
        repository = IosConfigurationRepository(userDefaults = mockUserDefaults)
    }


    @Test
    fun `getConfigurationSettings returns default values when no settings saved`() = runTest {
        mockUserDefaults.clear() // Ensure a clean state for this test
        // Repository is already using mockUserDefaults due to setup
        val settings = repository.getConfigurationSettings().first()

        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertEquals(true, settings.notificationsEnabled)
    }

    @Test
    fun `saveConfigurationSettings and getConfigurationSettings work correctly`() = runTest {
        val newSettings = ConfigurationSettings(theme = AppTheme.DARK, notificationsEnabled = false)

        // Repository uses mockUserDefaults
        repository.saveConfigurationSettings(newSettings)
        val savedSettings = repository.getConfigurationSettings().first()

        assertEquals(AppTheme.DARK, savedSettings.theme)
        assertEquals(false, savedSettings.notificationsEnabled)

        // Verify that data was actually saved to the mock
        assertEquals(AppTheme.DARK.name, mockUserDefaults.stringForKey(APP_THEME_KEY))
        assertEquals(false, mockUserDefaults.boolForKey(NOTIFICATIONS_ENABLED_KEY))
    }

    @Test
    fun `getConfigurationSettings reflects direct NSUserDefaults changes via mock`() = runTest {
        // Directly manipulate the mockUserDefaults instance
        mockUserDefaults.setObject(AppTheme.LIGHT.name, forKey = APP_THEME_KEY)
        mockUserDefaults.setObject(false, forKey = NOTIFICATIONS_ENABLED_KEY)

        // Settings flow should pick up changes if it reloads or observes the underlying source.
        // The current IosConfigurationRepository implementation loads settings once into _settingsFlow
        // and only updates _settingsFlow on save. So, external changes to userDefaults
        // won't be reflected unless loadSettings() is called again (e.g., by creating a new repo instance or if the flow has logic to re-query).
        // To test this behavior accurately, we might need to create a new repo instance.
        val newRepoInstance = IosConfigurationRepository(userDefaults = mockUserDefaults)
        val settings = newRepoInstance.getConfigurationSettings().first()

        assertEquals(AppTheme.LIGHT, settings.theme)
        assertEquals(false, settings.notificationsEnabled)
    }

    @Test
    fun `notificationsEnabled defaults to true if key is missing`() = runTest {
        mockUserDefaults.clear()
        mockUserDefaults.setObject(AppTheme.DARK.name, forKey = APP_THEME_KEY) // Theme exists
        // NOTIFICATIONS_ENABLED_KEY is missing

        val settings = repository.getConfigurationSettings().first()
        assertEquals(AppTheme.DARK, settings.theme)
        assertTrue(settings.notificationsEnabled, "Notifications should default to true if key is absent")
    }

    @Test
    fun `notificationsEnabled is false if explicitly set to false`() = runTest {
        mockUserDefaults.clear()
        mockUserDefaults.setObject(AppTheme.DARK.name, forKey = APP_THEME_KEY)
        mockUserDefaults.setObject(false, forKey = NOTIFICATIONS_ENABLED_KEY)

        val settings = repository.getConfigurationSettings().first()
        assertEquals(AppTheme.DARK, settings.theme)
        assertEquals(false, settings.notificationsEnabled)
    }
}
