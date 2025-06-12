package com.example.moneytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType

@Composable
fun TransactionForm(
    isLoading: Boolean = false,
    error: String? = null,
    onSubmit: (TransactionType, Double, TransactionCategory, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.OTHER) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Selecionar Categoria") },
            text = {
                Column {
                    TransactionCategory.DEFAULT_CATEGORIES.forEach { category ->
                        TextButton(
                            onClick = {
                                selectedCategory = category
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}