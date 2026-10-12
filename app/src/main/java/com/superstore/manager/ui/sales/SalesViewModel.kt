package com.superstore.manager.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.CartLineItem
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.data.repository.SalesRepository
import com.superstore.manager.util.UnitConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class SalesViewModel(
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cart = MutableStateFlow<List<CartLineItem>>(emptyList())
    val cart: StateFlow<List<CartLineItem>> = _cart.asStateFlow()

    private val _discount = MutableStateFlow("0")
    val discount: StateFlow<String> = _discount.asStateFlow()

    private val _saleSuccess = MutableStateFlow(false)
    val saleSuccess: StateFlow<Boolean> = _saleSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = combine(
        productRepository.getAllActiveProducts(),
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

    val totalAmount: StateFlow<BigDecimal> = _cart.map { items ->
        items.sumOf { it.lineTotal }.setScale(2, RoundingMode.HALF_UP)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val netTotal: StateFlow<BigDecimal> = combine(totalAmount, _discount) { total, discStr ->
        val disc = UnitConverter.parseBigDecimal(discStr)
        (total - disc).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDiscount(disc: String) {
        _discount.value = disc
    }

    fun addToCart(product: ProductEntity, quantity: BigDecimal = BigDecimal.ONE) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }

        val sellingPrice = UnitConverter.parseBigDecimal(product.sellingPrice)

        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            val lineTotal = UnitConverter.calculateSalesValue(newQty, sellingPrice)
            currentList[index] = existing.copy(quantity = newQty, lineTotal = lineTotal)
        } else {
            val lineTotal = UnitConverter.calculateSalesValue(quantity, sellingPrice)
            currentList.add(
                CartLineItem(
                    product = product,
                    quantity = quantity,
                    selectedUnit = product.unit,
                    unitPrice = sellingPrice,
                    lineTotal = lineTotal
                )
            )
        }
        _cart.value = currentList
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: BigDecimal) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }

        if (index >= 0) {
            if (newQuantity <= BigDecimal.ZERO) {
                currentList.removeAt(index)
            } else {
                val existing = currentList[index]
                val lineTotal = UnitConverter.calculateSalesValue(newQuantity, existing.unitPrice)
                currentList[index] = existing.copy(quantity = newQuantity, lineTotal = lineTotal)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun completeSale() {
        viewModelScope.launch {
            val disc = UnitConverter.parseBigDecimal(_discount.value)
            val result = salesRepository.recordSaleAtomically(
                cartItems = _cart.value,
                discount = disc
            )
            result.onSuccess {
                _cart.value = emptyList()
                _discount.value = "0"
                _saleSuccess.value = true
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Failed to record sale"
            }
        }
    }

    fun clearMessages() {
        _saleSuccess.value = false
        _errorMessage.value = null
    }
}

class SalesViewModelFactory(
    private val productRepository: ProductRepository,
    private val salesRepository: SalesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(productRepository, salesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
