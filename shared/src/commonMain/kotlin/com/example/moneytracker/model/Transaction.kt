package com.example.moneytracker.model

import kotlinx.datetime.LocalDateTime

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionCategory {
    FOOD,
    BILLS,
    ENTERTAINMENT,
    TRANSPORT,
    SHOPPING,
    SALARY,
    OTHER
}

data class Transaction(
    val id: String = "",
    val type: TransactionType,
    val amount: Double,
    val category: TransactionCategory,
    val description: String = "",
    val date: String,
    val userId: String = ""
) 