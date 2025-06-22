package com.example.moneytracker.data

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsFlow(userId: String): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Result<Unit> // Alterado de Flow<Result<Unit>> para suspend
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
    suspend fun getTransactionsByCategory(userId: String, category: TransactionCategory): Result<List<Transaction>>
    suspend fun getTransactionsByDateRange(userId: String, startDate: String, endDate: String): Result<List<Transaction>>
} 