package com.example.moneytracker.model

data class Budget(
    val id: String = "",
    val userId: String = "",
    val category: TransactionCategory = TransactionCategory.OTHER,
    val amount: Double = 0.0,
    val month: Int = 1,
    val year: Int = 2024,
    val spent: Double = 0.0
) {
    val remaining: Double
        get() = amount - spent
    
    val progress: Float
        get() = (spent / amount).toFloat().coerceIn(0f, 1f)
}

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
) 