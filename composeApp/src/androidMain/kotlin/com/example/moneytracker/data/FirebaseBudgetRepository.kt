package com.example.moneytracker.data

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseBudgetRepository : BudgetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val budgetsCollection = db.collection("budgets")
    private val savingsGoalsCollection = db.collection("savings_goals")

    private fun QueryDocumentSnapshot.toBudget(): Budget {
        return Budget(
            id = id,
            userId = getString("userId") ?: "",
            category = TransactionCategory.DEFAULT_CATEGORIES.find { it.id == getString("categoryId") }
                ?: TransactionCategory.OTHER,
            amount = getDouble("amount") ?: 0.0,
            month = getLong("month")?.toInt() ?: 1,
            year = getLong("year")?.toInt() ?: 2024,
            spent = getDouble("spent") ?: 0.0
        )
    }

    private fun Budget.toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "categoryId" to category.id,
            "amount" to amount,
            "month" to month,
            "year" to year,
            "spent" to spent
        )
    }

    override fun getBudgetsFlow(userId: String): Flow<List<Budget>> = callbackFlow {
        val subscription = budgetsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val budgets = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val categoryId = doc.getString("categoryId") ?: return@mapNotNull null
                        val category = TransactionCategory.DEFAULT_CATEGORIES.find { it.id == categoryId }
                            ?: return@mapNotNull null
                        
                        Budget(
                            category = category,
                            amount = doc.getDouble("amount") ?: return@mapNotNull null,
                            month = doc.getLong("month")?.toInt() ?: return@mapNotNull null,
                            year = doc.getLong("year")?.toInt() ?: return@mapNotNull null
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(budgets)
            }

        awaitClose { subscription.remove() }
    }

    override fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoal>> = callbackFlow {
        val subscription = savingsGoalsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        SavingsGoal(
                            name = doc.getString("name") ?: return@mapNotNull null,
                            targetAmount = doc.getDouble("targetAmount") ?: return@mapNotNull null,
                            currentAmount = doc.getDouble("currentAmount") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { subscription.remove() }
    }

    override fun addBudget(userId: String, budget: Budget): Flow<Result<Unit>> = callbackFlow {
        try {
            val data = hashMapOf(
                "userId" to userId,
                "categoryId" to budget.category.id,
                "amount" to budget.amount
            )
            
            val addOperation = budgetsCollection.add(data)
            
            addOperation.addOnSuccessListener {
                trySend(Result.success(Unit))
                close()
            }.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
                close(exception)
            }
            
            awaitClose()
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }

    override fun addSavingsGoal(userId: String, goal: SavingsGoal): Flow<Result<Unit>> = callbackFlow {
        try {
            val data = hashMapOf(
                "userId" to userId,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentAmount" to goal.currentAmount
            )
            
            val addOperation = savingsGoalsCollection.add(data)
            
            addOperation.addOnSuccessListener {
                trySend(Result.success(Unit))
                close()
            }.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
                close(exception)
            }
            
            awaitClose()
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }

    override suspend fun updateBudget(userId: String, budget: Budget): Result<Unit> = try {
        val snapshot = budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", budget.category.id)
            .get()
            .await()

        if (snapshot.documents.isEmpty()) {
            Result.failure(IllegalStateException("Budget not found"))
        } else {
            val doc = snapshot.documents.first()
            val data = hashMapOf(
                "userId" to userId,
                "categoryId" to budget.category.id,
                "amount" to budget.amount
            )
            budgetsCollection.document(doc.id).set(data).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateSavingsGoal(userId: String, goal: SavingsGoal): Result<Unit> = try {
        val snapshot = savingsGoalsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", goal.name)
            .get()
            .await()

        if (snapshot.documents.isEmpty()) {
            Result.failure(IllegalStateException("Savings goal not found"))
        } else {
            val doc = snapshot.documents.first()
            val data = hashMapOf(
                "userId" to userId,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentAmount" to goal.currentAmount
            )
            savingsGoalsCollection.document(doc.id).set(data).await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteBudget(userId: String, categoryId: String): Result<Unit> = try {
        val snapshot = budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()

        if (snapshot.documents.isEmpty()) {
            Result.failure(IllegalStateException("Budget not found"))
        } else {
            val doc = snapshot.documents.first()
            budgetsCollection.document(doc.id).delete().await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteSavingsGoal(userId: String, goalName: String): Result<Unit> = try {
        val snapshot = savingsGoalsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", goalName)
            .get()
            .await()

        if (snapshot.documents.isEmpty()) {
            Result.failure(IllegalStateException("Savings goal not found"))
        } else {
            val doc = snapshot.documents.first()
            savingsGoalsCollection.document(doc.id).delete().await()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createBudget(budget: Budget) {
        budgetsCollection.add(budget.toMap()).await()
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetsCollection.document(budget.id).set(budget.toMap()).await()
    }

    override suspend fun deleteBudget(budgetId: String) {
        budgetsCollection.document(budgetId).delete().await()
    }

    override suspend fun getBudget(budgetId: String): Budget? {
        return budgetsCollection.document(budgetId).get().await().let { doc ->
            if (doc.exists()) {
                (doc as QueryDocumentSnapshot).toBudget()
            } else {
                null
            }
        }
    }

    override fun getBudgets(userId: String, month: Int, year: Int): Flow<List<Budget>> = callbackFlow {
        val listener = budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val budgets = snapshot?.documents?.mapNotNull { doc ->
                    (doc as? QueryDocumentSnapshot)?.toBudget()
                } ?: emptyList()

                trySend(budgets)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateSpentAmount(
        userId: String,
        category: TransactionCategory,
        month: Int,
        year: Int,
        amount: Double
    ) {
        val budgetDoc = budgetsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", category.name)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .await()
            .documents
            .firstOrNull()

        budgetDoc?.reference?.update("spent", amount)?.await()
    }
} 