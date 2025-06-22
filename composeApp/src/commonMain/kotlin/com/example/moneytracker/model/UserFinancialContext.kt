package com.example.moneytracker.model // Movido para o pacote model

import kotlinx.datetime.LocalDate

// Definição de TransactionCategory e Budget precisam estar acessíveis aqui
// Supondo que já estão no pacote com.example.moneytracker.model

data class UserFinancialContext(
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val totalIncome: Double,
    val totalExpenses: Double,
    val monthlyBudget: Double,
    val topExpenseCategories: List<Pair<TransactionCategory, Double>>,
    val currentDate: LocalDate
)
