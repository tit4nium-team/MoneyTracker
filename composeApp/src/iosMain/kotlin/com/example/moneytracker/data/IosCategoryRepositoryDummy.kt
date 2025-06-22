package com.example.moneytracker.data

import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class IosCategoryRepositoryDummy : CategoryRepository {
    private val errorMessage = "Funcionalidade de categorias não implementada no iOS."
    private val unsupportedException = UnsupportedOperationException(errorMessage)

    override fun getCategoriesFlow(userId: String): Flow<List<TransactionCategory>> {
        println("WARN: IosCategoryRepositoryDummy.getCategoriesFlow chamado")
        // Retorna um flow vazio para evitar crash se a UI espera uma lista.
        return emptyFlow()
    }

    override fun addCategory(userId: String, category: TransactionCategory): Flow<Result<Unit>> {
        println("WARN: IosCategoryRepositoryDummy.addCategory chamado")
        return flowOf(Result.failure(unsupportedException))
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> {
        println("WARN: IosCategoryRepositoryDummy.deleteCategory chamado")
        return Result.failure(unsupportedException)
    }
}
