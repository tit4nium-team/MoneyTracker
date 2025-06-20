package com.example.moneytracker.model

data class Transaction(
    val id: String = "",
    val type: TransactionType,
    val amount: Double,
    val category: TransactionCategory,
    val description: String,
    val date: String,
    val userId: String
)

enum class TransactionType {
    INCOME, EXPENSE
}