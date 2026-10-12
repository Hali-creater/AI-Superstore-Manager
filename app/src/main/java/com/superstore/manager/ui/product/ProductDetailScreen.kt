package com.superstore.manager.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.InventoryTransactionEntity
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    repository: ProductRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    val aliases by repository.getAliasesForProduct(productId).collectAsState(initial = emptyList())
    val transactions by repository.getTransactionsForProduct(productId).collectAsState(initial = emptyList())

    var newAliasText by remember { mutableStateOf("") }

    LaunchedEffect(productId) {
        product = repository.getProductById(productId)
    }

    val currentProduct = product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentProduct?.nameEn ?: stringResource(R.string.product_detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (currentProduct != null) {
                        IconButton(onClick = { onNavigateToEdit(currentProduct.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (currentProduct == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val stock = UnitConverter.parseBigDecimal(currentProduct.currentStock)
            val cost = UnitConverter.parseBigDecimal(currentProduct.costPrice)
            val sell = UnitConverter.parseBigDecimal(currentProduct.sellingPrice)

            val totalCost = UnitConverter.calculateTotalCost(stock, cost)
            val totalSales = UnitConverter.calculateSalesValue(stock, sell)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentProduct.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentProduct.nameUr.isNotBlank()) {
                                Text(
                                    text = currentProduct.nameUr,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Brand: ${currentProduct.brand.ifBlank { "N/A" }} | Category: ${currentProduct.category.ifBlank { "General" }}")
                        Text(text = "Current Stock: ${UnitConverter.formatQuantity(stock)} ${currentProduct.unit.name.lowercase()}", fontWeight = FontWeight.Bold)
                        Text(text = "Purchase Cost: Rs. ${currentProduct.costPrice} / ${currentProduct.unit.name.lowercase()}")
                        Text(text = "Selling Price: Rs. ${currentProduct.sellingPrice} / ${currentProduct.unit.name.lowercase()}")
                        Text(text = "Stock Valuation: Cost Rs. $totalCost | Potential Sales Rs. $totalSales", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.product_aliases),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            aliases.forEach { alias ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(alias.alias) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newAliasText,
                                onValueChange = { newAliasText = it },
                                label = { Text("Add Alias / Nickname") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                if (newAliasText.isNotBlank()) {
                                    newAliasText = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.product_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (transactions.isEmpty()) {
                    Text("No inventory transaction history yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            TransactionItemRow(tx = tx, unitName = currentProduct.unit.name.lowercase())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(tx: InventoryTransactionEntity, unitName: String) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(tx.timestamp))

    val qty = UnitConverter.parseBigDecimal(tx.quantityChange)
    val isPositive = qty >= java.math.BigDecimal.ZERO

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${tx.type.name} - ${tx.reason.ifBlank { "System Log" }}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else ""}${tx.quantityChange} $unitName",
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "New Stock: ${tx.newStock}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
