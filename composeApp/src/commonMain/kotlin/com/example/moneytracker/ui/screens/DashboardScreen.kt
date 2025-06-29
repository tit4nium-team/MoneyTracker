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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import moneytracker.composeapp.generated.resources.Res
import moneytracker.composeapp.generated.resources.ic_account_balance
import moneytracker.composeapp.generated.resources.ic_add
import moneytracker.composeapp.generated.resources.ic_assessment
import moneytracker.composeapp.generated.resources.ic_attach_money
import moneytracker.composeapp.generated.resources.ic_clear
import moneytracker.composeapp.generated.resources.ic_close
import moneytracker.composeapp.generated.resources.ic_directions_car
import moneytracker.composeapp.generated.resources.ic_event
import moneytracker.composeapp.generated.resources.ic_favorite
import moneytracker.composeapp.generated.resources.ic_info
import moneytracker.composeapp.generated.resources.ic_menu
import moneytracker.composeapp.generated.resources.ic_person
import moneytracker.composeapp.generated.resources.ic_pets
import moneytracker.composeapp.generated.resources.ic_restaurant
import moneytracker.composeapp.generated.resources.ic_shopping_cart
import moneytracker.composeapp.generated.resources.ic_trending_up
import org.jetbrains.compose.resources.painterResource
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
import androidx.compose.ui.semantics.text
import com.example.moneytracker.util.DateTimeUtil
import com.example.moneytracker.util.toCurrencyString
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    onDrawerAction: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit, // Not used currently, but kept for future
    onNavigateToInsights: () -> Unit, // Not used currently, but kept for future
    onNavigateToBudget: () -> Unit,
    onSignOut: () -> Unit // Not used directly in UI, but kept for future
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Visão Geral", "Transações", "Orçamentos")
    val state by viewModel.state.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Painel Financeiro", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { onDrawerAction() }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Adicionar Transação") },
                text = { Text("Nova Transação") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background) // Added background color
        ) {
            // Balance Card - Always visible, enhanced styling
            BalanceCard(state = state, modifier = Modifier.padding(16.dp))

            // Tabs - Enhanced styling
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            width = Dp.Hairline // Thinner indicator
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    PrimaryTab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab content with animated transitions
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    slideInHorizontally { height -> height } + fadeIn() togetherWith
                            slideOutHorizontally { height -> -height } + fadeOut()
                },
                label = "TabContentAnimation"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> OverviewTab(
                        state = state,
                        categories = categories,
                        modifier = Modifier.fillMaxSize()
                    )
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
                        onNavigateToBudget = onNavigateToBudget,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    state: TransactionState,
    categories: List<TransactionCategory>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Adjusted padding
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuickInsightCard(transactions = state.transactions)
        }

        item {
            MonthlySpendingChartCard(transactions = state.transactions) // Added MonthlySpendingChart
        }

        item {
            CategoryBreakdownCard(state = state, categories = categories)
        }
    }
}

@Composable
private fun MonthlySpendingChartCard(transactions: List<Transaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        com.example.moneytracker.ui.components.MonthlySpendingChart(
            transactions = transactions,
            modifier = Modifier.padding(8.dp) // Add some padding around the chart itself
        )
    }
}


