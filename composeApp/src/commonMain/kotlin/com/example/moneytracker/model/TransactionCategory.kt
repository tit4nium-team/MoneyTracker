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
        val HEALTH = TransactionCategory("health", "Saúde")
        val EDUCATION = TransactionCategory("education", "Educação")
        val INVESTMENT = TransactionCategory("investiment", "Investimentos")
        val HOUSING = TransactionCategory("housing", "Casa")
        val CLOTHING = TransactionCategory("clothing", "Vestuário")
        val PERSONAL_CARE = TransactionCategory("personal_care", "Cuidados Pessoais")
        val GIFTS = TransactionCategory("gifts", "Presentes")
        val PETS = TransactionCategory("pets", "Pets")
        val INSURANCE = TransactionCategory("insurance", "Seguros")
        val SUBSCRIPTIONS = TransactionCategory("subscriptions", "Assinaturas")
        val OTHER = TransactionCategory("other", "Outros")

        val DEFAULT_CATEGORIES = listOf(
            FOOD,
            BILLS,
            ENTERTAINMENT,
            TRANSPORT,
            SHOPPING,
            SALARY,
            HEALTH,
            EDUCATION,
            INVESTMENT,
            HOUSING,
            CLOTHING,
            PERSONAL_CARE,
            GIFTS,
            PETS,
            INSURANCE,
            SUBSCRIPTIONS,
            OTHER
        )
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
