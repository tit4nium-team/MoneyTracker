package com.example.moneytracker.data

import android.content.Context
import com.example.moneytracker.MoneyTrackerApplication

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            return TransactionRepositoryImpl(MoneyTrackerApplication.appContext)
        }

        actual fun provideAuthRepository(): AuthRepository {
            return AuthRepositoryImpl(MoneyTrackerApplication.appContext)
        }

        actual fun provideCategoryRepository(): CategoryRepository {
            return CategoryRepositoryImpl(MoneyTrackerApplication.appContext)
        }

        actual fun provideBudgetRepository(): BudgetRepository {
            return BudgetRepositoryImpl(MoneyTrackerApplication.appContext)
        }

        actual fun provideConfigurationRepository(): ConfigurationRepository {
            return AndroidConfigurationRepository(MoneyTrackerApplication.appContext)
        }
    }
}