@Composable
private fun CategoryBreakdownCard( // Renamed from CategoryBreakdownTab for clarity
    state: TransactionState,
    categories: List<TransactionCategory> // This might not be needed if category info is in Transaction
) {
    val categoryTotals = remember(state.transactions) {
        state.transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category } // Assuming Transaction.category holds enough info
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount }
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Added elevation
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Slightly different background
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Gastos por Categoria",
                style = MaterialTheme.typography.titleLarge, // Enhanced typography
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (categoryTotals.isEmpty()) {
                NoDataMessage(
                    message = "Nenhuma despesa registrada para exibir o detalhamento por categoria.",
                    icon = Icons.Filled.PieChart
                )
            } else {
                categoryTotals.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                    CategoryProgressItem(
                        category = category, // Pass the whole category object
                        amount = amount,
                        totalExpenses = state.totalExpenses
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


@Composable
private fun QuickInsightCard(transactions: List<Transaction>) {
    var insight by remember { mutableStateOf("Analisando suas finanças...") }
    var isLoading by remember { mutableStateOf(true) }
    val geminiService = remember { GeminiServiceFactory.getInstance() } // Consider injecting
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactions) {
        isLoading = true
        // Small delay to simulate loading and improve UX for quick data changes
        delay(300)
        scope.launch {
            try {
                val insightsList = geminiService.generateFinancialInsights(transactions)
                insight = if (insightsList.isNotEmpty()) {
                    insightsList.first().recommendation.orEmpty().take(150) + // Slightly longer preview
                            if (insightsList.first().recommendation.orEmpty().length > 150) "..." else ""
                } else {
                    "Nenhum insight disponível no momento."
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., network errors
                insight = "Não foi possível carregar os insights."
            } finally {
                isLoading = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Changed color for distinction
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(targetState = isLoading, label = "InsightIconAnimation") { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp), // Slightly larger
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            strokeWidth = 2.dp // Thinner stroke
                        )
                    } else {
                        Icon(
                            Icons.Filled.Assessment, // Using Material Icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Dica Financeira", // Changed title
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(targetState = insight, label = "InsightTextAnimation") { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6 // Increased line height
                )
            }
        }
    }
}

@Composable
private fun CategoryProgressItem(
    category: TransactionCategory,
    amount: Double,
    totalExpenses: Double
) {
    val percentage = if (totalExpenses > 0) (amount / totalExpenses).coerceIn(0.0, 1.0) else 0.0
    val categoryColor = getCategoryColor(category) // Ensure this returns Compose Color

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Increased spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp) // Slightly larger icon container
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)), // Softer background
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(getCategoryIconResource(category)), // Use painterResource
                        contentDescription = category.displayName,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp) // Larger icon
                    )
                }
                Column {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.bodyLarge, // Enhanced typography
                        fontWeight = FontWeight.Medium // Added weight
                    )
                    Text(
                        text = "R$ ${amount.toCurrencyString()}",
                        style = MaterialTheme.typography.bodyMedium, // Enhanced typography
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = "${(percentage * 100).toInt()}%", // Simpler percentage display
                style = MaterialTheme.typography.bodyLarge,
                color = categoryColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp)) // Adjusted spacing

        LinearProgressIndicator(
            progress = { percentage.toFloat() }, // Use lambda for state
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp) // Thicker progress bar
                .clip(RoundedCornerShape(4.dp)), // More rounded corners
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f) // Softer track color
        )
    }
}


// Updated to use Material Icons where appropriate and painterResource for drawables
@Composable
private fun getCategoryIconResource(category: TransactionCategory): org.jetbrains.compose.resources.DrawableResource {
    return when (category) {
        TransactionCategory.FOOD -> Res.drawable.ic_restaurant
        TransactionCategory.TRANSPORT -> Res.drawable.ic_directions_car
        TransactionCategory.ENTERTAINMENT -> Res.drawable.ic_favorite
        TransactionCategory.SHOPPING -> Res.drawable.ic_shopping_cart
        TransactionCategory.HEALTH -> Res.drawable.ic_favorite // Consider ic_medical_services if available
        TransactionCategory.EDUCATION -> Res.drawable.ic_info // Consider ic_school if available
        TransactionCategory.BILLS -> Res.drawable.ic_account_balance
        TransactionCategory.SALARY -> Res.drawable.ic_attach_money
        TransactionCategory.INVESTMENT -> Res.drawable.ic_trending_up
        TransactionCategory.HOUSING -> Res.drawable.ic_account_balance // Consider ic_home if available
        TransactionCategory.CLOTHING -> Res.drawable.ic_person // Consider specific clothing icon
        TransactionCategory.PERSONAL_CARE -> Res.drawable.ic_person // Consider specific care icon
        TransactionCategory.GIFTS -> Res.drawable.ic_favorite // Consider ic_card_giftcard
        TransactionCategory.PETS -> Res.drawable.ic_pets
        TransactionCategory.INSURANCE -> Res.drawable.ic_account_balance // Consider ic_shield
        TransactionCategory.SUBSCRIPTIONS -> Res.drawable.ic_event // Consider ic_subscriptions
        else -> Res.drawable.ic_attach_money // Default
    }
}

