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
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class CategoryViewModel(
    private val repository: CategoryRepository = RepositoryProvider.provideCategoryRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var userId: String? = null
    private val _categories = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categories: StateFlow<List<TransactionCategory>> = _categories
    private val _error = MutableStateFlow<String?>(null) // Adicionado para feedback de erro
    val error: StateFlow<String?> = _error

    fun setUserId(id: String) {
        userId = id
        loadCategories()
    }

    private fun loadCategories() {
        userId?.let { uid ->
            scope.launch {
                try {
                    _error.value = null
                    // No iOS, com IosCategoryRepositoryDummy, 'categories' será uma lista vazia (de emptyFlow).
                    // Similar ao TransactionViewModel, isso é "seguro", mas não um erro explícito.
                    repository.getCategoriesFlow(uid).collect { categories ->
                        _categories.value = categories
                    }
                } catch (e: Exception) { // Embora emptyFlow não deva lançar aqui.
                    println("Error in CategoryViewModel.loadCategories: ${e.message}")
                    _error.value = e.message ?: "Erro ao carregar categorias"
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
        // A dummy já retorna Flow<Result.failure>, então o chamador na UI deve tratar.
        // Adicionar try-catch aqui não faria muito sentido para a exceção da dummy,
        // pois ela está encapsulada no Result que o Flow emite.
        if (userId == null) return MutableStateFlow(Result.failure(IllegalStateException("User not logged in")))
        return flow { repository.addCategory(userId!!, category) }
    }

    fun deleteCategory(categoryId: String) {
        userId?.let { uid ->
            scope.launch {
                try {
                    _error.value = null
                    repository.deleteCategory(uid, categoryId)
                    // Após deletar, idealmente recarregar categorias ou remover da lista local.
                    // No iOS, a dummy lançará exceção, então o catch abaixo tratará.
                    loadCategories() // Recarrega para refletir a exclusão (se bem-sucedida)
                } catch (e: Exception) {
                    println("Error in CategoryViewModel.deleteCategory: ${e.message}")
                    _error.value = e.message ?: "Erro ao deletar categoria"
                }
            }
        }
    }
}