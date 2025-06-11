package com.example.moneytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.Calculator
import com.example.moneytracker.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel
) {
    var expression by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var showError by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val displayAmount = try {
        if (expression.isNotEmpty()) {
            Calculator.evaluate(expression).toString()
        } else "0"
    } catch (e: Exception) {
        if (expression.isNotEmpty()) {
            expression
        } else "0"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (transactionType == TransactionType.EXPENSE) "Edit Expense" else "Edit Income") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            try {
                                val amount = Calculator.evaluate(expression)
                                viewModel.addTransaction(
                                    type = transactionType,
                                    amount = amount,
                                    category = selectedCategory,
                                    description = description.ifEmpty { selectedCategory.name }
                                )
                                onNavigateBack()
                            } catch (e: Exception) {
                                showError = true
                            }
                        },
                        enabled = expression.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        },
        containerColor = Color(0xFFFAF9F6) // Light cream background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Transaction Type Toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                SegmentedButton(
                    selected = transactionType == TransactionType.EXPENSE,
                    onClick = { transactionType = TransactionType.EXPENSE },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Color(0xFF4A5043),
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.LightGray,
                        inactiveContentColor = Color.Black
                    )
                ) {
                    Text("Expense")
                }
                SegmentedButton(
                    selected = transactionType == TransactionType.INCOME,
                    onClick = { transactionType = TransactionType.INCOME },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Color(0xFF4A5043),
                        activeContentColor = Color.White,
                        inactiveContainerColor = Color.LightGray,
                        inactiveContentColor = Color.Black
                    )
                ) {
                    Text("Income")
                }
            }

            // Amount display with color based on transaction type
            Text(
                text = if (transactionType == TransactionType.INCOME) "+$displayAmount" else "-$displayAmount",
                fontSize = 48.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(vertical = 16.dp),
                color = if (transactionType == TransactionType.INCOME) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.error
            )

            // Description TextField
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A5043),
                    focusedLabelColor = Color(0xFF4A5043)
                )
            )

            // Category and Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Today button
                OutlinedButton(
                    onClick = { /* Date picker logic */ },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A5043) // Olive green
                    )
                ) {
                    Text("Today")
                }

                // Category button
                OutlinedButton(
                    onClick = { showCategoryDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A5043) // Olive green
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedCategory) {
                                TransactionCategory.FOOD -> Icons.Default.ShoppingCart
                                TransactionCategory.BILLS -> Icons.Default.ShoppingCart
                                TransactionCategory.ENTERTAINMENT -> Icons.Default.ShoppingCart
                                TransactionCategory.TRANSPORT -> Icons.Default.ShoppingCart
                                TransactionCategory.SHOPPING -> Icons.Default.ShoppingCart
                                TransactionCategory.SALARY -> Icons.Default.ShoppingCart
                                TransactionCategory.OTHER -> Icons.Default.ShoppingCart
                            },
                            contentDescription = null
                        )
                        Text(selectedCategory.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calculator grid
            val buttonModifier = Modifier
                .size(72.dp)
                .padding(4.dp)

            val buttonColor = Color(0xFF4A5043) // Olive green
            val numberButtonColor = Color(0xFFE8E8E8) // Light gray

            // Row 1: Operations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorButton("(", buttonModifier, buttonColor) { expression += "(" }
                CalculatorButton(")", buttonModifier, buttonColor) { expression += ")" }
                CalculatorButton("-", buttonModifier, buttonColor) { expression += "-" }
                CalculatorButton("+", buttonModifier, buttonColor) { expression += "+" }
            }

            // Row 2: 7-8-9-÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorButton("7", buttonModifier, numberButtonColor) { expression += "7" }
                CalculatorButton("8", buttonModifier, numberButtonColor) { expression += "8" }
                CalculatorButton("9", buttonModifier, numberButtonColor) { expression += "9" }
                CalculatorButton("÷", buttonModifier, buttonColor) { expression += "/" }
            }

            // Row 3: 4-5-6-×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorButton("4", buttonModifier, numberButtonColor) { expression += "4" }
                CalculatorButton("5", buttonModifier, numberButtonColor) { expression += "5" }
                CalculatorButton("6", buttonModifier, numberButtonColor) { expression += "6" }
                CalculatorButton("×", buttonModifier, buttonColor) { expression += "*" }
            }

            // Row 4: 1-2-3-⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorButton("1", buttonModifier, numberButtonColor) { expression += "1" }
                CalculatorButton("2", buttonModifier, numberButtonColor) { expression += "2" }
                CalculatorButton("3", buttonModifier, numberButtonColor) { expression += "3" }
                CalculatorButton("⌫", buttonModifier, Color(0xFFC41E3A)) { // Red color for delete
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                }
            }

            // Row 5: 000-.-0-=
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorButton("000", buttonModifier, numberButtonColor) { expression += "000" }
                CalculatorButton(".", buttonModifier, numberButtonColor) { expression += "." }
                CalculatorButton("0", buttonModifier, numberButtonColor) { expression += "0" }
                CalculatorButton("=", buttonModifier, Color(0xFFCCE5A9)) { // Light green for equals
                    try {
                        val result = Calculator.evaluate(expression)
                        expression = result.toString()
                    } catch (e: Exception) {
                        showError = true
                    }
                }
            }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("Select Category") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TransactionCategory.values().forEach { category ->
                                Surface(
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDialog = false
                                    },
                                    color = if (selectedCategory == category) 
                                        Color(0xFF4A5043).copy(alpha = 0.1f)
                                    else Color.Transparent,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (category) {
                                                TransactionCategory.FOOD -> Icons.Default.ShoppingCart
                                                TransactionCategory.BILLS -> Icons.Default.ShoppingCart
                                                TransactionCategory.ENTERTAINMENT -> Icons.Default.ShoppingCart
                                                TransactionCategory.TRANSPORT -> Icons.Default.ShoppingCart
                                                TransactionCategory.SHOPPING -> Icons.Default.ShoppingCart
                                                TransactionCategory.SALARY -> Icons.Default.ShoppingCart
                                                TransactionCategory.OTHER -> Icons.Default.ShoppingCart
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF4A5043)
                                        )
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showError) {
                AlertDialog(
                    onDismissRequest = { showError = false },
                    title = { Text("Invalid Expression") },
                    text = { Text("Please check your input and try again.") },
                    confirmButton = {
                        TextButton(onClick = { showError = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = backgroundColor,
            contentColor = if (backgroundColor == Color(0xFF4A5043)) Color.White else Color.Black
        )
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
} 
