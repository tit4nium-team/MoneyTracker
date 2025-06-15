package com.example.moneytracker.data

import platform.Foundation.NSUserDefaults // Correct placement of import

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            // Assuming a default or contextless implementation for iOS if applicable
            // Or, if context is truly needed, this approach might need rethinking
            // For now, let's assume a simplified constructor or a way to get context
            return TransactionRepositoryImpl() // Placeholder, might need adjustment
        }

        actual fun provideAuthRepository(): AuthRepository {
            return AuthRepositoryImpl() // Placeholder
        }

        actual fun provideCategoryRepository(): CategoryRepository {
            return CategoryRepositoryImpl() // Placeholder
        }

        actual fun provideBudgetRepository(): BudgetRepository {
            return BudgetRepositoryImpl() // Placeholder
        }

        actual fun provideConfigurationRepository(): ConfigurationRepository {
            return IosConfigurationRepository(NSUserDefaults.standardUserDefaults) // Corrected instantiation
        }
    }
}
