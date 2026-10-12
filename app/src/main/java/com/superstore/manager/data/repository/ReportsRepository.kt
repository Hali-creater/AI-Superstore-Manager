package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.util.UnitConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.math.RoundingMode

data class ReportsData(
    val totalSalesRevenue: BigDecimal = BigDecimal.ZERO,
    val totalCogs: BigDecimal = BigDecimal.ZERO,
    val estimatedGrossProfit: BigDecimal = BigDecimal.ZERO,
    val totalStockCostValuation: BigDecimal = BigDecimal.ZERO,
    val totalStockSalesValuation: BigDecimal = BigDecimal.ZERO,
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalSalesCount: Int = 0
)

class ReportsRepository(private val db: AppDatabase) {

    fun getReportsData(): Flow<ReportsData> {
        return combine(
            db.productDao().getAllActiveProducts(),
            db.saleDao().getAllSales()
        ) { products, sales ->
            var stockCostValuation = BigDecimal.ZERO
            var stockSalesValuation = BigDecimal.ZERO
            var lowStockCount = 0

            products.forEach { p ->
                val current = UnitConverter.parseBigDecimal(p.currentStock)
                val cost = UnitConverter.parseBigDecimal(p.costPrice)
                val sell = UnitConverter.parseBigDecimal(p.sellingPrice)
                val thresh = UnitConverter.parseBigDecimal(p.minThreshold)

                stockCostValuation = stockCostValuation.add(UnitConverter.calculateTotalCost(current, cost))
                stockSalesValuation = stockSalesValuation.add(UnitConverter.calculateSalesValue(current, sell))

                if (current <= thresh) {
                    lowStockCount++
                }
            }

            val salesRevenue = sales.sumOf { BigDecimal(it.netAmount) }.setScale(2, RoundingMode.HALF_UP)
            val cogs = salesRevenue.multiply(BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP)
            val grossProfit = (salesRevenue - cogs).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

            ReportsData(
                totalSalesRevenue = salesRevenue,
                totalCogs = cogs,
                estimatedGrossProfit = grossProfit,
                totalStockCostValuation = stockCostValuation.setScale(2, RoundingMode.HALF_UP),
                totalStockSalesValuation = stockSalesValuation.setScale(2, RoundingMode.HALF_UP),
                totalProductsCount = products.size,
                lowStockCount = lowStockCount,
                totalSalesCount = sales.size
            )
        }
    }
}
