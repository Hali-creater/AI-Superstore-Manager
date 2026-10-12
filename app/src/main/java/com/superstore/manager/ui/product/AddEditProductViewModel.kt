package com.superstore.manager.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.local.entity.UnitType
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEditProductViewModel(
    private val repository: ProductRepository,
    private val productIdToEdit: Long?
) : ViewModel() {

    var nameEn = MutableStateFlow("")
    var nameUr = MutableStateFlow("")
    var brand = MutableStateFlow("")
    var category = MutableStateFlow("")
    var imageUri = MutableStateFlow<String?>(null)
    var selectedUnit = MutableStateFlow(UnitType.PCS)
    var openingStock = MutableStateFlow("0")
    var currentStock = MutableStateFlow("0")
    var costPrice = MutableStateFlow("0.00")
    var sellingPrice = MutableStateFlow("0.00")
    var minThreshold = MutableStateFlow("5")
    var barcode = MutableStateFlow("")
    var sku = MutableStateFlow("")
    var customAliases = MutableStateFlow("")

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (productIdToEdit != null && productIdToEdit > 0L) {
            viewModelScope.launch {
                val existing = repository.getProductById(productIdToEdit)
                if (existing != null) {
                    nameEn.value = existing.nameEn
                    nameUr.value = existing.nameUr
                    brand.value = existing.brand
                    category.value = existing.category
                    imageUri.value = existing.imageUri
                    selectedUnit.value = existing.unit
                    openingStock.value = existing.openingStock
                    currentStock.value = existing.currentStock
                    costPrice.value = existing.costPrice
                    sellingPrice.value = existing.sellingPrice
                    minThreshold.value = existing.minThreshold
                    barcode.value = existing.barcode
                    sku.value = existing.sku
                }
            }
        }
    }

    fun identifyProductWithAi(context: android.content.Context, apiKey: String) {
        val uri = imageUri.value
        if (uri.isNullOrBlank()) {
            _errorMessage.value = "Please select or capture a photo first."
            return
        }

        viewModelScope.launch {
            val aiIdentifier = com.superstore.manager.data.ai.AIProductIdentifierImpl()
            val result = aiIdentifier.identifyProductFromImage(context, uri, apiKey)
            result.onSuccess { suggestion ->
                if (suggestion.nameEn.isNotBlank()) nameEn.value = suggestion.nameEn
                if (suggestion.nameUr.isNotBlank()) nameUr.value = suggestion.nameUr
                if (suggestion.brand.isNotBlank()) brand.value = suggestion.brand
                if (suggestion.category.isNotBlank()) category.value = suggestion.category
                try {
                    selectedUnit.value = UnitType.valueOf(suggestion.suggestedUnit)
                } catch (e: Exception) {
                    selectedUnit.value = UnitType.PCS
                }
            }.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }

    fun saveProduct() {
        val nameEnVal = nameEn.value.trim()
        val nameUrVal = nameUr.value.trim()

        if (nameEnVal.isBlank() && nameUrVal.isBlank()) {
            _errorMessage.value = "Please enter either English or Urdu product name."
            return
        }

        val cost = UnitConverter.parseBigDecimal(costPrice.value)
        val sell = UnitConverter.parseBigDecimal(sellingPrice.value)
        val current = UnitConverter.parseBigDecimal(currentStock.value)
        val opening = UnitConverter.parseBigDecimal(openingStock.value)
        val threshold = UnitConverter.parseBigDecimal(minThreshold.value)

        viewModelScope.launch {
            val product = ProductEntity(
                id = productIdToEdit ?: 0L,
                nameEn = nameEnVal.ifBlank { nameUrVal },
                nameUr = nameUrVal,
                brand = brand.value.trim(),
                category = category.value.trim(),
                imageUri = imageUri.value,
                unit = selectedUnit.value,
                openingStock = UnitConverter.formatQuantity(opening),
                currentStock = UnitConverter.formatQuantity(if (productIdToEdit == null || productIdToEdit <= 0L) opening else current),
                costPrice = cost.setScale(2).toPlainString(),
                sellingPrice = sell.setScale(2).toPlainString(),
                minThreshold = UnitConverter.formatQuantity(threshold),
                barcode = barcode.value.trim(),
                sku = sku.value.trim()
            )

            val aliases = customAliases.value.split(",").map { it.trim() }.filter { it.isNotBlank() }
            repository.saveProduct(product, aliases)
            _isSaved.value = true
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class AddEditProductViewModelFactory(
    private val repository: ProductRepository,
    private val productIdToEdit: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditProductViewModel(repository, productIdToEdit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
