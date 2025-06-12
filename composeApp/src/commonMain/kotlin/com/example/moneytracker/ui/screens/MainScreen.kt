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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    authViewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigate: (String) -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Controle Financeiro") },
                navigationIcon = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            contentAlignment = Alignment.TopStart
        ) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Histórico") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onNavigate(Screen.MonthlyHistory.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Análises") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onNavigate(Screen.Insights.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Configurações") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onNavigate(Screen.Settings.route)
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("Sair") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        authViewModel.signOut()
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }


        Column(modifier = Modifier.padding(padding)) {
            DashboardScreen(
                viewModel = viewModel,
                categoryViewModel = categoryViewModel,
                onAddTransaction = { onNavigate(Screen.EditExpense.route) },
                onNavigateToHistory = { onNavigate(Screen.MonthlyHistory.route) },
                onNavigateToInsights = { onNavigate(Screen.Insights.route) },
                onSignOut = { authViewModel.signOut() }
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
