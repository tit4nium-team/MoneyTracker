package com.example.moneytracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import moneytracker.composeapp.generated.resources.Res
import moneytracker.composeapp.generated.resources.ic_account_balance
import moneytracker.composeapp.generated.resources.ic_arrow_back
import moneytracker.composeapp.generated.resources.ic_assessment
import moneytracker.composeapp.generated.resources.ic_attach_money
import moneytracker.composeapp.generated.resources.ic_error
import moneytracker.composeapp.generated.resources.ic_keyboard_arrow_down
import moneytracker.composeapp.generated.resources.ic_keyboard_arrow_up
import moneytracker.composeapp.generated.resources.ic_lightbulb
import moneytracker.composeapp.generated.resources.ic_money_off
import moneytracker.composeapp.generated.resources.ic_trending_up
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.Insight
import com.example.moneytracker.viewmodel.TransactionState
import com.example.moneytracker.viewmodel.InsightsViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: TransactionState,
    insightsViewModel: InsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val insightsState by insightsViewModel.state.collectAsState()

    LaunchedEffect(state.transactions) {
        insightsViewModel.generateInsights(state.transactions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análises Financeiras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                insightsState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Analisando suas finanças...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                insightsState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_error),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = insightsState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                insightsState.insights.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_assessment),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Aguardando Dados",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Adicione mais transações para receber análises personalizadas",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Suas Finanças em Detalhes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Descubra insights valiosos sobre seus gastos e economias",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(insightsState.insights) { insight ->
                            InsightCard(insight = insight)
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
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
                                        text = "Dica do Dia",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Mantenha suas finanças organizadas registrando todas as suas transações regularmente.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsightCard(insight: Insight) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconRes = when {
                        insight.title.contains("economia", ignoreCase = true) -> Res.drawable.ic_attach_money
                        insight.title.contains("gasto", ignoreCase = true) -> Res.drawable.ic_money_off
                        insight.title.contains("orçamento", ignoreCase = true) -> Res.drawable.ic_account_balance
                        else -> Res.drawable.ic_trending_up
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                         painter = painterResource(if (expanded) Res.drawable.ic_keyboard_arrow_up else Res.drawable.ic_keyboard_arrow_down),
                        contentDescription = if (expanded) "Recolher" else "Expandir"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = insight.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    insight.recommendation?.let { recommendation ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_lightbulb),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 