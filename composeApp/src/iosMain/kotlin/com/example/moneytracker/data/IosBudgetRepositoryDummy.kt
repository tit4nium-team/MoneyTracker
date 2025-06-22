package com.example.moneytracker.data

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class IosBudgetRepositoryDummy : BudgetRepository {
    private val errorMessage = "Funcionalidade de orçamento não implementada no iOS."
    private val unsupportedException = UnsupportedOperationException(errorMessage)

    override fun getBudgetsFlow(userId: String): Flow<List<Budget>> {
        println("WARN: IosBudgetRepositoryDummy.getBudgetsFlow chamado")
        return emptyFlow()
    }

    override fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoal>> {
        println("WARN: IosBudgetRepositoryDummy.getSavingsGoalsFlow chamado")
        return emptyFlow()
    }

    override fun addBudget(userId: String, budget: Budget): Flow<Result<Unit>> {
        println("WARN: IosBudgetRepositoryDummy.addBudget chamado")
        return flowOf(Result.failure(unsupportedException))
    }

    override fun addSavingsGoal(userId: String, goal: SavingsGoal): Flow<Result<Unit>> {
        println("WARN: IosBudgetRepositoryDummy.addSavingsGoal chamado")
        return flowOf(Result.failure(unsupportedException))
    }

    override suspend fun updateBudget(userId: String, budget: Budget): Result<Unit> {
        println("WARN: IosBudgetRepositoryDummy.updateBudget (com userId) chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun updateSavingsGoal(userId: String, goal: SavingsGoal): Result<Unit> {
        println("WARN: IosBudgetRepositoryDummy.updateSavingsGoal chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun deleteBudget(userId: String, categoryId: String): Result<Unit> {
        println("WARN: IosBudgetRepositoryDummy.deleteBudget (com userId, categoryId) chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun deleteSavingsGoal(userId: String, goalName: String): Result<Unit> {
        println("WARN: IosBudgetRepositoryDummy.deleteSavingsGoal chamado")
        return Result.failure(unsupportedException)
    }

    override suspend fun createBudget(budget: Budget) {
        println("WARN: IosBudgetRepositoryDummy.createBudget chamado")
        throw unsupportedException
    }

    override suspend fun updateBudget(budget: Budget) {
        println("WARN: IosBudgetRepositoryDummy.updateBudget chamado")
        throw unsupportedException
    }

    override suspend fun deleteBudget(budgetId: String) {
        println("WARN: IosBudgetRepositoryDummy.deleteBudget (com budgetId) chamado")
        throw unsupportedException
    }

    override suspend fun getBudget(budgetId: String): Budget? {
        println("WARN: IosBudgetRepositoryDummy.getBudget chamado")
        // Lançar exceção ou retornar null. Retornar null pode ser mais seguro para evitar crash
        // se o chamador não estiver em um try-catch e puder lidar com null.
        // No entanto, para consistência com outras funções suspend, lançar é mais explícito.
        throw unsupportedException
    }

    override fun getBudgets(userId: String, month: Int, year: Int): Flow<List<Budget>> {
        println("WARN: IosBudgetRepositoryDummy.getBudgets (com month, year) chamado")
        return emptyFlow()
    }

    override suspend fun updateSpentAmount(userId: String, category: TransactionCategory, month: Int, year: Int, amount: Double) {
        println("WARN: IosBudgetRepositoryDummy.updateSpentAmount chamado")
        throw unsupportedException
    }
}
