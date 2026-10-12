package com.superstore.manager.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superstore.manager.R

@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    onNavigateToSales: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToLowStock: () -> Unit,
    onNavigateToStockReceiving: () -> Unit
) {
    val summary by viewModel.summary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.dashboard_sync_status) + ": " + summary.syncStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Text(
            text = stringResource(R.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_action_new_sale),
                icon = Icons.Default.AddShoppingCart,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = onNavigateToSales
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_action_import_bill),
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = onNavigateToBills
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_action_add_product),
                icon = Icons.Default.Add,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onNavigateToAddProduct
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_action_receive_stock),
                icon = Icons.Default.MoveToInbox,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onNavigateToStockReceiving
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Store Summary / صورتحال",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_today_sales),
                value = "Rs. ${summary.todaySalesRevenue}",
                subtitle = "${summary.todayCompletedSales} Sales Today",
                icon = Icons.Default.AttachMoney,
                iconColor = MaterialTheme.colorScheme.primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_gross_profit),
                value = "Rs. ${summary.estimatedGrossProfit}",
                subtitle = "Est. Margin",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = Color(0xFF2E7D32)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_active_products),
                value = "${summary.activeProductsCount}",
                subtitle = "In Catalogue",
                icon = Icons.Default.Category,
                iconColor = MaterialTheme.colorScheme.secondary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_pending_bills),
                value = "${summary.pendingBillsCount}",
                subtitle = "Awaiting Review",
                icon = Icons.Default.PendingActions,
                iconColor = MaterialTheme.colorScheme.tertiary,
                onClick = onNavigateToBills
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_low_stock),
                value = "${summary.lowStockCount}",
                subtitle = "Tap to view list",
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFE65100),
                onClick = onNavigateToLowStock
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_out_of_stock),
                value = "${summary.outOfStockCount}",
                subtitle = "Empty Stock",
                icon = Icons.Default.ErrorOutline,
                iconColor = MaterialTheme.colorScheme.error,
                onClick = onNavigateToLowStock
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
