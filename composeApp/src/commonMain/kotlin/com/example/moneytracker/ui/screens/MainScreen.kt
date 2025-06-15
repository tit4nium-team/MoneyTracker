package com.example.moneytracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.Screen
import com.example.moneytracker.viewmodel.AuthViewModel
import com.example.moneytracker.viewmodel.TransactionViewModel
import com.example.moneytracker.viewmodel.CategoryViewModel
import com.example.moneytracker.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    onNavigate: (String) -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Histórico") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.MonthlyHistory.route)
                        }
                    },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Insights") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Insights.route)
                        }
                    },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Orçamentos") },
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Budget.route)
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Pergunte ao Gil") },
                    selected = selectedItem == 4,
                    onClick = {
                        selectedItem = 4
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Chat.route)
                        }
                    },
                    icon = { Icon(Icons.Default.ThumbUp, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Configurações") },
                    selected = selectedItem == 5,
                    onClick = {
                        selectedItem = 5
                        scope.launch {
                            drawerState.close()
                            onNavigate(Screen.Settings.route)
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Sair") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            authViewModel.signOut()
                            drawerState.close()
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        }
    ) {
        DashboardScreen(
            viewModel = viewModel,
            categoryViewModel = categoryViewModel,
            budgetViewModel = budgetViewModel,
            onAddTransaction = { onNavigate(Screen.EditExpense.route) },
            onNavigateToHistory = { onNavigate(Screen.MonthlyHistory.route) },
            onNavigateToInsights = { onNavigate(Screen.Insights.route) },
            onDrawerAction = { scope.launch { drawerState.open() } },
            onSignOut = {
                scope.launch {
                    authViewModel.signOut()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListItem2(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(transaction.description) },
        supportingContent = { Text(transaction.category.name) },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.INCOME -> "+R$ ${String.format("%.2f", transaction.amount)}"
                        TransactionType.EXPENSE -> "-R$ ${
                            String.format(
                                "%.2f",
                                transaction.amount
                            )
                        }"
                    },
                    color = when (transaction.type) {
                        TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem3(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = transaction.description.ifEmpty { transaction.category.name },
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    text = transaction.date,
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
                        text = when (transaction.type) {
                            TransactionType.INCOME -> "+$${
                                String.format(
                                    "%.2f",
                                    transaction.amount
                                )
                            }"

                            TransactionType.EXPENSE -> "-$${
                                String.format(
                                    "%.2f",
                                    transaction.amount
                                )
                            }"
                        },
                        color = when (transaction.type) {
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete transaction",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
