package com.superstore.manager.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.repository.CartLineItem
import com.superstore.manager.util.UnitConverter
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSalesScreen(
    viewModel: SalesViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val netTotal by viewModel.netTotal.collectAsState()
    val discount by viewModel.discount.collectAsState()

    val saleSuccess by viewModel.saleSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showCartDialog by remember { mutableStateOf(false) }

    if (saleSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Sale Completed Successfully! / فروخت مکمل") },
            text = { Text("Stock has been deducted and transaction recorded atomically.") },
            confirmButton = {
                Button(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text("Error / غلطی") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Cart (${cart.sumOf { it.quantity.toInt() }} items)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Total: Rs. $netTotal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCartDialog = true },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Cart / Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sales_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text(stringResource(R.string.action_search) + " (English / اردو)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    SalesProductRow(
                        product = product,
                        onAddToCart = { qty -> viewModel.addToCart(product, qty) }
                    )
                }
            }
        }
    }

    if (showCartDialog) {
        CartCheckoutBottomSheet(
            cart = cart,
            discount = discount,
            totalAmount = totalAmount,
            netTotal = netTotal,
            onDiscountChange = { viewModel.setDiscount(it) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onConfirmSale = {
                showCartDialog = false
                viewModel.completeSale()
            },
            onDismiss = { showCartDialog = false }
        )
    }
}

@Composable
fun SalesProductRow(
    product: ProductEntity,
    onAddToCart: (BigDecimal) -> Unit
) {
    var inputQty by remember { mutableStateOf("1") }

    val currentStock = UnitConverter.parseBigDecimal(product.currentStock)
    val isOutOfStock = currentStock <= BigDecimal.ZERO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = product.nameEn, fontWeight = FontWeight.Bold)
                    if (product.nameUr.isNotBlank()) {
                        Text(text = product.nameUr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(text = "Rs. ${product.sellingPrice} / ${product.unit.name.lowercase()}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Stock: ${UnitConverter.formatQuantity(currentStock)} ${product.unit.name.lowercase()}",
                    fontSize = 12.sp,
                    color = if (isOutOfStock) MaterialTheme.colorScheme.error else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputQty,
                onValueChange = { inputQty = it },
                modifier = Modifier.width(70.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val qty = UnitConverter.parseBigDecimal(inputQty, BigDecimal.ONE)
                    onAddToCart(qty)
                },
                enabled = !isOutOfStock
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutBottomSheet(
    cart: List<CartLineItem>,
    discount: String,
    totalAmount: BigDecimal,
    netTotal: BigDecimal,
    onDiscountChange: (String) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onConfirmSale: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sales_cart),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cart, key = { it.product.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.product.nameEn, fontWeight = FontWeight.Bold)
                            Text(text = "Rs. ${item.unitPrice} x ${UnitConverter.formatQuantity(item.quantity)} = Rs. ${item.lineTotal}", fontSize = 12.sp)
                        }

                        IconButton(onClick = { onRemoveItem(item.product.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.sales_total) + ": Rs. $totalAmount", style = MaterialTheme.typography.bodyLarge)

                OutlinedTextField(
                    value = discount,
                    onValueChange = onDiscountChange,
                    label = { Text(stringResource(R.string.sales_discount)) },
                    modifier = Modifier.width(120.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Text(
                text = "Net Amount: Rs. $netTotal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onConfirmSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(R.string.sales_complete), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
