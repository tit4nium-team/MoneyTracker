package com.example.moneytracker.data

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            return FirebaseTransactionRepositoryImpl()
        }

        actual fun provideAuthRepository(): AuthRepository {
            return FirebaseAuthRepositoryImpl()
        }

    actual fun provideCategoryRepository(): CategoryRepository {
        return FirebaseCategoryRepositoryImpl()
    }

        actual fun provideBudgetRepository(): BudgetRepository {
            return FirebaseBudgetRepositoryImpl()
        }
    }
}