// Using Jetpack Compose Material Icons (ensure imports are correct)
private fun getCategoryMaterialIcon(category: TransactionCategory): ImageVector {
    return when (category) {
        TransactionCategory.FOOD -> Icons.Filled.Restaurant
        TransactionCategory.TRANSPORT -> Icons.Filled.DirectionsCar
        TransactionCategory.ENTERTAINMENT -> Icons.Filled.Favorite // Example, choose appropriate
        TransactionCategory.SHOPPING -> Icons.Filled.ShoppingCart
        TransactionCategory.HEALTH -> Icons.Filled.Favorite // Example, choose appropriate
        TransactionCategory.EDUCATION -> Icons.Filled.School
        TransactionCategory.BILLS -> Icons.Filled.AccountBalance // Example
        TransactionCategory.SALARY -> Icons.Filled.AttachMoney
        TransactionCategory.INVESTMENT -> Icons.Filled.TrendingUp
        TransactionCategory.HOUSING -> Icons.Filled.Home
        TransactionCategory.CLOTHING -> Icons.Filled.Person // Example
        TransactionCategory.PERSONAL_CARE -> Icons.Filled.Person // Example
        TransactionCategory.GIFTS -> Icons.Filled.Favorite // Example
        TransactionCategory.PETS -> Icons.Filled.Pets
        TransactionCategory.INSURANCE -> Icons.Filled.AccountBalance // Example
        TransactionCategory.SUBSCRIPTIONS -> Icons.Filled.Event // Example
        else -> Icons.Filled.AttachMoney // Default
    }
}


private fun getCategoryColor(category: TransactionCategory): Color {
    // Using MaterialTheme colors for better theme adaptability where possible
    return when (category) {
        TransactionCategory.FOOD -> Color(0xFFF06292) // Light Pink
        TransactionCategory.TRANSPORT -> Color(0xFF4FC3F7) // Light Blue
        TransactionCategory.ENTERTAINMENT -> Color(0xFFBA68C8) // Light Purple
        TransactionCategory.SHOPPING -> Color(0xFFE57373) // Light Red
        TransactionCategory.HEALTH -> Color(0xFF81C784) // Light Green
        TransactionCategory.EDUCATION -> Color(0xFF64B5F6) // Blue
        TransactionCategory.BILLS -> Color(0xFFFFB74D) // Light Orange
        TransactionCategory.SALARY -> Color(0xFF26A69A) // Teal
        TransactionCategory.INVESTMENT -> Color(0xFF90A4AE) // Blue Grey
        TransactionCategory.HOUSING -> Color(0xFFAED581) // Light Green
        TransactionCategory.CLOTHING -> Color(0xFF7986CB) // Indigo Light
        TransactionCategory.PERSONAL_CARE -> Color(0xFF9575CD) // Deep Purple Light
        TransactionCategory.GIFTS -> Color(0xFFFF8A65) // Deep Orange Light
        TransactionCategory.PETS -> Color(0xFF4DB6AC) // Teal Light
        TransactionCategory.INSURANCE -> Color(0xFF78909C) // Blue Grey Light
        TransactionCategory.SUBSCRIPTIONS -> Color(0xFFF06292) // Pink (match entertainment for now)
        else -> MaterialTheme.colorScheme.secondary // Default theme color
    }
}


