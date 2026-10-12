package com.superstore.manager.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductCatalogueViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = combine(
        repository.getAllActiveProducts(),
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.lowercase().trim()
            list.filter {
                it.nameEn.lowercase().contains(q) ||
                it.nameUr.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.barcode.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun archiveProduct(productId: Long) {
        viewModelScope.launch {
            repository.archiveProduct(productId)
        }
    }
}

class ProductCatalogueViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductCatalogueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductCatalogueViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
