package com.example.moneytracker.data

import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesFlow(userId: String): Flow<List<TransactionCategory>>
    suspend fun addCategory(userId: String, category: TransactionCategory): Result<Unit> // Alterado de Flow<Result<Unit>> para suspend
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit>
}