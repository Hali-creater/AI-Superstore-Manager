package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.InventoryTransactionEntity
import com.superstore.manager.data.local.entity.TransactionType
import androidx.room.withTransaction
import com.superstore.manager.util.UnitConverter
import java.math.BigDecimal

class StockRepository(private val db: AppDatabase) {

    suspend fun receiveStock(
        productId: Long,
        quantityReceived: BigDecimal,
        purchaseCostPerUnit: BigDecimal,
        supplierName: String = "",
        invoiceRef: String = ""
    ): Result<Unit> {
        return try {
            db.withTransaction {
                val product = db.productDao().getProductById(productId)
                    ?: throw IllegalArgumentException("Product not found")

                val currentStock = UnitConverter.parseBigDecimal(product.currentStock)
                val newStock = currentStock + quantityReceived

                val tx = InventoryTransactionEntity(
                    productId = productId,
                    type = TransactionType.RECEIVING,
                    quantityChange = "+${UnitConverter.formatQuantity(quantityReceived)}",
                    previousStock = product.currentStock,
                    newStock = UnitConverter.formatQuantity(newStock),
                    unitCost = purchaseCostPerUnit.setScale(2).toPlainString(),
                    referenceId = invoiceRef,
                    reason = if (supplierName.isNotBlank()) "Supplier: $supplierName" else "Stock Receiving"
                )
                db.inventoryTransactionDao().insertTransaction(tx)

                db.productDao().updateProduct(
                    product.copy(
                        currentStock = UnitConverter.formatQuantity(newStock),
                        costPrice = purchaseCostPerUnit.setScale(2).toPlainString(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adjustStock(
        productId: Long,
        actualCount: BigDecimal,
        reason: String
    ): Result<Unit> {
        return try {
            db.withTransaction {
                val product = db.productDao().getProductById(productId)
                    ?: throw IllegalArgumentException("Product not found")

                val currentStock = UnitConverter.parseBigDecimal(product.currentStock)
                val diff = actualCount - currentStock

                val type = if (diff >= BigDecimal.ZERO) TransactionType.ADJUSTMENT_ADD else TransactionType.ADJUSTMENT_SUB
                val formattedDiff = if (diff >= BigDecimal.ZERO) "+${UnitConverter.formatQuantity(diff)}" else UnitConverter.formatQuantity(diff)

                val tx = InventoryTransactionEntity(
                    productId = productId,
                    type = type,
                    quantityChange = formattedDiff,
                    previousStock = product.currentStock,
                    newStock = UnitConverter.formatQuantity(actualCount),
                    unitCost = product.costPrice,
                    reason = reason.ifBlank { "Physical Stock Audit Count" }
                )
                db.inventoryTransactionDao().insertTransaction(tx)

                db.productDao().updateProduct(
                    product.copy(
                        currentStock = UnitConverter.formatQuantity(actualCount),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
