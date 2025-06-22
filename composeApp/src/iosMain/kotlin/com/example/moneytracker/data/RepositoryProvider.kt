package com.example.moneytracker.data

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            println("INFO: Usando FirebaseTransactionRepositoryImpl (KMP) para TransactionRepository no iOS")
            return FirebaseTransactionRepositoryImpl()
        }

        actual fun provideCategoryRepository(): CategoryRepository {
            println("INFO: Usando FirebaseCategoryRepositoryImpl (KMP) para CategoryRepository no iOS")
            return FirebaseCategoryRepositoryImpl()
        }

        actual fun provideAuthRepository(): AuthRepository {
            println("INFO: Usando FirebaseAuthRepositoryImpl (KMP) para AuthRepository no iOS")
            return FirebaseAuthRepositoryImpl()
        }

        actual fun provideBudgetRepository(): BudgetRepository {
            println("INFO: Usando FirebaseBudgetRepositoryImpl (KMP) para BudgetRepository no iOS")
            return FirebaseBudgetRepositoryImpl()
        }
    }
}