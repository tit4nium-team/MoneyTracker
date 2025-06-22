package com.example.moneytracker.data

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            println("INFO: Usando IosTransactionRepositoryDummy para TransactionRepository no iOS")
            return IosTransactionRepositoryDummy()
        }

        actual fun provideCategoryRepository(): CategoryRepository {
            println("INFO: Usando IosCategoryRepositoryDummy para CategoryRepository no iOS")
            return IosCategoryRepositoryDummy()
        }

        actual fun provideAuthRepository(): AuthRepository {
            println("INFO: Usando IosAuthRepositoryDummy para AuthRepository no iOS (inalterado nesta etapa)")
            return IosAuthRepositoryDummy() // Mantém a dummy anterior
        }

        actual fun provideBudgetRepository(): BudgetRepository {
            println("INFO: Usando IosBudgetRepositoryDummy para BudgetRepository no iOS")
            return IosBudgetRepositoryDummy()
        }
    }
}