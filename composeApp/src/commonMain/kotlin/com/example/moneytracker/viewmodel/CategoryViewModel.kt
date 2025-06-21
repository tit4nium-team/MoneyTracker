package com.example.moneytracker.viewmodel

import com.example.moneytracker.data.CategoryRepository
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock

class CategoryViewModel(
    private val repository: CategoryRepository = RepositoryProvider.provideCategoryRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var userId: String? = null
    private val _categories = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categories: StateFlow<List<TransactionCategory>> = _categories

    fun setUserId(id: String) {
        userId = id
        loadCategories()
    }

    private fun loadCategories() {
        userId?.let { uid ->
            scope.launch {
                repository.getCategoriesFlow(uid).collect { categories ->
                    _categories.value = categories
                }
            }
        }
    }

    fun addCategory(name: String, icon: String = "shopping_cart"): Flow<Result<Unit>> {
        val category = TransactionCategory(
            id = "temp_${Clock.System.now().toEpochMilliseconds()}",
            name = name,
            icon = icon,
            isCustom = true
        )
        return repository.addCategory(userId ?: return MutableStateFlow(Result.failure(IllegalStateException("User not logged in"))), category)
    }

    fun deleteCategory(categoryId: String) {
        userId?.let { uid ->
            scope.launch {
                repository.deleteCategory(uid, categoryId)
            }
        }
    }
} 