package com.superstore.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.superstore.manager.R

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Products : Screen("products", R.string.nav_products, Icons.Default.Inventory)
    object Sales : Screen("sales", R.string.nav_sales, Icons.Default.PointOfSale)
    object Bills : Screen("bills", R.string.nav_bills, Icons.AutoMirrored.Filled.ReceiptLong)
    object Reports : Screen("reports", R.string.nav_reports, Icons.Default.Analytics)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    object AddEditProduct : Screen("product_add_edit?productId={productId}", R.string.product_add, Icons.Default.Inventory) {
        fun createRoute(productId: Long? = null) = if (productId != null) "product_add_edit?productId=$productId" else "product_add_edit"
    }
    object ProductDetail : Screen("product_detail/{productId}", R.string.product_detail, Icons.Default.Inventory) {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object LowStockList : Screen("low_stock_list", R.string.dashboard_low_stock, Icons.Default.Inventory)
    object BillReview : Screen("bill_review/{billId}", R.string.bill_review_title, Icons.AutoMirrored.Filled.ReceiptLong) {
        fun createRoute(billId: Long) = "bill_review/$billId"
    }
    object StockReceiving : Screen("stock_receiving", R.string.stock_receiving, Icons.Default.Inventory)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Products,
    Screen.Sales,
    Screen.Bills,
    Screen.Reports,
    Screen.Settings
)
