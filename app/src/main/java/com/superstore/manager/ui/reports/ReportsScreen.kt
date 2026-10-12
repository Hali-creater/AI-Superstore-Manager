package com.superstore.manager.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstore.manager.R
import com.superstore.manager.data.repository.ProductRepository
import com.superstore.manager.data.repository.ReportsData
import com.superstore.manager.data.repository.ReportsRepository
import com.superstore.manager.util.CsvExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    reportsRepository: ReportsRepository,
    productRepository: ProductRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val reportsData by reportsRepository.getReportsData().collectAsState(initial = ReportsData())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.reports_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = {
                coroutineScope.launch {
                    val products = productRepository.getAllActiveProductsList()
                    val file = CsvExporter.exportProductsToCsv(context, products)
                    if (file != null) {
                        CsvExporter.shareCsvFile(context, file, "Export Product Catalogue")
                    }
                }
            }) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sales & Profit Summary / منافع اور آمدن",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = stringResource(R.string.reports_sales_revenue), style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rs. ${reportsData.totalSalesRevenue}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.reports_gross_profit), style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rs. ${reportsData.estimatedGrossProfit}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Cost of Goods Sold (COGS): Rs. ${reportsData.totalCogs}", fontSize = 12.sp)
                Text(text = "Total Recorded Transactions: ${reportsData.totalSalesCount}", fontSize = 12.sp)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.reports_stock_value) + " / اسٹاک کی مالیت",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Cost Valuation", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rs. ${reportsData.totalStockCostValuation}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Potential Sales Valuation", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rs. ${reportsData.totalStockSalesValuation}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Active Products in Catalogue: ${reportsData.totalProductsCount}", fontSize = 12.sp)
                Text(text = "Low Stock Items: ${reportsData.lowStockCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