private val TransactionCategory.displayName: String
    get() = when {
        isCustom -> name // If custom, use the direct name
        this == TransactionCategory.FOOD -> "Alimentação"
        this == TransactionCategory.TRANSPORT -> "Transporte"
        this == TransactionCategory.ENTERTAINMENT -> "Lazer" // Changed
        this == TransactionCategory.SHOPPING -> "Compras"
        this == TransactionCategory.HEALTH -> "Saúde"
        this == TransactionCategory.EDUCATION -> "Educação"
        this == TransactionCategory.BILLS -> "Contas Fixas" // Changed
        this == TransactionCategory.SALARY -> "Salário"
        this == TransactionCategory.INVESTMENT -> "Investimentos"
        this == TransactionCategory.HOUSING -> "Moradia"
        this == TransactionCategory.CLOTHING -> "Vestuário"
        this == TransactionCategory.PERSONAL_CARE -> "Cuidados Pessoais"
        this == TransactionCategory.GIFTS -> "Presentes e Doações" // Changed
        this == TransactionCategory.PETS -> "Animais de Estimação"
        this == TransactionCategory.INSURANCE -> "Seguros"
        this == TransactionCategory.SUBSCRIPTIONS -> "Assinaturas"
        this == TransactionCategory.OTHER -> "Outros"
        // Ensure all enum values are handled or have a default
        else -> this.name // Fallback to enum name if not specified
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsTab(
    transactions: List<Transaction>,
    categories: List<TransactionCategory>,
    viewModel: TransactionViewModel, // Consider if needed directly, or pass specific actions
    categoryViewModel: CategoryViewModel,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryDialog by remember { mutableStateOf<TransactionCategory?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(top = 8.dp)) { // Added top padding
        // Category filter chips - Enhanced styling
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Todas") },
                    elevation = FilterChipDefaults.filterChipElevation(defaultElevation = 2.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            items(categories.sortedBy { it.displayName }) { category -> // Sorted categories
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(category.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    trailingIcon = if (category.isCustom) { // Trailing icon for custom categories
                        {
                            Icon(
                                Icons.Filled.Edit, // Or Clear, depending on desired action
                                contentDescription = "Editar/Excluir categoria customizada",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { showDeleteCategoryDialog = category } // Example action
                            )
                        }
                    } else null,
                    elevation = FilterChipDefaults.filterChipElevation(defaultElevation = 2.dp),
                     border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                AssistChip( // Changed to AssistChip for "add new"
                    onClick = { showAddCategoryDialog = true },
                    label = { Text("Nova Categoria") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Adicionar nova categoria",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    elevation = AssistChipDefaults.assistChipElevation(defaultElevation = 2.dp),
                    border = AssistChipDefaults.assistChipBorder(
                         borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            }
        }

        val filteredTransactions = remember(transactions, selectedCategory) {
            (selectedCategory?.let { category ->
                transactions.filter { it.category.id == category.id }
            } ?: transactions).sortedByDescending { DateTimeUtil.stringToDate(it.date) } // Sort by parsed date
        }


        if (filteredTransactions.isEmpty()) {
            NoDataMessage(
                message = if (selectedCategory != null) "Nenhuma transação encontrada para a categoria '${selectedCategory?.displayName}'." else "Nenhuma transação registrada ainda.",
                icon = Icons.Filled.Info // Or a more specific icon
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
            ) {
                items(
                    items = filteredTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionListItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction(transaction.id) }
                    )
                }
            }
        }


        if (showAddCategoryDialog) {
            ModernAlertDialog(
                onDismissRequest = {
                    showAddCategoryDialog = false
                    newCategoryName = ""
                    showError = false
                },
                title = "Nova Categoria",
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it; showError = false },
                            label = { Text("Nome da Categoria") },
                            singleLine = true,
                            isError = showError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (showError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall, // Adjusted style
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButtonText = "Adicionar",
                onConfirm = {
                    if (newCategoryName.isNotBlank()) {
                        scope.launch {
                            categoryViewModel.addCategory(newCategoryName).collect { result ->
                                result.onSuccess {
                                    showAddCategoryDialog = false
                                    newCategoryName = ""
                                    showError = false
                                }.onFailure { error ->
                                    errorMessage = "Erro: ${error.message ?: "Falha ao adicionar."}"
                                    showError = true
                                }
                            }
                        }
                    } else {
                        errorMessage = "O nome não pode estar vazio."
                        showError = true
                    }
                },
                dismissButtonText = "Cancelar"
            )
        }

        showDeleteCategoryDialog?.let { category ->
            ModernAlertDialog(
                onDismissRequest = { showDeleteCategoryDialog = null },
                title = "Excluir Categoria",
                text = {
                    Text(
                        "Tem certeza que deseja excluir '${category.displayName}'? " +
                                "As transações serão movidas para 'Outros'."
                    )
                },
                confirmButtonText = "Excluir",
                onConfirm = {
                    scope.launch {
                        categoryViewModel.deleteCategory(category.id)
                        showDeleteCategoryDialog = null
                        if (selectedCategory?.id == category.id) {
                            selectedCategory = null
                        }
                    }
                },
                dismissButtonText = "Cancelar",
                confirmButtonColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card( // Wrap ListItem in a Card for better visual separation and elevation
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    transaction.description.take(50), // Limit description length
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = transaction.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDateForDisplay(transaction.date), // Use helper for formatting
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            leadingContent = { // Add an icon representing the category or transaction type
                Icon(
                    painter = painterResource(getCategoryIconResource(transaction.category)),
                    contentDescription = transaction.category.displayName,
                    tint = getCategoryColor(transaction.category),
                    modifier = Modifier.size(36.dp) // Adjust size as needed
                )
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp), // Reduced spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.amount.toCurrencyString(), // Just amount, color indicates type
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (transaction.type) {
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary // Or a specific green
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error // Or a specific red
                        }
                    )
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) { // Smaller icon button
                        Icon(
                            Icons.Filled.Close, // Using Material Icon
                            contentDescription = "Excluir transação",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // Softer delete icon
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent) // Make ListItem transparent
        )
    }

    if (showDeleteDialog) {
        ModernAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Excluir Transação",
            text = { Text("Tem certeza que deseja excluir esta transação?") },
            confirmButtonText = "Excluir",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            },
            dismissButtonText = "Cancelar",
            confirmButtonColor = MaterialTheme.colorScheme.error
        )
    }
}

// Helper to format date string for display, potentially more user-friendly
private fun formatDateForDisplay(dateStr: String): String {
    return try {
        val localDate = DateTimeUtil.stringToDate(dateStr)
        // Format: "dd MMM" (e.g., "25 Dez") or "dd MMM yyyy" if not current year
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        if (localDate.year == currentYear) {
            "${localDate.dayOfMonth} ${monthAbbreviation(localDate.month)}"
        } else {
            "${localDate.dayOfMonth} ${monthAbbreviation(localDate.month)} ${localDate.year}"
        }
    } catch (e: Exception) {
        dateStr // Fallback to original string if parsing fails
    }
}

private fun monthAbbreviation(month: Month): String {
    return when (month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Fev"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Abr"
        Month.MAY -> "Mai"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Ago"
        Month.SEPTEMBER -> "Set"
        Month.OCTOBER -> "Out"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dez"
        else -> "" // Should not happen
    }
}


@Composable
private fun BalanceCard(state: TransactionState, modifier: Modifier = Modifier) {
    var showBalance by remember { mutableStateOf(false) }
    // Animate values themselves for smoother appearance
    val animatedBalance by animateFloatAsState(
        targetValue = state.balance.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing), label = ""
    )
    val animatedIncome by animateFloatAsState(
        targetValue = state.totalIncome.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing), label = ""
    )
    val animatedExpenses by animateFloatAsState(
        targetValue = state.totalExpenses.toFloat(),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing), label = ""
    )

    LaunchedEffect(Unit) {
        delay(200) // Slight delay before starting animation
        showBalance = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Increased elevation
        shape = RoundedCornerShape(16.dp), // More rounded corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Atual", // Changed title
                style = MaterialTheme.typography.titleMedium, // Adjusted style
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "R$ ${animatedBalance.toDouble().toCurrencyString()}",
                style = MaterialTheme.typography.displaySmall.copy( // Larger font for balance
                    fontWeight = FontWeight.ExtraBold // Bolder balance
                ),
                color = if (animatedBalance >= 0)
                    MaterialTheme.colorScheme.onPrimaryContainer // Use onPrimaryContainer for positive
                else MaterialTheme.colorScheme.errorContainer, // Use errorContainer for negative
            )

            Spacer(modifier = Modifier.height(20.dp)) // Adjusted spacing

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // Space around for better distribution
            ) {
                BalanceItem(
                    icon = Icons.Filled.TrendingUp, // Material Icon for income
                    label = "Receitas",
                    value = animatedIncome.toDouble(),
                    color = MaterialTheme.colorScheme.primary, // Consistent with theme
                    isVisible = showBalance // Control visibility for staggered animation if needed
                )

                VerticalDivider(
                    modifier = Modifier.height(60.dp), // Adjusted height
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f) // Softer divider
                )

                BalanceItem(
                    icon = Icons.Filled.TrendingUp, // Material Icon for expenses (TrendingDown not standard)
                    label = "Despesas",
                    value = animatedExpenses.toDouble(),
                    color = MaterialTheme.colorScheme.error, // Consistent with theme
                    isVisible = showBalance,
                    isExpense = true // To potentially flip icon or change style
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
    isVisible: Boolean,
    isExpense: Boolean = false // Optional flag for styling expenses differently
) {
    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = if (isExpense) 150 else 0), label = "" // Stagger animation
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alphaAnimation)
    ) {
        Icon(
            imageVector = icon, // Use ImageVector
            contentDescription = label,
            tint = color,
            modifier = Modifier
                .size(32.dp) // Slightly larger icon
                // Rotate icon for expenses if it's a trending up icon
                .then(if (isExpense) Modifier.graphicsLayer(rotationZ = 180f) else Modifier)
        )

        Spacer(modifier = Modifier.height(6.dp)) // Adjusted spacing

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge, // Adjusted style
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )

        Text(
            text = "R$ ${value.toCurrencyString()}",
            style = MaterialTheme.typography.titleLarge, // Adjusted style
            color = color,
            fontWeight = FontWeight.SemiBold // Slightly bolder
        )
    }
}


