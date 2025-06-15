package com.example.moneytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    
    val currentDate = remember {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .let { "${it.month.name} ${it.year}" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orçamento - $currentDate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Orçamento")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.budgets) { budget ->
                    BudgetCard(
                        budget = budget,
                        onEdit = { selectedBudget = budget },
                        onDelete = { viewModel.deleteBudget(budget.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount ->
                viewModel.createBudget(category, amount)
                showAddDialog = false
            }
        )
    }

    selectedBudget?.let { budget ->
        EditBudgetDialog(
            budget = budget,
            onDismiss = { selectedBudget = null },
            onConfirm = { amount ->
                viewModel.updateBudget(budget, amount)
                selectedBudget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCard(
    budget: Budget,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = budget.progress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gasto: R$ ${budget.spent}")
                Text("Meta: R$ ${budget.amount}")
            }

            Text(
                text = "Restante: R$ ${budget.remaining}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (budget.remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    categories: List<TransactionCategory>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionCategory, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Orçamento") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedCategory?.let { category ->
                        amount.toDoubleOrNull()?.let { value ->
                            onConfirm(category, value)
                        }
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBudgetDialog(
    budget: Budget,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(budget.amount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Orçamento - ${budget.category.name}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Novo Valor") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { value ->
                        onConfirm(value)
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetItem(budget: Budget, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(budget.category.name) },
        supportingContent = { Text("R$ ${String.format("%.2f", budget.amount)}") },
        trailingContent = {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir orçamento",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Orçamento") },
            text = { Text("Tem certeza que deseja excluir este orçamento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavingsGoalItem(goal: SavingsGoal, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0)

    ListItem(
        headlineContent = { Text(goal.name) },
        supportingContent = {
            Column {
                Text("Meta: R$ ${String.format("%.2f", goal.targetAmount)}")
                Text("Atual: R$ ${String.format("%.2f", goal.currentAmount)}")
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir meta",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Meta") },
            text = { Text("Tem certeza que deseja excluir esta meta de economia?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 