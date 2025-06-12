package com.example.moneytracker.data

import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategoriesFlow(userId: String): Flow<List<TransactionCategory>>
    fun addCategory(userId: String, category: TransactionCategory): Flow<Result<Unit>>
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit>
} 