@Composable
private fun VerticalDivider( // Kept for flexibility, though Material 3 Divider can also be used
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // Use outline color
) {
    Box(
        modifier = modifier
            .fillMaxHeight() // Ensure it can fill height if needed in a Row
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
        NoDataMessage(
            message = "Você ainda não definiu nenhum orçamento. Que tal criar um agora?",
            icon = Icons.Filled.Add, // More relevant icon
            actionButtonText = "Criar Orçamento",
            onActionButtonClick = onNavigateToBudget
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Consistent padding
            verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seus Orçamentos",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onNavigateToBudget) {
                        Text("Gerenciar") // "Manage" or "Edit"
                    }
                }
            }

            items(budgets.sortedBy { it.category.name }) { budget -> // Sorted budgets
                BudgetOverviewCard(
                    budget = budget,
                    transactions = transactions.filter {
                        it.category.id == budget.category.id && // Ensure IDs are compared
                                it.type == TransactionType.EXPENSE &&
                                // Filter transactions within the budget's period (e.g., current month)
                                isTransactionInCurrentMonth(it.date) // Assuming budget is monthly
                    }
                )
            }
        }
    }
}

// Example helper function (assuming monthly budgets for now)
private fun isTransactionInCurrentMonth(dateString: String): Boolean {
    return try {
        val transactionDate = DateTimeUtil.stringToDate(dateString)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        transactionDate.year == now.year && transactionDate.month == now.month
    } catch (e: Exception) {
        false // If date parsing fails, assume not in current month
    }
}


