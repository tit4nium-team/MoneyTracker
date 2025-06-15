package com.example.moneytracker.data

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            return FirebaseRepository()
        }

        actual fun provideAuthRepository(): AuthRepository {
            return FirebaseAuthRepository()
        }

    actual fun provideCategoryRepository(): CategoryRepository {
        return FirebaseCategoryRepository()
    }

        actual fun provideBudgetRepository(): BudgetRepository {
            return FirebaseBudgetRepository()
        }
    }
} 