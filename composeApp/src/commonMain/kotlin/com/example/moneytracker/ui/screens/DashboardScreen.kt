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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.semantics.text
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp


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
    // ViewModel states
    val state by viewModel.state.collectAsState()
    // val categories by viewModel.categories.collectAsState() // Será usado nos novos componentes
    // val budgetState by budgetViewModel.state.collectAsState() // Será usado se integrarmos orçamento

    // O TopAppBar será removido para dar espaço ao título "Expense Overview" diretamente na área de conteúdo.
    // O FloatingActionButton também será removido, pois o novo design especifica uma barra de navegação inferior
    // com um botão de ação central.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface, // Fundo branco para toda a tela
        // Remover TopAppBar e FloatingActionButton conforme o novo design
        topBar = { /* No TopAppBar for this new design */ },
        floatingActionButton = { /* No FAB, new design implies it in BottomBar */ }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Aplicar padding do Scaffold
                .padding(horizontal = 16.dp), // Padding horizontal geral para o conteúdo
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espaço entre os itens principais
        ) {
            // Os itens da LazyColumn serão adicionados nos próximos passos:
            // 1. Título "Expense Overview"
            // 2. Cartões Resumo (SummaryCardColumn)
            // 3. Gráfico de Despesas Anuais (YearlyExpensesBarChart)
            // 4. Gráfico de Gastos Mensais (MonthlySpendingChart)
            // 5. Minhas Categorias (MyCategoriesList)
            // 6. Categorias em Tendência (TrendingCategoriesList)

            item {
                Text(
                    text = "Expense Overview",
                    style = MaterialTheme.typography.headlineMedium, // Um estilo um pouco maior
                    modifier = Modifier.padding(vertical = 8.dp) // Espaçamento vertical para o título
                )
            }

            item {
                val totalExpenses = state.transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                val transactionCount = state.transactions.size
                val monthlyChange = calculateMonthlyChange(state.transactions) // Função existente
                val lightGreen = Color(0xFF4CAF50) // Definida conforme Passo 1 do Plano

                SummaryCardColumn(
                    totalExpenses = totalExpenses,
                    transactionCount = transactionCount,
                    monthlyChange = monthlyChange,
                    lightGreen = lightGreen
                )
            }

            item {
                // Passando transações reais. Se quisermos forçar 2019-2022,
                // precisaríamos de mock data aqui. Por ora, usará os dados disponíveis.
                YearlyExpensesBarChart(transactions = state.transactions, modifier = Modifier.fillMaxWidth())
            }

            item {
                MonthlySpendingChart(modifier = Modifier.fillMaxWidth())
            }

            item {
                MyCategoriesList(modifier = Modifier.fillMaxWidth())
            }

            item {
                TrendingCategoriesList(modifier = Modifier.fillMaxWidth())
            }

            // Placeholder para os próximos itens (se houver mais seções)
            // item {
            //     Text(
            //         "Fim do Conteúdo",
            //         style = MaterialTheme.typography.bodyLarge,
            //         modifier = Modifier.padding(top = 8.dp)
            //     )
            // }
            // item { BalanceCard(state = state) } // Será removido ou incorporado de outra forma
            // item { QuickInsightCard(transactions = state.transactions) } // Será removido ou redesenhado

            // A TabRow e seu conteúdo (CategoryBreakdownTab, TransactionsTab, BudgetOverviewTab)
            // serão removidos pois o novo design é uma única coluna de informações no dashboard.
            // A navegação para essas seções, se ainda necessária, seria gerenciada pela nova
            // barra de navegação inferior.
        }
    }
}

@Composable
fun TrendingCategoriesList(modifier: Modifier = Modifier) {
    val lightGreen = Color(0xFF4CAF50)
    // Dados de exemplo
    val trendingCategoriesData = listOf(
        Triple("Dining", 300.0, 15.0),
        Triple("Entertainment", 200.0, -5.0),
        Triple("Groceries", 150.0, 10.0),
        Triple("Utilities", 100.0, 8.0),
        Triple("Subscriptions", 50.0, 0.0)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Trending categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            trendingCategoriesData.forEach { (name, spent, change) ->
                TrendingCategoryItem(
                    categoryName = name,
                    spentAmount = spent,
                    percentageChange = change,
                    lightGreen = lightGreen
                )
            }
        }
    }
}

