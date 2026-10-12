package com.superstore.manager.ui.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.superstore.manager.R
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.util.UnitConverter
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogueScreen(
    viewModel: ProductCatalogueViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToProductDetail: (Long) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.product_add))
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
                text = stringResource(R.string.product_catalogue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text(stringResource(R.string.action_search) + " (English / اردو)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No products found. Tap '+' to add a product.\nکوئی پروڈکٹ نہیں مل سکی۔",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onClick = { onNavigateToProductDetail(product.id) },
                            onEditClick = { onNavigateToEditProduct(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val currentStock = UnitConverter.parseBigDecimal(product.currentStock)
    val minThreshold = UnitConverter.parseBigDecimal(product.minThreshold)
    val costPrice = UnitConverter.parseBigDecimal(product.costPrice)
    val sellingPrice = UnitConverter.parseBigDecimal(product.sellingPrice)

    val isLowStock = currentStock <= minThreshold && currentStock > BigDecimal.ZERO
    val isOutOfStock = currentStock <= BigDecimal.ZERO

    val totalStockCost = UnitConverter.calculateTotalCost(currentStock, costPrice)
    val totalSalesValue = UnitConverter.calculateSalesValue(currentStock, sellingPrice)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!product.imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = product.nameEn,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.nameUr.isNotBlank()) {
                        Text(
                            text = product.nameUr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${product.brand} • ${product.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Price: Rs. ${product.sellingPrice} / ${product.unit.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val stockColor = when {
                        isOutOfStock -> MaterialTheme.colorScheme.error
                        isLowStock -> Color(0xFFE65100)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Text(
                        text = "Stock: ${UnitConverter.formatQuantity(currentStock)} ${product.unit.name.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = stockColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Valuation: Cost Rs. $totalStockCost | Potential Rs. $totalSalesValue",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
            }
        }
    }
}
