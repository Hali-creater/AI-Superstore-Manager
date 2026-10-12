package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

data class DashboardSummary(
    val activeProductsCount: Int = 0,
    val todaySalesRevenue: BigDecimal = BigDecimal.ZERO,
    val todayCompletedSales: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val estimatedGrossProfit: BigDecimal = BigDecimal.ZERO,
    val pendingBillsCount: Int = 0,
    val lastBackupTime: String = "Never",
    val syncStatus: String = "Local Only"
)

class DashboardRepository(private val db: AppDatabase) {

    fun getDashboardSummary(): Flow<DashboardSummary> {
        return combine(
            db.productDao().getAllActiveProducts(),
            db.saleDao().getAllSales(),
            db.billImportDao().getPendingBillsCount()
        ) { products, sales, pendingBills ->

            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todaySales = sales.filter { it.timestamp >= startOfToday }
            val todayRevenue = todaySales.sumOf { BigDecimal(it.netAmount) }
            val completedSalesCount = todaySales.size

            var lowStock = 0
            var outOfStock = 0

            products.forEach { product ->
                val current = BigDecimal(product.currentStock)
                val threshold = BigDecimal(product.minThreshold)
                if (current <= BigDecimal.ZERO) {
                    outOfStock++
                } else if (current <= threshold) {
                    lowStock++
                }
            }

            var estimatedProfit = BigDecimal.ZERO
            todaySales.forEach { sale ->
                val total = BigDecimal(sale.netAmount)
                estimatedProfit = estimatedProfit.add(total.multiply(BigDecimal("0.12")))
            }

            DashboardSummary(
                activeProductsCount = products.size,
                todaySalesRevenue = todayRevenue.setScale(2, RoundingMode.HALF_UP),
                todayCompletedSales = completedSalesCount,
                lowStockCount = lowStock,
                outOfStockCount = outOfStock,
                estimatedGrossProfit = estimatedProfit.setScale(2, RoundingMode.HALF_UP),
                pendingBillsCount = pendingBills,
                lastBackupTime = "Local Only",
                syncStatus = "Offline Ready"
            )
        }
    }
}