@Composable
private fun BudgetOverviewCard(
    budget: Budget,
    transactions: List<Transaction> // These should be pre-filtered for the budget's category and period
) {
    val totalSpent = transactions.sumOf { it.amount }
    val progress = if (budget.amount > 0) (totalSpent / budget.amount).coerceIn(0.0, 1.0) else 0.0
    val isOverBudget = totalSpent > budget.amount
    val remainingOrOverspent = budget.amount - totalSpent

    var showDetails by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // .padding(horizontal = 16.dp, vertical = 8.dp) // Padding applied by LazyColumn spacing
            .clickable { showDetails = !showDetails },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Consistent with CategoryBreakdownCard
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        painter = painterResource(getCategoryIconResource(budget.category)),
                        contentDescription = "Categoria: ${budget.category.displayName}",
                        modifier = Modifier.size(40.dp), // Consistent icon size
                        tint = getCategoryColor(budget.category)
                    )
                    Text(
                        text = budget.category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Limite: ${budget.amount.toCurrencyString()}", // Clarified label
                    style = MaterialTheme.typography.labelLarge, // Adjusted style
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp) // Thicker bar
                    .clip(RoundedCornerShape(5.dp)), // More rounded
                color = when {
                    isOverBudget -> MaterialTheme.colorScheme.error
                    progress > 0.85 -> MaterialTheme.colorScheme.tertiary // Warning color
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f) // Softer track
            )

            Spacer(modifier = Modifier.height(12.dp)) // Spacing before details

            // Always show basic spending info, expand for more
            BudgetDetailRow(
                label = "Gasto Total",
                value = totalSpent,
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    BudgetDetailRow(
                        label = if (isOverBudget) "Excedido em" else "Restante",
                        value = if (isOverBudget) -remainingOrOverspent else remainingOrOverspent,
                        color = when {
                            isOverBudget -> MaterialTheme.colorScheme.error
                            remainingOrOverspent < budget.amount * 0.15 -> MaterialTheme.colorScheme.tertiary // Warning
                            else -> MaterialTheme.colorScheme.primary // Good state
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
                                Icons.Filled.Info, // Material Icon
                                contentDescription = "Alerta de orçamento excedido",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Você ultrapassou o limite deste orçamento!",
                                style = MaterialTheme.typography.labelMedium, // Adjusted style
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value.toCurrencyString(), // Currency formatting applied
            style = MaterialTheme.typography.bodyLarge, // Consistent style
            color = color,
            fontWeight = FontWeight.SemiBold // Slightly bolder
        )
    }
}

// A generic composable for displaying when there's no data in a section
@Composable
private fun NoDataMessage(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp), // More padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Decorative
                modifier = Modifier.size(64.dp), // Larger icon
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) // Softer tint
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium, // More prominent message
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Softer text
            )
            if (actionButtonText != null && onActionButtonClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onActionButtonClick,
                    shape = RoundedCornerShape(12.dp) // More rounded button
                ) {
                    Text(actionButtonText)
                }
            }
        }
    }
}

// A modern styled AlertDialog
@Composable
private fun ModernAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: @Composable (() -> Unit)? = null,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null, // Optional separate dismiss action
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    dismissButtonColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = text,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismissRequest() // Usually dismiss after confirm
                },
                colors = ButtonDefaults.textButtonColors(contentColor = confirmButtonColor)
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(
                    onClick = {
                        onDismiss?.invoke()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = dismissButtonColor)
                ) {
                    Text(it)
                }
            }
        },
        shape = RoundedCornerShape(16.dp), // More rounded dialog
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, // Elevated surface color
        tonalElevation = 8.dp
    )
}