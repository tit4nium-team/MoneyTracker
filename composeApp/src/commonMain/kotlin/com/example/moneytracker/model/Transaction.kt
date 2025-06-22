package com.example.moneytracker.model

import kotlinx.serialization.Serializable

@Serializable // Adicionado
data class Transaction(
    val id: String = "",
    val type: TransactionType, // Precisa ser @Serializable também ou o enum é serializável por padrão
    val amount: Double,
    val category: TransactionCategory,
    val description: String,
    val date: String,
    val userId: String
)

@Serializable // Adicionado
enum class TransactionType {
    INCOME, EXPENSE
}