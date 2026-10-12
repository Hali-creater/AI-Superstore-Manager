package com.superstore.manager.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.superstore.manager.R
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.util.UnitConverter
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowStockScreen(
    repository: ProductRepository,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (Long) -> Unit
) {
    val products by repository.getAllActiveProducts().collectAsState(initial = emptyList())

    val lowStockList = products.filter { p ->
        val curr = UnitConverter.parseBigDecimal(p.currentStock)
        val thresh = UnitConverter.parseBigDecimal(p.minThreshold)
        curr <= thresh
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_low_stock)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lowStockList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All products have sufficient stock! / تمام پروڈکٹس کا اسٹاک ٹھیک ہے",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "${lowStockList.size} items below threshold / ضروری اسٹاک سے کم اشیاء",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lowStockList, key = { it.id }) { product ->
                        val curr = UnitConverter.parseBigDecimal(product.currentStock)
                        val thresh = UnitConverter.parseBigDecimal(product.minThreshold)
                        val isOutOfStock = curr <= BigDecimal.ZERO

                        Card(
                            onClick = { onNavigateToProductDetail(product.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOutOfStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${product.nameEn} ${if (product.nameUr.isNotBlank()) "(${product.nameUr})" else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Current Stock: ${UnitConverter.formatQuantity(curr)} ${product.unit.name.lowercase()}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOutOfStock) MaterialTheme.colorScheme.onErrorContainer else Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "Min Threshold: ${UnitConverter.formatQuantity(thresh)} ${product.unit.name.lowercase()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isOutOfStock) MaterialTheme.colorScheme.error else Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
