package com.example.moneytracker.data

expect class RepositoryProvider {
    companion object {
        fun provideTransactionRepository(): TransactionRepository
    }
} 