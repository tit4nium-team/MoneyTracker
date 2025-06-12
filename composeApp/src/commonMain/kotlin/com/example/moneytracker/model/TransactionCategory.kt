package com.example.moneytracker.model

data class TransactionCategory(
    val id: String,
    val name: String,
    val icon: String = "shopping_cart",
    val isCustom: Boolean = false
) {
    companion object {
        val FOOD = TransactionCategory("food", "Alimentação")
        val BILLS = TransactionCategory("bills", "Contas")
        val ENTERTAINMENT = TransactionCategory("entertainment", "Entretenimento")
        val TRANSPORT = TransactionCategory("transport", "Transporte")
        val SHOPPING = TransactionCategory("shopping", "Compras")
        val SALARY = TransactionCategory("salary", "Salário")
        val OTHER = TransactionCategory("other", "Outros")

        val DEFAULT_CATEGORIES = listOf(FOOD, BILLS, ENTERTAINMENT, TRANSPORT, SHOPPING, SALARY, OTHER)
    }

    override fun toString(): String = name
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionCategory) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
} 