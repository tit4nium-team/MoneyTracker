package com.example.moneytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import moneytracker.composeapp.generated.resources.Res
import moneytracker.composeapp.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.toCurrencyString
import com.example.moneytracker.viewmodel.TransactionState
import kotlinx.datetime.*

data class MonthYear(
    val year: Int,
    val month: Month
) : Comparable<MonthYear> {
    override fun compareTo(other: MonthYear): Int {
        val yearComparison = year.compareTo(other.year)
        return if (yearComparison != 0) {
            yearComparison
        } else {
            month.compareTo(other.month)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyHistoryScreen(
    state: TransactionState,
    onNavigateBack: () -> Unit
) {
    val monthlyData = state.transactions.groupBy { transaction ->
        val dateStr = transaction.date.toString()
        val date = if (dateStr.contains("GMT")) {
            // Parse date string format: "Wed Jun 11 20:30:02 GMT-03:00 2025"
            val regex = """(\w{3}) (\w{3}) (\d{1,2}) (\d{2}):(\d{2}):(\d{2}) GMT([+-]\d{2}):(\d{2}) (\d{4})""".toRegex()
            val match = regex.find(dateStr)
            
            if (match != null) {
                val groups = match.groupValues
                val month = groups[2] // Month abbreviation
                val day = groups[3]
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
                    else -> 1
                }
                
                LocalDate(
                    year = year.toInt(),
                    monthNumber = monthNumber,
                    dayOfMonth = day.toInt()
                )
            } else {
                // Fallback to current date if parsing fails
                Clock.System.now().toLocalDateTime(TimeZone.UTC).date
            }
        } else {
            try {
                // Try parsing as epoch milliseconds
                Instant.fromEpochMilliseconds(dateStr.toLong())
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            } catch (e: Exception) {
                // If all parsing fails, return current date
                Clock.System.now().toLocalDateTime(TimeZone.UTC).date
            }
        }

        val monthName = when (date.month) {
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
        
        "$monthName ${date.year}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico Mensal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (monthlyData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma transação registrada",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(monthlyData.entries.toList().sortedByDescending { it.key }) { (month, transactions) ->
                    MonthlyHistoryCard(month = month, transactions = transactions)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyHistoryCard(
    month: String,
    transactions: List<Transaction>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = month,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val balance = totalIncome - totalExpenses

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Receitas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "R$ ${totalIncome.toCurrencyString()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Despesas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "R$ ${totalExpenses.toCurrencyString()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column {
                    Text(
                        text = "Saldo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "R$ ${balance.toCurrencyString()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            transactions.forEach { transaction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = transaction.category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = when (transaction.type) {
                            TransactionType.INCOME -> "+R$ ${transaction.amount.toCurrencyString()}"
                            TransactionType.EXPENSE -> "-R$ ${transaction.amount.toCurrencyString()}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (transaction.type) {
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
} 