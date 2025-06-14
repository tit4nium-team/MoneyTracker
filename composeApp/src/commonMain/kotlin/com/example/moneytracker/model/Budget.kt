package com.example.moneytracker.model


data class Budget(
    val category: TransactionCategory,
    val amount: Double
)

data class SavingsGoal(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
) 