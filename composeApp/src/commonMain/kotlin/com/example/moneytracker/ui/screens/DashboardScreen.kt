package com.example.moneytracker.ui.screens

import MoneyTrackerTheme
import Primary
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
import androidx.compose.foundation.Canvas
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.util.DateTimeUtil
import com.example.moneytracker.util.toCurrencyString
import kotlinx.datetime.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.nativeCanvas // To get the underlying native canvas if absolutely needed, but prefer DrawScope methods
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

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
    onNavigateToBudget: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Transações", "Orçamento")
    val state by viewModel.state.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Money Tracker") },
                navigationIcon = {
                    IconButton(onClick = { onDrawerAction() }) {
                        Icon(
                            painterResource(Res.drawable.ic_menu),
                            contentDescription = "Open menu"
                        )
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
                Icon(painterResource(Res.drawable.ic_add), contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Cards de resumo
            SummaryCards(state = state)
            // Gráfico de barras anual
            YearlyExpensesBarChart(transactions = state.transactions)
            // Balance Card - Always visible
            BalanceCard(state = state)
            Spacer(modifier = Modifier.height(8.dp))
            QuickInsightCard(transactions = state.transactions)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "meus cartões",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
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
                    onNavigateToBudget = onNavigateToBudget
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
                                    painterResource(Res.drawable.ic_account_balance),
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

                        MonthlySpendingChart(state.transactions)

                        YearlyExpensesBarChart(state.transactions)


                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySpendingChart(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    // Dados de exemplo para o gráfico mensal

    //val maxSpending = monthlyData.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

    val barWidth: Dp = 32.dp
    val chartActualHeight: Dp = 120.dp
    val barSpacing: Dp = 16.dp
    val textMeasureAreaHeight: Dp = 20.dp
    val textPaddingTop: Dp = 4.dp
    val totalChartHeight = chartActualHeight + textMeasureAreaHeight
    val lightGreen = Color(0xFF4CAF50)

    val textMeasurer = rememberTextMeasurer()
    val monthTextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total monthly spending", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.height(totalChartHeight).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    transactions.forEachIndexed { index, transaction ->
                        val barHeightPx = (transaction.amount / 100 * chartActualHeight.toPx())
                            .toFloat()
                            .coerceAtLeast(0f)

                        val barXOffset = index * (barWidth.toPx() + barSpacing.toPx())
                        val barYOffset = chartActualHeight.toPx() - barHeightPx

                        drawRoundRect(
                            color = lightGreen,
                            topLeft = Offset(x = barXOffset, y = barYOffset),
                            size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeightPx),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )

                        val textLayoutResult = textMeasurer.measure(
                            text = transaction.date,
                            style = monthTextStyle
                        )

                        val textXOffset =
                            barXOffset + (barWidth.toPx() / 2) - (textLayoutResult.size.width / 2)
                        val textYOffset = chartActualHeight.toPx() + textPaddingTop.toPx()

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(x = textXOffset, y = textYOffset)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyExpensesBarChart(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    val expensesByYear = transactions.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.date.take(4) }
        .mapValues { it.value.sumOf { t -> t.amount } }
    val years = expensesByYear.keys.sorted()
    val maxExpense = expensesByYear.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val barWidth: Dp = 32.dp
    val chartActualHeight: Dp = 120.dp // Renomeado de chartHeight para clareza
    val barSpacing: Dp = 16.dp
    val textMeasureAreaHeight: Dp = 20.dp // Espaço reservado para o texto abaixo das barras
    val textPaddingTop: Dp = 4.dp // Espaçamento entre a base do gráfico e o topo do texto
    val totalChartHeight = chartActualHeight + textMeasureAreaHeight

    val textMeasurer = rememberTextMeasurer()
    val yearTextStyle = TextStyle(
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Card(
        modifier = modifier
            .fillMaxWidth(),
        // .padding(16.dp), // Padding será gerenciado pela LazyColumn e espaçamento interno
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Yearly expenses", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.height(totalChartHeight).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // size.height aqui é totalChartHeight.toPx()

                    years.forEachIndexed { index, year ->
                        val expense = expensesByYear[year] ?: 0.0
                        // Calcula a altura da barra baseada na altura real do gráfico (chartActualHeight)
                        val barHeightPx = (expense / maxExpense * chartActualHeight.toPx())
                            .toFloat()
                            .coerceAtLeast(0f)

                        val barXOffset = index * (barWidth.toPx() + barSpacing.toPx())
                        val barYOffset =
                            chartActualHeight.toPx() - barHeightPx // Y é de cima para baixo

                        drawRoundRect(
                            color = Color(0xFF4CAF50), // Verde claro definido (Success color)
                            topLeft = Offset(x = barXOffset, y = barYOffset),
                            size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeightPx),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()) // Reduzir um pouco o raio
                        )

                        // Medir e desenhar o texto do ano usando DrawScope.drawText
                        val textLayoutResult = textMeasurer.measure(
                            text = year,
                            style = yearTextStyle
                        )

                        val textXOffset =
                            barXOffset + (barWidth.toPx() / 2) - (textLayoutResult.size.width / 2)
                        val textYOffset = chartActualHeight.toPx() + textPaddingTop.toPx()

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(x = textXOffset, y = textYOffset)
                        )
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
            val insightsList = geminiService.generateFinancialInsights(transactions)
            insight = if (insightsList.isNotEmpty()) {
                // Limita o texto a um tamanho razoável e adiciona reticências se necessário
                val recommendation = insightsList.first().recommendation.orEmpty()
                if (recommendation.length > 120) {
                    recommendation.take(120) + "..."
                } else {
                    recommendation
                }
            } else {
                "Nenhum insight disponível." // Or some other default message
            }
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_assessment),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Dica do Dia",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth(),
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.5
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
                        painter = painterResource(getCategoryIcon(category)),
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
                        text = "R$ ${amount.toCurrencyString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = "${kotlin.math.round(percentage * 10) / 10.0}%",
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

@Composable
private fun getCategoryIcon(category: TransactionCategory): org.jetbrains.compose.resources.DrawableResource {
    return when (category) {
        TransactionCategory.FOOD -> Res.drawable.ic_restaurant
        TransactionCategory.TRANSPORT -> Res.drawable.ic_directions_car
        TransactionCategory.ENTERTAINMENT -> Res.drawable.ic_favorite
        TransactionCategory.SHOPPING -> Res.drawable.ic_shopping_cart
        TransactionCategory.HEALTH -> Res.drawable.ic_favorite // Placeholder, ideal: ic_local_hospital or similar
        TransactionCategory.EDUCATION -> Res.drawable.ic_info // Placeholder, ideal: ic_school or similar
        TransactionCategory.BILLS -> Res.drawable.ic_account_balance
        TransactionCategory.SALARY -> Res.drawable.ic_attach_money
        TransactionCategory.INVESTMENT -> Res.drawable.ic_trending_up
        TransactionCategory.HOUSING -> Res.drawable.ic_account_balance // Placeholder, ideal: ic_home or similar
        TransactionCategory.CLOTHING -> Res.drawable.ic_person
        TransactionCategory.PERSONAL_CARE -> Res.drawable.ic_person
        TransactionCategory.GIFTS -> Res.drawable.ic_favorite
        TransactionCategory.PETS -> Res.drawable.ic_pets
        TransactionCategory.INSURANCE -> Res.drawable.ic_account_balance // Placeholder
        TransactionCategory.SUBSCRIPTIONS -> Res.drawable.ic_event // Placeholder
        else -> Res.drawable.ic_attach_money // Generic placeholder for others
    }
}

private val TransactionCategory.displayName: String
    get() = when {
        isCustom -> name // Se for uma categoria customizada, usa o nome direto
        this == TransactionCategory.FOOD -> "Alimentação"
        this == TransactionCategory.TRANSPORT -> "Transporte"
        this == TransactionCategory.ENTERTAINMENT -> "Entretenimento"
        this == TransactionCategory.SHOPPING -> "Compras"
        this == TransactionCategory.HEALTH -> "Saúde"
        this == TransactionCategory.EDUCATION -> "Educação"
        this == TransactionCategory.BILLS -> "Contas"
        this == TransactionCategory.SALARY -> "Salário"
        this == TransactionCategory.INVESTMENT -> "Investimentos"
        this == TransactionCategory.HOUSING -> "Moradia"
        this == TransactionCategory.CLOTHING -> "Vestuário"
        this == TransactionCategory.PERSONAL_CARE -> "Cuidados Pessoais"
        this == TransactionCategory.GIFTS -> "Presentes"
        this == TransactionCategory.PETS -> "Animais de Estimação"
        this == TransactionCategory.INSURANCE -> "Seguros"
        this == TransactionCategory.SUBSCRIPTIONS -> "Assinaturas"
        this == TransactionCategory.OTHER -> "Outros"
        else -> TransactionCategory.OTHER.displayName
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
    var showDeleteCategoryDialog by remember { mutableStateOf<TransactionCategory?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Category filter chips
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
                    label = { Text("Todas") }
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { selectedCategory = category },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.displayName)
                            if (category.isCustom) {
                                IconButton(
                                    onClick = { showDeleteCategoryDialog = category },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        painterResource(Res.drawable.ic_clear),
                                        contentDescription = "Excluir categoria",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            item {
                FilterChip(
                    selected = false,
                    onClick = { showAddCategoryDialog = true },
                    label = { Text("Nova Categoria") },
                    leadingIcon = {
                        Icon(
                            painterResource(Res.drawable.ic_add),
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

        // Filtered and sorted transactions list
        val filteredTransactions = selectedCategory?.let { category ->
            transactions.filter { it.category.id == category.id }
        } ?: transactions

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredTransactions.sortedByDescending { it.date },
                key = { it.id }
            ) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onDelete = { onDeleteTransaction(transaction.id) }
                )
            }
        }

        // Add Category Dialog
        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Nova Categoria") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Nome da Categoria") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (showError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                scope.launch {
                                    categoryViewModel.addCategory(newCategoryName)
                                        .collect { result ->
                                            result.onSuccess {
                                                showAddCategoryDialog = false
                                                newCategoryName = ""
                                                showError = false
                                            }.onFailure { error ->
                                                errorMessage =
                                                    "Falha ao adicionar categoria: ${error.message}"
                                                showError = true
                                            }
                                        }
                                }
                            }
                        },
                        enabled = newCategoryName.isNotBlank()
                    ) {
                        Text("Adicionar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddCategoryDialog = false
                            newCategoryName = ""
                            showError = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Delete Category Dialog
        showDeleteCategoryDialog?.let { category ->
            AlertDialog(
                onDismissRequest = { showDeleteCategoryDialog = null },
                title = { Text("Excluir Categoria") },
                text = {
                    Text(
                        "Tem certeza que deseja excluir a categoria '${category.displayName}'? " +
                                "Todas as transações desta categoria serão movidas para 'Outros'."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                categoryViewModel.deleteCategory(category.id)
                                showDeleteCategoryDialog = null
                                if (selectedCategory?.id == category.id) {
                                    selectedCategory = null
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteCategoryDialog = null }) {
                        Text("Cancelar")
                    }
                }
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

    ListItem(
        headlineContent = { Text(transaction.description) },
        supportingContent = {
            Column {
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.INCOME -> "+R$ ${transaction.amount.toCurrencyString()}"
                        TransactionType.EXPENSE -> "-R$ ${transaction.amount.toCurrencyString()}"
                    },
                    color = when (transaction.type) {
                        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painterResource(Res.drawable.ic_close),
                        contentDescription = "Excluir transação",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

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

private fun formatDate(dateStr: String): String {
    return DateTimeUtil.formatDateForDashboard(dateStr)
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
        )
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
                text = "R$ ${state.balance.toCurrencyString()}",
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
                    icon = painterResource(Res.drawable.ic_add),
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
                    icon = painterResource(Res.drawable.ic_close),
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
    icon: androidx.compose.ui.graphics.painter.Painter,
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
            painter = icon,
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
            text = "R$ ${value.toCurrencyString()}",
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
                    painterResource(Res.drawable.ic_add),
                    contentDescription = "Definir Novo Orçamento",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Você ainda não tem nenhum orçamento definido",
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(Res.drawable.ic_add), contentDescription = null)
                        Text("Definir Novo Orçamento")
                    }
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
                        painter = painterResource(Res.drawable.ic_account_balance),
                        contentDescription = "Categoria do Orçamento",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = budget.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "R$ ${budget.amount.toCurrencyString()}",
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
                                painter = painterResource(Res.drawable.ic_info),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Orçamento excedido em R$ ${(-remaining).toCurrencyString()}",
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
            text = "R$ ${value.toCurrencyString()}",
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SummaryCards(state: TransactionState) {
    val totalExpenses =
        state.transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val transactionCount = state.transactions.size
    val monthlyChange = calculateMonthlyChange(state.transactions)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            icon = painterResource(Res.drawable.ic_close),
            label = "Total expenses",
            value = "R$ ${totalExpenses.toCurrencyString()}"
        )
        SummaryCard(
            icon = painterResource(Res.drawable.ic_event),
            label = "No. of transactions",
            value = "$transactionCount"
        )
        SummaryCard(
            icon = painterResource(Res.drawable.ic_trending_up),
            label = "Monthly change",
            value = "${if (monthlyChange >= 0) "+" else ""}${monthlyChange}%"
        )
    }
}

@Composable
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private fun calculateMonthlyChange(transactions: List<Transaction>): Double {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val thisMonth = now.monthNumber
    val thisYear = now.year
    val lastMonth = if (thisMonth == 1) 12 else thisMonth - 1
    val lastMonthYear = if (thisMonth == 1) thisYear - 1 else thisYear
    val thisMonthTotal = transactions.filter {
        it.type == TransactionType.EXPENSE &&
                it.date.take(7) == "${thisMonth - thisYear}" //"%04d-%02d".format(thisYear, thisMonth)
    }.sumOf { it.amount }
    val lastMonthTotal = transactions.filter {
        it.type == TransactionType.EXPENSE &&
                it.date.take(7) == "${lastMonth - lastMonth}"//"%04d-%02d".format(lastMonthYear, lastMonth)
    }.sumOf { it.amount }
    return if (lastMonthTotal == 0.0) 0.0 else ((thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100
}

@Composable
private fun YearlyExpensesBarChart(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    val expensesByYear = transactions.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.date.take(4) }
        .mapValues { it.value.sumOf { t -> t.amount } }
    val years = expensesByYear.keys.sorted()
    val maxExpense = expensesByYear.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val barWidth: Dp = 32.dp
    val chartHeight: Dp = 120.dp
    val barSpacing: Dp = 16.dp
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Yearly expenses", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.height(chartHeight).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    years.forEachIndexed { i, year ->
                        val expense = expensesByYear[year] ?: 0.0
                        val barHeight = (expense / maxExpense * size.height).toFloat()
                        drawRoundRect(
                            color = Primary.copy(alpha = 0.2f),
                            topLeft = Offset(
                                x = i * (barWidth.toPx() + barSpacing.toPx()),
                                y = size.height - barHeight
                            ),
                            size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )
                        // Desenhar o texto do ano centralizado abaixo da barra, usando nativeCanvas
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = year,
                                topLeft = Offset(
                                    x = i * (barWidth.toPx() + barSpacing.toPx()) + barWidth.toPx() / 2,
                                    y = size.height + 4.dp.toPx() // Adjust y-position as needed, consider text alignment
                                ),
                                TextStyle.Default
                            )
                        }
                    }
                }
            }
        }
    }
}