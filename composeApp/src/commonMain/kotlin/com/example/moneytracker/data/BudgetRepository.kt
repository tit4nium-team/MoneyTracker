package com.example.moneytracker.data

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsFlow(userId: String): Flow<List<Budget>>
    fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoal>>
    fun addBudget(userId: String, budget: Budget): Flow<Result<Unit>>
    fun addSavingsGoal(userId: String, goal: SavingsGoal): Flow<Result<Unit>>
    suspend fun updateBudget(userId: String, budget: Budget): Result<Unit>
    suspend fun updateSavingsGoal(userId: String, goal: SavingsGoal): Result<Unit>
    suspend fun deleteBudget(userId: String, categoryId: String): Result<Unit>
    suspend fun deleteSavingsGoal(userId: String, goalName: String): Result<Unit>
} 