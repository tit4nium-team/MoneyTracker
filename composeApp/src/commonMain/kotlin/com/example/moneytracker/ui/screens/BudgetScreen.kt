package com.example.moneytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.*

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
    var showMonthPicker by remember { mutableStateOf(false) }
    
    val currentDate = remember {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var selectedMonth by remember { mutableStateOf(currentDate.month) }
    var selectedYear by remember { mutableStateOf(currentDate.year) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.loadBudgets(selectedMonth.ordinal, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orçamentos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Orçamento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == Month.JANUARY) {
                            selectedMonth = Month.DECEMBER
                            selectedYear--
                        } else {
                            selectedMonth = selectedMonth.minus(1)
                        }
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Mês Anterior",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    TextButton(
                        onClick = { showMonthPicker = true }
                    ) {
                        Text(
                            text = "${selectedMonth.name} $selectedYear",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = {
                        if (selectedMonth == Month.DECEMBER) {
                            selectedMonth = Month.JANUARY
                            selectedYear++
                        } else {
                            selectedMonth = selectedMonth.plus(1)
                        }
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Próximo Mês",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Orçado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "R$ ${String.format("%.2f", state.budgets.sumOf { it.amount })}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Gasto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "R$ ${String.format("%.2f", state.budgets.sumOf { it.spent })}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = if (state.budgets.sumOf { it.amount } > 0) {
                            (state.budgets.sumOf { it.spent } / state.budgets.sumOf { it.amount }).toFloat().coerceIn(0f, 1f)
                        } else 0f,
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            state.budgets.sumOf { it.spent } >= state.budgets.sumOf { it.amount } * 0.9 -> MaterialTheme.colorScheme.error
                            state.budgets.sumOf { it.spent } >= state.budgets.sumOf { it.amount } * 0.7 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Restante: R$ ${String.format("%.2f", state.budgets.sumOf { it.amount - it.spent })}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.budgets.sumOf { it.amount - it.spent } < 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Nenhum orçamento definido",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Clique no + para adicionar um orçamento",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    }

    if (showMonthPicker) {
        AlertDialog(
            onDismissRequest = { showMonthPicker = false },
            title = { Text("Selecionar Mês") },
            text = {
                Column {
                    Month.entries.chunked(3).forEach { monthRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            monthRow.forEach { month ->
                                TextButton(
                                    onClick = {
                                        selectedMonth = month
                                        showMonthPicker = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (month == selectedMonth)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(month.name.substring(0, 3))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Ano Anterior")
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.KeyboardArrowRight, "Próximo Ano")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthPicker = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showAddDialog) {
        AddBudgetDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount, replicateForAllMonths ->
                viewModel.createBudget(category, amount, selectedMonth.ordinal, selectedYear, replicateForAllMonths)
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
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = budget.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Meta: R$ ${budget.amount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = budget.progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        budget.progress >= 0.9f -> MaterialTheme.colorScheme.error
                        budget.progress >= 0.7f -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gasto",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "R$ ${budget.spent}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Restante: R$ ${budget.remaining}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (budget.remaining < 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    categories: List<TransactionCategory>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionCategory, Double, Boolean) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var replicateForAllMonths by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(8.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(bottom = 4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Novo Orçamento",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Categoria
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Categoria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { 
                                    Text(
                                        "Selecione uma categoria",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        if (expanded) Icons.Default.KeyboardArrowUp 
                                        else Icons.Default.KeyboardArrowDown,
                                        "Expandir"
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = if (selectedCategory == category)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Valor
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Valor do Orçamento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { 
                                amount = it
                                showError = false
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { 
                                Text(
                                    "R$ ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            placeholder = { 
                                Text(
                                    "0,00",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            isError = showError,
                            supportingText = if (showError) {
                                { Text("Digite um valor válido") }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Opção de Replicar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replicateForAllMonths = !replicateForAllMonths }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replicar para todos os meses",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Criar este orçamento para todo o ano",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = replicateForAllMonths,
                            onCheckedChange = { replicateForAllMonths = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        amount.toDoubleOrNull()?.let { value ->
                            onConfirm(category, value, replicateForAllMonths)
                        } ?: run { showError = true }
                    }
                },
                enabled = selectedCategory != null && amount.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Criar Orçamento",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("R$ ") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { value ->
                        onConfirm(value)
                    }
                },
                enabled = amount.isNotEmpty()
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