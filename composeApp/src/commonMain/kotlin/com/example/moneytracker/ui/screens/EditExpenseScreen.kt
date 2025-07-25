package com.example.moneytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import moneytracker.composeapp.generated.resources.Res
import moneytracker.composeapp.generated.resources.ic_account_balance
import moneytracker.composeapp.generated.resources.ic_add
import moneytracker.composeapp.generated.resources.ic_arrow_back
import moneytracker.composeapp.generated.resources.ic_arrow_forward
import moneytracker.composeapp.generated.resources.ic_assessment
import moneytracker.composeapp.generated.resources.ic_directions_car
import moneytracker.composeapp.generated.resources.ic_favorite
import moneytracker.composeapp.generated.resources.ic_info
import moneytracker.composeapp.generated.resources.ic_person
import moneytracker.composeapp.generated.resources.ic_pets
import moneytracker.composeapp.generated.resources.ic_restaurant
import moneytracker.composeapp.generated.resources.ic_shopping_cart
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.Calculator
import com.example.moneytracker.viewmodel.TransactionViewModel
import com.example.moneytracker.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val categories by categoryViewModel.categories.collectAsState()
    var selectedCategory by remember(categories) { 
        mutableStateOf(categories.firstOrNull() ?: TransactionCategory.OTHER) 
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (transactionType == TransactionType.EXPENSE) "Adicionar Despesa" else "Adicionar Receita") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            try {
                                val numericAmount = amount.toDoubleOrNull() ?: 0.0
                                if (numericAmount > 0) {
                                    scope.launch {
                                        viewModel.addTransaction(
                                            type = transactionType,
                                            amount = numericAmount,
                                            category = selectedCategory,
                                            description = description.ifEmpty { selectedCategory.name }
                                        ).collect { result ->
                                            result.onSuccess {
                                                onNavigateBack()
                                            }.onFailure { error ->
                                                errorMessage = "Falha ao salvar transação"
                                                showError = true
                                            }
                                        }
                                    }
                                } else {
                                    errorMessage = "Por favor, insira um valor maior que 0"
                                    showError = true
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Ocorreu um erro"
                                showError = true
                            }
                        },
                        enabled = amount.isNotEmpty() && categories.isNotEmpty()
                    ) {
                        Text("Salvar")
                    }
                }
            )
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Alternador de tipo de transação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { transactionType = TransactionType.EXPENSE },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (transactionType == TransactionType.EXPENSE) 
                                MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (transactionType == TransactionType.EXPENSE) 
                                MaterialTheme.colorScheme.onError 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Despesa")
                    }
                    FilledTonalButton(
                        onClick = { transactionType = TransactionType.INCOME },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (transactionType == TransactionType.INCOME) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (transactionType == TransactionType.INCOME) 
                                MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Receita")
                    }
                }

                // Exibição do valor
                Text(
                    text = "R$ ${if (amount.isEmpty()) "0,00" else amount.replace(".", ",")}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = if (transactionType == TransactionType.INCOME) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.error
                )

                // Campo de descrição
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                // Cartão de categoria selecionada
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .height(56.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { showCategoryDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(getCategoryIconResource(selectedCategory.id)),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(selectedCategory.name)
                        }
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_forward),
                            contentDescription = "Selecionar categoria"
                        )
                    }
                }

                // Teclado numérico
                val buttonModifier = Modifier
                    .size(72.dp)
                    .padding(4.dp)

                // Linha 1: 7-8-9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalculatorButton("7", buttonModifier) { amount += "7" }
                    CalculatorButton("8", buttonModifier) { amount += "8" }
                    CalculatorButton("9", buttonModifier) { amount += "9" }
                }

                // Linha 2: 4-5-6
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalculatorButton("4", buttonModifier) { amount += "4" }
                    CalculatorButton("5", buttonModifier) { amount += "5" }
                    CalculatorButton("6", buttonModifier) { amount += "6" }
                }

                // Linha 3: 1-2-3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalculatorButton("1", buttonModifier) { amount += "1" }
                    CalculatorButton("2", buttonModifier) { amount += "2" }
                    CalculatorButton("3", buttonModifier) { amount += "3" }
                }

                // Linha 4: 0-,-⌫
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CalculatorButton("0", buttonModifier) { amount += "0" }
                    CalculatorButton(",", buttonModifier) { 
                        if (!amount.contains(".")) amount += "." 
                    }
                    CalculatorButton("⌫", buttonModifier, MaterialTheme.colorScheme.error) { 
                        if (amount.isNotEmpty()) {
                            amount = amount.dropLast(1)
                        }
                    }
                }
            }
        }

        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = { Text("Selecionar Categoria") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            items(categories) { category ->
                                CategoryItem(
                                    category = category,
                                    selected = category.id == selectedCategory.id,
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDialog = false
                                    }
                                )
                            }
                        }
                        TextButton(
                            onClick = { 
                                showAddCategoryDialog = true
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painterResource(Res.drawable.ic_add), contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Adicionar Nova Categoria")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Fechar")
                    }
                }
            )
        }

        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Adicionar Nova Categoria") },
                text = {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nome da Categoria") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                scope.launch {
                                    categoryViewModel.addCategory(newCategoryName).collect { result ->
                                        result.onSuccess {
                                            showAddCategoryDialog = false
                                            newCategoryName = ""
                                        }.onFailure { error ->
                                            errorMessage = "Falha ao adicionar categoria"
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
                    TextButton(onClick = { 
                        showAddCategoryDialog = false
                        newCategoryName = ""
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
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { 
                        showError = false
                        errorMessage = ""
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: TransactionCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(getCategoryIconResource(category.id)),
                contentDescription = null,
                tint = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun getCategoryIconResource(categoryId: String): org.jetbrains.compose.resources.DrawableResource {
    return when (categoryId) {
        "food" -> Res.drawable.ic_restaurant
        "bills" -> Res.drawable.ic_account_balance
        "entertainment" -> Res.drawable.ic_favorite
        "transport" -> Res.drawable.ic_directions_car
        "shopping" -> Res.drawable.ic_shopping_cart
        "salary" -> Res.drawable.ic_account_balance
        "health" -> Res.drawable.ic_favorite // Placeholder for LocalHospital
        "education" -> Res.drawable.ic_info // Placeholder for School
        "investment" -> Res.drawable.ic_assessment
        "housing" -> Res.drawable.ic_account_balance // Placeholder for Home
        "clothing" -> Res.drawable.ic_person
        "personal_care" -> Res.drawable.ic_person
        "gifts" -> Res.drawable.ic_favorite
        "pets" -> Res.drawable.ic_pets
        "insurance" -> Res.drawable.ic_account_balance
        "subscriptions" -> Res.drawable.ic_assessment
        else -> Res.drawable.ic_shopping_cart
    }
}
