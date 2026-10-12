package com.superstore.manager.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.data.repository.StockRepository
import com.superstore.manager.util.UnitConverter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockReceivingScreen(
    productRepository: ProductRepository,
    stockRepository: StockRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val products by productRepository.getAllActiveProducts().collectAsState(initial = emptyList())

    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantityInput by remember { mutableStateOf("10") }
    var costPriceInput by remember { mutableStateOf("0.00") }
    var supplierName by remember { mutableStateOf("") }
    var invoiceRef by remember { mutableStateOf("") }

    var expandedProductDropdown by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stock_receiving)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (statusMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        text = statusMessage!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Select Product / پروڈکٹ منتخب کریں", fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(
                expanded = expandedProductDropdown,
                onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.let { "${it.nameEn} ${if (it.nameUr.isNotBlank()) "(${it.nameUr})" else ""}" } ?: "Choose Product",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedProductDropdown,
                    onDismissRequest = { expandedProductDropdown = false }
                ) {
                    products.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.nameEn} ${if (p.nameUr.isNotBlank()) "(${p.nameUr})" else ""}") },
                            onClick = {
                                selectedProduct = p
                                costPriceInput = p.costPrice
                                expandedProductDropdown = false
                            }
                        )
                    }
                }
            }

            if (selectedProduct != null) {
                val p = selectedProduct!!
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Current Stock: ${p.currentStock} ${p.unit.name.lowercase()}", fontWeight = FontWeight.Bold)
                        Text("Current Purchase Cost: Rs. ${p.costPrice}")
                    }
                }

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity Received (${p.unit.name.lowercase()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = costPriceInput,
                    onValueChange = { costPriceInput = it },
                    label = { Text("Purchase Cost Per Unit (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(stringResource(R.string.stock_supplier)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = invoiceRef,
                    onValueChange = { invoiceRef = it },
                    label = { Text("Invoice / Reference No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val qty = UnitConverter.parseBigDecimal(quantityInput)
                            val cost = UnitConverter.parseBigDecimal(costPriceInput)
                            val result = stockRepository.receiveStock(
                                productId = p.id,
                                quantityReceived = qty,
                                purchaseCostPerUnit = cost,
                                supplierName = supplierName,
                                invoiceRef = invoiceRef
                            )
                            result.onSuccess {
                                statusMessage = "Stock received successfully!"
                                selectedProduct = null
                            }.onFailure {
                                statusMessage = "Error: ${it.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Stock Receipt")
                }
            }
        }
    }
}
