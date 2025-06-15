package com.example.moneytracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.moneytracker.model.AppTheme
import com.example.moneytracker.model.ConfigurationSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class) // Or use MockitoJUnitRunner if not using Robolectric context
class AndroidConfigurationRepositoryTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var repository: AndroidConfigurationRepository
    private lateinit var dataStore: DataStore<Preferences>
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + kotlinx.coroutines.Job()) // CoroutineScope for DataStore

    // Use Robolectric to get a real Context if needed, or mock it.
    // For DataStore, it's often easier to use a real (test) DataStore instance.
    private lateinit var context: Context

    // Preference keys (mirroring the ones in AndroidConfigurationRepository)
    private val APP_THEME_KEY = androidx.datastore.preferences.core.stringPreferencesKey("app_theme")
    private val NOTIFICATIONS_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("notifications_enabled")


    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() // Robolectric application context
        // Create a test DataStore
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { context.preferencesDataStoreFile("test_settings") }
        )
        repository = AndroidConfigurationRepository(context)

        // Override the DataStore in the repository with the test one.
        // This requires making the dataStore property in AndroidConfigurationRepository accessible for testing
        // (e.g., internal or using a helper). For this example, we'll assume direct re-initialization
        // or that it uses a context that can be controlled to use this test datastore.
        // A cleaner way is to inject DataStore into AndroidConfigurationRepository.
        // For now, let's clear the actual datastore used by the repo if possible,
        // or ensure tests don't interfere. The below re-initialization with test datastore is preferred.

        // To ensure the repository uses THIS datastore, we'd ideally inject it.
        // If not, we rely on `context.preferencesDataStoreFile("settings")` being the one we want to test
        // or clear it before each test.
        // For this test, we'll create a repository that *should* use "settings" file,
        // and we'll also directly manipulate "test_settings" to verify logic,
        // then test the repo instance. This is a bit indirect.
        // A better approach is to inject DataStore<Preferences> into AndroidConfigurationRepository.
        // Let's assume for now the test will operate on the default "settings" DataStore.
        // To make it robust, we'll clear it.
         runTest(testDispatcher) {
            context.dataStoreFile("settings").delete() // Clear actual settings file for test isolation
         }
         repository = AndroidConfigurationRepository(context) // Re-init to use the fresh file
    }

    @After
    fun tearDown() {
        // Clean up DataStore file
        context.dataStoreFile("test_settings").delete()
        context.dataStoreFile("settings").delete() // Clean up the one used by repo
        testScope.coroutineContext.job.cancel() // Cancel the scope
    }

    @Test
    fun `getConfigurationSettings returns default values when no settings saved`() = testScope.runTest {
        val settings = repository.getConfigurationSettings().first()
        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertEquals(true, settings.notificationsEnabled)
    }

    @Test
    fun `saveConfigurationSettings and getConfigurationSettings work correctly`() = testScope.runTest {
        val newSettings = ConfigurationSettings(theme = AppTheme.DARK, notificationsEnabled = false)
        repository.saveConfigurationSettings(newSettings)

        val savedSettings = repository.getConfigurationSettings().first()
        assertEquals(AppTheme.DARK, savedSettings.theme)
        assertEquals(false, savedSettings.notificationsEnabled)
    }

    @Test
    fun `getConfigurationSettings reflects direct DataStore changes`() = testScope.runTest {
        // Directly edit the DataStore that the repository should be using
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = AppTheme.LIGHT.name
            preferences[NOTIFICATIONS_ENABLED_KEY] = false
        }

        val settings = repository.getConfigurationSettings().first()
        assertEquals(AppTheme.LIGHT, settings.theme)
        assertEquals(false, settings.notificationsEnabled)
    }
     private val Context.dataStore: DataStore<Preferences>
        get() = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { this.preferencesDataStoreFile("settings") } // Ensure this matches repo name
        )
}