@Composable
private fun TrendingCategoryItem(
    categoryName: String,
    spentAmount: Double,
    percentageChange: Double,
    lightGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícone pode ser adicionado aqui se desejado, similar ao CategoryListItem
            // Para este exemplo, manterei simples conforme a descrição "Nome/$gasto (Variação%)"
            Text(
                text = "$categoryName/$${spentAmount.toCurrencyString(0)}", // 0 para sem casas decimais no gasto
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = "(${if (percentageChange >= 0) "+" else ""}${String.format("%.0f", percentageChange)}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    percentageChange > 0 -> lightGreen
                    percentageChange < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun MyCategoriesList(modifier: Modifier = Modifier) {
    val lightGreen = Color(0xFF4CAF50)
    // Dados de exemplo
    val categoriesData = listOf(
        Triple("Food", "Spent: $1,200", -10.0),
        Triple("Shopping", "Clothing, Electronics | Spent: $800", 0.0), // Exemplo com mais detalhes
        Triple("Fuel & Transit", "Transportation | Spent: $300", 5.0)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "My categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp) // Padding abaixo do título da seção
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categoriesData.forEach { (name, details, monthlyChange) ->
                CategoryListItem(
                    categoryName = name,
                    details = details, // Inclui "Spent: $valor" e pode incluir descrição
                    monthlyChange = monthlyChange,
                    lightGreen = lightGreen,
                    // Placeholder para ícone, pode ser aprimorado depois
                    icon = Icons.Outlined.AccountBalanceWallet // Genérico por enquanto
                )
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    icon: ImageVector,
    categoryName: String,
    details: String, // Ex: "Spent: $1,200" ou "Clothing, Electronics | Spent: $800"
    monthlyChange: Double,
    lightGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = categoryName,
                tint = MaterialTheme.colorScheme.primary, // Cor padrão para ícones de categoria
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${if (monthlyChange >= 0) "+" else ""}${String.format("%.0f", monthlyChange)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    monthlyChange > 0 -> lightGreen
                    monthlyChange < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun MonthlySpendingChart(modifier: Modifier = Modifier) {
    // Dados de exemplo para o gráfico mensal
    val monthlyData = listOf(
        "Jan" to 320.0,
        "Feb" to 380.0,
        "Mar" to 420.0,
        "Apr" to 480.0
    )
    val maxSpending = monthlyData.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

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
                    monthlyData.forEachIndexed { index, (month, spending) ->
                        val barHeightPx = (spending / maxSpending * chartActualHeight.toPx())
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
                            text = month,
                            style = monthTextStyle
                        )

                        val textXOffset = barXOffset + (barWidth.toPx() / 2) - (textLayoutResult.size.width / 2)
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

// --- Novos Composables para o Redesign ---

@Composable
private fun SummaryCardColumn(
    totalExpenses: Double,
    transactionCount: Int,
    monthlyChange: Double,
    lightGreen: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IndividualSummaryCard(
            imageVector = Icons.Outlined.AccountBalanceWallet,
            label = "Total expenses",
            value = "$${totalExpenses.toCurrencyString()}",
            iconColor = MaterialTheme.colorScheme.error // Despesas geralmente associadas a "vermelho" ou cor de erro
        )
        IndividualSummaryCard(
            imageVector = Icons.Outlined.ListAlt,
            label = "No. of transactions",
            value = transactionCount.toString(),
            iconColor = MaterialTheme.colorScheme.primary // Cor neutra ou primária
        )
        IndividualSummaryCard(
            imageVector = if (monthlyChange >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
            label = "Monthly change",
            value = "${String.format("%.1f", monthlyChange)}%",
            iconColor = if (monthlyChange >= 0) lightGreen else MaterialTheme.colorScheme.error,
            valueColor = if (monthlyChange >= 0) lightGreen else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun IndividualSummaryCard(
    imageVector: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp)) // Espaço antes da seta
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "View details", // Pode ser null se a seta for puramente decorativa
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Os composables CategoryBreakdownTab, QuickInsightCard, CategoryProgressItem,
// getCategoryColor, getCategoryIcon, TransactionsTab, TransactionListItem,
// BalanceCard, BalanceItem, VerticalDivider, BudgetOverviewTab, BudgetOverviewCard,
// BudgetDetailRow, SummaryCards, SummaryCard, calculateMonthlyChange
// e YearlyExpensesBarChart (na sua forma antiga) serão removidos ou drasticamente
// alterados/substituídos pelos novos componentes do design.
// YearlyExpensesBarChart será modificado conforme o plano.
// Outros como SummaryCards, BalanceCard, etc., serão substituídos por novos composables.
// Manterei o `YearlyExpensesBarChart` por enquanto, pois ele será ajustado.
// Outros que não fazem parte do novo design serão removidos implicitamente ao
// não serem chamados na nova estrutura da LazyColumn.
// A remoção explícita do código não utilizado será feita ao final para limpeza.


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
            .fillMaxWidth()
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
                        val barYOffset = chartActualHeight.toPx() - barHeightPx // Y é de cima para baixo

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

                        val textXOffset = barXOffset + (barWidth.toPx() / 2) - (textLayoutResult.size.width / 2)
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