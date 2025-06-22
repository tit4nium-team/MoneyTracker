package com.example.moneytracker.viewmodel

import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.InsightType
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.math.abs
import com.example.moneytracker.util.toCurrencyString
import com.example.moneytracker.util.formatDecimalPlaces

data class InsightsState(
    val insights: List<Insight> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InsightsViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    private fun parseDate(dateStr: String): LocalDateTime {
        // Handle the format "Wed Jun 11 20:30:02 GMT-03:00 2025"
        val regex = """(\w{3}) (\w{3}) (\d{1,2}) (\d{2}):(\d{2}):(\d{2}) GMT([+-]\d{2}):(\d{2}) (\d{4})""".toRegex()
        val match = regex.find(dateStr)
        
        return if (match != null) {
            val groups = match.groupValues
            val month = groups[2] // Month abbreviation
            val day = groups[3]
            val hour = groups[4]
            val minute = groups[5]
            val second = groups[6]
            val year = groups[9]
            
            val monthNumber = when (month) {
                "Jan" -> 1
                "Feb" -> 2
                "Mar" -> 3
                "Apr" -> 4
                "May" -> 5
                "Jun" -> 6
                "Jul" -> 7
                "Aug" -> 8
                "Sep" -> 9
                "Oct" -> 10
                "Nov" -> 11
                "Dec" -> 12
                else -> 1 // Should not happen with the regex
            }
            
            LocalDateTime(
                year = year.toInt(),
                monthNumber = monthNumber,
                dayOfMonth = day.toInt(),
                hour = hour.toInt(),
                minute = minute.toInt(),
                second = second.toInt(),
                nanosecond = 0
            )
        } else {
            // Fallback to epoch milliseconds or current time if not parsable by regex
            try {
                Instant.fromEpochMilliseconds(dateStr.toLong())
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            } catch (e: NumberFormatException) { // More specific exception
                // If not a valid Long, then it's an unparseable string format for this simplified parser
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
    }

    fun generateInsights(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            _state.value = InsightsState(insights = emptyList())
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        scope.launch {
            try {
                val insights = analyzeTransactions(transactions)
                _state.value = InsightsState(insights = insights)
            } catch (e: Exception) {
                _state.value = InsightsState(error = e.message)
            }
        }
    }

    private fun analyzeTransactions(transactions: List<Transaction>): List<Insight> {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses

        val insights = mutableListOf<Insight>()

        // Insight sobre saúde financeira geral
        insights.add(
            Insight(
                title = "Saúde Financeira Geral",
                description = "Sua renda total é R$ ${totalIncome.toCurrencyString()} e suas despesas são R$ ${totalExpenses.toCurrencyString()}, resultando em um saldo ${if (balance >= 0) "positivo" else "negativo"} de R$ ${abs(balance).toCurrencyString()}.",
                recommendation = if (balance < 0) {
                    "Considere reduzir despesas ou encontrar fontes adicionais de renda para melhorar sua saúde financeira."
                } else {
                    "Ótimo trabalho mantendo um saldo positivo! Considere investir ou poupar o excedente."
                },
                type = InsightType.GENERAL
            )
        )

        // Padrões de gastos por categoria
        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.name }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (categoryExpenses.isNotEmpty()) {
            val topCategory = categoryExpenses.first()
            insights.add(
                Insight(
                    title = "Categoria com Maior Gasto",
                    description = "Sua categoria com maior gasto é '${topCategory.first}' com R$ ${topCategory.second.toCurrencyString()}.",
                    recommendation = "Revise seus gastos nesta categoria para identificar oportunidades de economia.",
                    type = InsightType.SPENDING_PATTERN
                )
            )
        }

        // Tendências mensais
        val monthlyData = transactions.groupBy { transaction ->
            val dateTime = parseDate(transaction.date.toString())
            val monthName = when (dateTime.month) {
                Month.JANUARY -> "Janeiro"
                Month.FEBRUARY -> "Fevereiro"
                Month.MARCH -> "Março"
                Month.APRIL -> "Abril"
                Month.MAY -> "Maio"
                Month.JUNE -> "Junho"
                Month.JULY -> "Julho"
                Month.AUGUST -> "Agosto"
                Month.SEPTEMBER -> "Setembro"
                Month.OCTOBER -> "Outubro"
                Month.NOVEMBER -> "Novembro"
                Month.DECEMBER -> "Dezembro"
            }
            "$monthName ${dateTime.year}"
        }

        if (monthlyData.size >= 2) {
            val monthlyTotals = monthlyData.mapValues { entry ->
                entry.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            }
            val trend = if (monthlyTotals.values.toList().takeLast(2).let { it[1] > it[0] }) "aumentando" else "diminuindo"
            
            insights.add(
                Insight(
                    title = "Tendência de Gastos Mensais",
                    description = "Seus gastos mensais estão $trend em comparação com o mês anterior.",
                    recommendation = if (trend == "aumentando") {
                        "Considere revisar seus gastos recentes para identificar áreas onde você pode economizar."
                    } else {
                        "Ótimo trabalho reduzindo seus gastos! Continue assim."
                    },
                    type = InsightType.INCOME_TREND
                )
            )
        }

        // Análise de economia
        if (totalIncome > 0) {
            val savingsRate = ((totalIncome - totalExpenses) / totalIncome * 100).coerceIn(0.0, 100.0)
            insights.add(
                Insight(
                    title = "Taxa de Economia",
                    description = "Sua taxa de economia atual é de ${savingsRate.formatDecimalPlaces(1)}% da sua renda.",
                    recommendation = when {
                        savingsRate >= 20.0 -> "Excelente taxa de economia! Considere investir parte desse valor para objetivos de longo prazo."
                        savingsRate >= 10.0 -> "Boa taxa de economia. Tente aumentar gradualmente para 20% para maior segurança financeira."
                        else -> "Sua taxa de economia está baixa. Tente identificar despesas não essenciais que podem ser reduzidas."
                    },
                    type = InsightType.SAVING_OPPORTUNITY
                )
            )
        }

        // Alerta de orçamento
        val monthlyAvgExpenses = if (monthlyData.isNotEmpty()) {
            totalExpenses / monthlyData.size
        } else {
            totalExpenses
        }

        if (categoryExpenses.any { it.second > monthlyAvgExpenses * 0.4 }) {
            val highExpenseCategory = categoryExpenses.first { it.second > monthlyAvgExpenses * 0.4 }
            insights.add(
                Insight(
                    title = "Alerta de Orçamento",
                    description = "A categoria '${highExpenseCategory.first}' representa uma parte significativa (${(highExpenseCategory.second / totalExpenses * 100).formatDecimalPlaces(1)}%) dos seus gastos totais.",
                    recommendation = "Considere estabelecer um limite de orçamento para esta categoria ou procurar alternativas mais econômicas.",
                    type = InsightType.BUDGET_ALERT
                )
            )
        }

        return insights
    }
} 