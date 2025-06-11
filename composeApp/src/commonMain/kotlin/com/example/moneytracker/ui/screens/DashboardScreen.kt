package com.example.moneytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.viewmodel.TransactionState
import com.example.moneytracker.viewmodel.TransactionViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Transações")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("MoneyTracker") },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Saldo Atual",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "R$ ${String.format("%.2f", state.balance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.balance >= 0) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Income and Expenses Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Income Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Entradas",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "R$ ${String.format("%.2f", state.totalIncome)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Expenses Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Saídas",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "R$ ${String.format("%.2f", state.totalExpenses)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.ShoppingCart, "Add transaction")
            }
        }
    ) { padding ->
        when (selectedTabIndex) {
            0 -> DashboardTab(
                state = state,
                modifier = Modifier.padding(padding)
            )
            1 -> TransactionsTab(
                transactions = state.transactions,
                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun DashboardTab(
    state: TransactionState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Prepare data for monthly trend
    val monthlyData = state.transactions
        .groupBy { it.date.substring(0, 7) } // Group by YYYY-MM
        .mapValues { (_, transactions) ->
            val income = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val expenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            income to expenses
        }
        .toList()
        .sortedBy { it.first } // Sort by date
        .takeLast(6) // Last 6 months

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Category Summary with Pie Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Gastos por Categoria",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Circular progress indicators for categories
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp)
//                        .padding(vertical = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    TransactionCategory.values()
//                        .filter { category ->
//                            state.transactions
//                                .any { it.type == TransactionType.EXPENSE && it.category == category }
//                        }
//                        .forEach { category ->
//                            val categoryExpenses = state.transactions
//                                .filter { it.type == TransactionType.EXPENSE && it.category == category }
//                                .sumOf { it.amount }
//                            val percentage = (categoryExpenses / state.totalExpenses)
//
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                modifier = Modifier.padding(horizontal = 4.dp)
//                            ) {
//                                Box(
//                                    contentAlignment = Alignment.Center,
//                                    modifier = Modifier.size(60.dp)
//                                ) {
//                                    CircularProgressIndicator(
//                                        progress = percentage.toFloat(),
//                                        modifier = Modifier.fillMaxSize(),
//                                        color = MaterialTheme.colorScheme.primary,
//                                        trackColor = MaterialTheme.colorScheme.primaryContainer,
//                                        strokeWidth = 8.dp
//                                    )
//                                    Text(
//                                        "${(percentage * 100).toInt()}%",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                }
//                                Text(
//                                    category.name,
//                                    style = MaterialTheme.typography.bodySmall,
//                                    maxLines = 1,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//                        }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
                
                // Category details list
                TransactionCategory.values().forEach { category ->
                    val categoryExpenses = state.transactions
                        .filter { it.type == TransactionType.EXPENSE && it.category == category }
                        .sumOf { it.amount }
                    
                    if (categoryExpenses > 0) {
                        val percentage = (categoryExpenses / state.totalExpenses * 100)
                        
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            category.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "${String.format("%.1f", percentage)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Text(
                                    "R$ ${String.format("%.2f", categoryExpenses)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = (categoryExpenses / state.totalExpenses).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Monthly Trend Card
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface
//            )
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text(
//                    "Tendência Mensal",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//
//                if (monthlyData.isNotEmpty()) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .padding(vertical = 16.dp)
//                    ) {
//                        // Find max value for scaling
//                        val maxValue = monthlyData.maxOf { (_, pair) -> maxOf(pair.first, pair.second) }
//
//                        // Graph area
//                        Row(
//                            modifier = Modifier
//                                .weight(1f)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.Bottom
//                        ) {
//                            monthlyData.forEach { (month, values) ->
//                                Column(
//                                    modifier = Modifier.weight(1f),
//                                    horizontalAlignment = Alignment.CenterHorizontally,
//                                    verticalArrangement = Arrangement.Bottom
//                                ) {
//                                    // Income bar
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxWidth(0.4f)
//                                            .height(160.dp * (values.first / maxValue).toFloat())
//                                            .background(
//                                                MaterialTheme.colorScheme.primary,
//                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
//                                            )
//                                    )
//                                    // Expenses bar
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxWidth(0.4f)
//                                            .height(160.dp * (values.second / maxValue).toFloat())
//                                            .background(
//                                                MaterialTheme.colorScheme.error,
//                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
//                                            )
//                                    )
//                                    Text(
//                                        month.substring(5), // Show only MM
//                                        style = MaterialTheme.typography.bodySmall,
//                                        modifier = Modifier.padding(top = 4.dp)
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    // Legend
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 8.dp),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(12.dp)
//                                .background(MaterialTheme.colorScheme.primary, CircleShape)
//                        )
//                        Text(
//                            "Entradas",
//                            modifier = Modifier.padding(start = 4.dp, end = 16.dp),
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                        Box(
//                            modifier = Modifier
//                                .size(12.dp)
//                                .background(MaterialTheme.colorScheme.error, CircleShape)
//                        )
//                        Text(
//                            "Saídas",
//                            modifier = Modifier.padding(start = 4.dp),
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                } else {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Sem dados para exibir")
//                    }
//                }
//            }
//        }

        // Income vs Expenses Comparison
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Comparativo Mensal",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (monthlyData.isNotEmpty()) {
                    val currentMonth = monthlyData.last()
                    val previousMonth = monthlyData.dropLast(1).lastOrNull()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Current Month
                        Text(
                            "Mês Atual (${currentMonth.first.substring(5)})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Entradas: R$ ${String.format("%.2f", currentMonth.second.first)}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Saídas: R$ ${String.format("%.2f", currentMonth.second.second)}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                "Saldo: R$ ${String.format("%.2f", currentMonth.second.first - currentMonth.second.second)}",
                                fontWeight = FontWeight.Bold,
                                color = if (currentMonth.second.first >= currentMonth.second.second)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }

                        if (previousMonth != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Previous Month
                            Text(
                                "Mês Anterior (${previousMonth.first.substring(5)})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Entradas: R$ ${String.format("%.2f", previousMonth.second.first)}",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Saídas: R$ ${String.format("%.2f", previousMonth.second.second)}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Text(
                                    "Saldo: R$ ${String.format("%.2f", previousMonth.second.first - previousMonth.second.second)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (previousMonth.second.first >= previousMonth.second.second)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }

                            // Month over Month comparison
                            Spacer(modifier = Modifier.height(16.dp))
                            val currentBalance = currentMonth.second.first - currentMonth.second.second
                            val previousBalance = previousMonth.second.first - previousMonth.second.second
                            val difference = currentBalance - previousBalance
                            val percentageChange = if (previousBalance != 0.0) {
                                (difference / previousBalance.absoluteValue) * 100
                            } else 0.0

                            Text(
                                "Variação: ${String.format("%.1f", percentageChange)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (difference >= 0)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sem dados para comparação")
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsTab(
    transactions: List<Transaction>,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = transactions,
            key = { it.id }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                onDelete = { onDeleteTransaction(transaction.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    transaction.description.ifEmpty { transaction.category.name },
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transaction.type == TransactionType.INCOME)
                            "+R$ ${String.format("%.2f", transaction.amount)}"
                        else
                            "-R$ ${String.format("%.2f", transaction.amount)}",
                        color = if (transaction.type == TransactionType.INCOME)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Deletar transação",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            leadingContent = {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.INCOME)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Deletar Transação") },
            text = { Text("Tem certeza que deseja deletar esta transação?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Deletar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 