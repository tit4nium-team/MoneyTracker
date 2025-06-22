package com.example.moneytracker.data

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class IosTransactionRepositoryDummy : TransactionRepository {
    private val errorMessage = "Funcionalidade de transações não implementada no iOS."
    private val unsupportedException = UnsupportedOperationException(errorMessage)

    override fun getTransactionsFlow(userId: String): Flow<List<Transaction>> {
        println("WARN: IosTransactionRepositoryDummy.getTransactionsFlow chamado")
        // Retorna um flow vazio, ou poderia emitir um erro se a UI puder lidar com isso.
        // Para evitar crash imediato se a UI espera uma lista, um flow vazio é mais seguro.
        return emptyFlow()
    }

    override fun addTransaction(transaction: Transaction): Flow<Result<Unit>> {
        println("WARN: IosTransactionRepositoryDummy.addTransaction chamado")
        return flowOf(Result.failure(unsupportedException))
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        println("WARN: IosTransactionRepositoryDummy.updateTransaction chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        println("WARN: IosTransactionRepositoryDummy.deleteTransaction chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun getTransactionsByCategory(userId: String, category: TransactionCategory): Result<List<Transaction>> {
        println("WARN: IosTransactionRepositoryDummy.getTransactionsByCategory chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun getTransactionsByDateRange(userId: String, startDate: String, endDate: String): Result<List<Transaction>> {
        println("WARN: IosTransactionRepositoryDummy.getTransactionsByDateRange chamado")
        return Result.failure(unsupportedException)
    }
}
