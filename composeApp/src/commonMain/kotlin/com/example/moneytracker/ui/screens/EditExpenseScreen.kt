package com.example.moneytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

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
                                    description = description.ifEmpty { selectedCategory.name },
                                    date = selectedDate
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
                // Date button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4A5043) // Olive green
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Text(formatDateForDisplay(selectedDate))
                    }
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
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
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

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    onDateSelected = { 
                        selectedDate = it
                        showDatePicker = false
                    },
                    selectedDate = selectedDate
                )
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
                                            Icons.Default.ShoppingCart,
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

@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit,
    selectedDate: String
) {
    var year by remember { mutableStateOf(selectedDate.substring(0, 4).toInt()) }
    var month by remember { mutableStateOf(selectedDate.substring(5, 7).toInt()) }
    var day by remember { mutableStateOf(selectedDate.substring(8, 10).toInt()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Date") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, null)
                    }
                    Text(year.toString(), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, null)
                    }
                }

                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (month > 1) month-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, null)
                    }
                    Text(
                        getMonthName(month),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { if (month < 12) month++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, null)
                    }
                }

                // Day Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (day > 1) day-- }) {
                        Icon(Icons.Default.KeyboardArrowLeft, null)
                    }
                    Text(day.toString().padStart(2, '0'), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (day < getDaysInMonth(year, month)) day++ }) {
                        Icon(Icons.Default.KeyboardArrowRight, null)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
                    onDateSelected(formattedDate)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

private fun getCurrentDate(): String {
    val now = kotlinx.datetime.Clock.System.now()
    val local = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return String.format("%04d-%02d-%02d", local.year, local.monthNumber, local.dayOfMonth)
}

private fun formatDateForDisplay(date: String): String {
    val year = date.substring(0, 4)
    val month = date.substring(5, 7)
    val day = date.substring(8, 10)
    return "$day/${month}/${year}"
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Janeiro"
        2 -> "Fevereiro"
        3 -> "Março"
        4 -> "Abril"
        5 -> "Maio"
        6 -> "Junho"
        7 -> "Julho"
        8 -> "Agosto"
        9 -> "Setembro"
        10 -> "Outubro"
        11 -> "Novembro"
        12 -> "Dezembro"
        else -> ""
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
} 
