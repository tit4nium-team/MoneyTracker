package com.example.moneytracker.data

actual class RepositoryProvider {
    actual companion object {
        actual fun provideTransactionRepository(): TransactionRepository {
            TODO("Transações: Not yet implemented for iOS. This will cause a crash if called.")
        }

        actual fun provideCategoryRepository(): CategoryRepository {
            TODO("Categorias: Not yet implemented for iOS. This will cause a crash if called.")
        }

        actual fun provideAuthRepository(): AuthRepository {
            println("INFO: Usando IosAuthRepositoryDummy para AuthRepository no iOS")
            return IosAuthRepositoryDummy()
        }

        actual fun provideBudgetRepository(): BudgetRepository {
            TODO("Orçamento: Not yet implemented for iOS. This will cause a crash if called.")
        }
    }
}