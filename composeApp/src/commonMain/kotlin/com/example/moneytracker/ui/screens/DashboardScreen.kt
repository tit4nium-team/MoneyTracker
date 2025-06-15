package com.example.moneytracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.viewmodel.TransactionState
import com.example.moneytracker.viewmodel.TransactionViewModel
import com.example.moneytracker.viewmodel.CategoryViewModel
import com.example.moneytracker.viewmodel.BudgetViewModel
import com.example.moneytracker.service.GeminiServiceFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    onDrawerAction: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Transactions", "Budget Overview")
    val state by viewModel.state.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Tracker") },
                navigationIcon = {
                    IconButton(onClick = { onDrawerAction() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance Card - Always visible
            BalanceCard(state = state)

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> CategoryBreakdownTab(state = state, categories = categories)
                1 -> TransactionsTab(
                    transactions = state.transactions,
                    categories = categories,
                    viewModel = viewModel,
                    categoryViewModel = categoryViewModel,
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    modifier = Modifier.fillMaxSize()
                )

                2 -> BudgetOverviewTab(
                    budgets = budgetState.budgets,
                    transactions = state.transactions,
                    onNavigateToBudget = { /* Implement navigation to BudgetScreen */ }
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownTab(
    state: TransactionState,
    categories: List<TransactionCategory>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = remember(state.transactions) {
        state.transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Insight Card
        item {
            QuickInsightCard(transactions = state.transactions)
        }

        // Category Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (categoryTotals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Nenhuma transação registrada ainda",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Divisão por categoria",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        categoryTotals.forEach { (category, amount) ->
                            CategoryProgressItem(
                                category = category,
                                amount = amount,
                                totalExpenses = state.totalExpenses
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickInsightCard(transactions: List<Transaction>) {
    var insight by remember { mutableStateOf("Analisando suas finanças...") }
    var isLoading by remember { mutableStateOf(true) }
    val geminiService = remember { GeminiServiceFactory.getInstance() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactions) {
        isLoading = true
        scope.launch {
            insight = geminiService.generateFinancialInsights(transactions)
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CategoryProgressItem(
    category: TransactionCategory,
    amount: Double,
    totalExpenses: Double
) {
    val percentage = if (totalExpenses > 0) (amount / totalExpenses) * 100 else 0.0
    val categoryColor = getCategoryColor(category)
    val categoryIcon = getCategoryIcon(category)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "R$ %.2f".format(amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = "%.1f%%".format(percentage),
                style = MaterialTheme.typography.bodyMedium,
                color = categoryColor,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = (percentage / 100).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.1f)
        )
    }
}

private fun getCategoryColor(category: TransactionCategory): Color {
    return when (category) {
        TransactionCategory.FOOD -> Color(0xFFF44336) // Vermelho
        TransactionCategory.TRANSPORT -> Color(0xFF2196F3) // Azul
        TransactionCategory.ENTERTAINMENT -> Color(0xFF9C27B0) // Roxo
        TransactionCategory.SHOPPING -> Color(0xFFE91E63) // Rosa
        TransactionCategory.HEALTH -> Color(0xFF4CAF50) // Verde
        TransactionCategory.EDUCATION -> Color(0xFF3F51B5) // Índigo
        TransactionCategory.BILLS -> Color(0xFFFF9800) // Laranja
        TransactionCategory.SALARY -> Color(0xFF009688) // Verde-água
        TransactionCategory.INVESTMENT -> Color(0xFF795548) // Marrom
        TransactionCategory.HOUSING -> Color(0xFF8BC34A) // Verde-claro
        TransactionCategory.CLOTHING -> Color(0xFFFF4081) // Rosa-escuro
        TransactionCategory.PERSONAL_CARE -> Color(0xFF9575CD) // Roxo-claro
        TransactionCategory.GIFTS -> Color(0xFFFFB74D) // Laranja-claro
        TransactionCategory.PETS -> Color(0xFF4DB6AC) // Verde-água-claro
        TransactionCategory.INSURANCE -> Color(0xFF90A4AE) // Azul-cinza-claro
        TransactionCategory.SUBSCRIPTIONS -> Color(0xFF7E57C2) // Roxo-médio
        else -> Color(0xFF607D8B) // Azul-cinza
    }
}

private fun getCategoryIcon(category: TransactionCategory): ImageVector {
    return when (category) {
        TransactionCategory.FOOD -> Icons.Default.Add
        TransactionCategory.TRANSPORT -> Icons.Default.Add
        TransactionCategory.ENTERTAINMENT -> Icons.Default.Add
        TransactionCategory.SHOPPING -> Icons.Default.Add
        TransactionCategory.HEALTH -> Icons.Default.Add
        TransactionCategory.EDUCATION -> Icons.Default.Add
        TransactionCategory.BILLS -> Icons.Default.Add
        TransactionCategory.SALARY -> Icons.Default.Add
        TransactionCategory.INVESTMENT -> Icons.Default.Add
        TransactionCategory.HOUSING -> Icons.Default.Add
        TransactionCategory.CLOTHING -> Icons.Default.Add
        TransactionCategory.PERSONAL_CARE -> Icons.Default.Add
        TransactionCategory.GIFTS -> Icons.Default.Add
        TransactionCategory.PETS -> Icons.Default.Add
        TransactionCategory.INSURANCE -> Icons.Default.Add
        TransactionCategory.SUBSCRIPTIONS -> Icons.Default.Add
        else -> Icons.Default.Add
    }
}

private val TransactionCategory.displayName: String
    get() = when (this) {
        TransactionCategory.FOOD -> "Alimentação"
        TransactionCategory.TRANSPORT -> "Transporte"
        TransactionCategory.ENTERTAINMENT -> "Entretenimento"
        TransactionCategory.SHOPPING -> "Compras"
        TransactionCategory.HEALTH -> "Saúde"
        TransactionCategory.EDUCATION -> "Educação"
        TransactionCategory.BILLS -> "Contas"
        TransactionCategory.SALARY -> "Salário"
        TransactionCategory.INVESTMENT -> "Investimentos"
        TransactionCategory.HOUSING -> "Moradia"
        TransactionCategory.CLOTHING -> "Vestuário"
        TransactionCategory.PERSONAL_CARE -> "Cuidados Pessoais"
        TransactionCategory.GIFTS -> "Presentes"
        TransactionCategory.PETS -> "Animais de Estimação"
        TransactionCategory.INSURANCE -> "Seguros"
        TransactionCategory.SUBSCRIPTIONS -> "Assinaturas"
        TransactionCategory.OTHER -> "Outros"
        else -> "Outros"
    }

@Composable
fun TransactionsTab(
    transactions: List<Transaction>,
    categories: List<TransactionCategory>,
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { selectedCategory = category },
                    label = { Text(category.toString()) },
                    leadingIcon = if (selectedCategory?.id == category.id) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    trailingIcon = if (category.isCustom) {
                        {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        categoryViewModel.deleteCategory(category.id)
                                    }
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete category",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Add Category Button
            item {
                FilterChip(
                    selected = false,
                    onClick = { showAddCategoryDialog = true },
                    label = { Text("Add Category") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Filtered transactions list
        val filteredTransactions = selectedCategory?.let { category ->
            transactions.filter { it.category.id == category.id }
        } ?: transactions

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (selectedCategory != null)
                            "Nenhuma transação encontrada para ${selectedCategory.toString()}"
                        else
                            "Nenhuma transação registrada ainda",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction(transaction.id) }
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Adicionar Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            scope.launch {
                                categoryViewModel.addCategory(categoryName).collect { result ->
                                    result.onSuccess {
                                        showAddCategoryDialog = false
                                        categoryName = ""
                                    }.onFailure {
                                        showError = true
                                    }
                                }
                            }
                        }
                    },
                    enabled = categoryName.isNotBlank()
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddCategoryDialog = false
                    categoryName = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Erro") },
            text = { Text("Falha ao adicionar categoria. Por favor, tente novamente.") },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
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
            .clickable { showDeleteDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = transaction.date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "R$ ${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == TransactionType.INCOME)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Transação") },
            text = { Text("Tem certeza que deseja excluir esta transação?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BalanceCard(state: TransactionState) {
    var showBalance by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (showBalance) 1f else 0f,
        animationSpec = tween(1000)
    )

    LaunchedEffect(Unit) {
        showBalance = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Total",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "R$ ${String.format("%.2f", state.balance)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (state.balance >= 0)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.alpha(animatedProgress)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceItem(
                    icon = Icons.Default.Add,
                    label = "Receitas",
                    value = state.totalIncome,
                    color = MaterialTheme.colorScheme.primary,
                    progress = animatedProgress
                )

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                )

                BalanceItem(
                    icon = Icons.Default.Delete,
                    label = "Despesas",
                    value = state.totalExpenses,
                    color = MaterialTheme.colorScheme.error,
                    progress = animatedProgress
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    icon: ImageVector,
    label: String,
    value: Double,
    color: Color,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(progress)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Text(
            text = "R$ ${String.format("%.2f", value)}",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(color)
    )
}

@Composable
private fun BudgetOverviewTab(
    budgets: List<Budget>,
    transactions: List<Transaction>,
    onNavigateToBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (budgets.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Nenhum orçamento definido",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Button(
                    onClick = onNavigateToBudget,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Criar Orçamento")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Visão Geral do Orçamento",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(budgets) { budget ->
                BudgetOverviewCard(
                    budget = budget,
                    transactions = transactions.filter {
                        it.category == budget.category &&
                                it.type == TransactionType.EXPENSE
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetOverviewCard(
    budget: Budget,
    transactions: List<Transaction>
) {
    val totalSpent = transactions.sumOf { it.amount }
    val progress = (totalSpent / budget.amount).coerceIn(0.0, 1.0)
    val isOverBudget = totalSpent > budget.amount
    val remaining = budget.amount - totalSpent

    var showDetails by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(1000)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { showDetails = !showDetails },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = budget.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "R$ ${String.format("%.2f", budget.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    isOverBudget -> MaterialTheme.colorScheme.error
                    progress > 0.9 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    progress > 0.7 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            if (showDetails) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    BudgetDetailRow(
                        label = "Gasto",
                        value = totalSpent,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BudgetDetailRow(
                        label = "Restante",
                        value = remaining,
                        color = when {
                            isOverBudget -> MaterialTheme.colorScheme.error
                            remaining < budget.amount * 0.2 -> MaterialTheme.colorScheme.error.copy(
                                alpha = 0.7f
                            )

                            remaining < budget.amount * 0.3 -> MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.7f
                            )

                            else -> MaterialTheme.colorScheme.primary
                        }
                    )

                    if (isOverBudget) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Orçamento excedido em R$ ${
                                    String.format(
                                        "%.2f",
                                        -remaining
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetDetailRow(
    label: String,
    value: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "R$ ${String.format("%.2f", value)}",
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
} 