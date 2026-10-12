package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.*
import androidx.room.withTransaction
import com.superstore.manager.util.UnitConverter
import java.math.BigDecimal
import java.math.RoundingMode

data class CartLineItem(
    val product: ProductEntity,
    val quantity: BigDecimal,
    val selectedUnit: UnitType,
    val unitPrice: BigDecimal,
    val lineTotal: BigDecimal
)

class SalesRepository(private val db: AppDatabase) {

    suspend fun recordSaleAtomically(
        cartItems: List<CartLineItem>,
        discount: BigDecimal = BigDecimal.ZERO,
        paymentMethod: String = "CASH",
        allowNegativeStock: Boolean = false
    ): Result<Long> {
        if (cartItems.isEmpty()) return Result.failure(Exception("Cart is empty"))

        return try {
            val saleId = db.withTransaction {
                val grossTotal = cartItems.sumOf { it.lineTotal }.setScale(2, RoundingMode.HALF_UP)
                val netTotal = (grossTotal - discount).coerceAtLeast(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
                val invoiceNo = "INV-${System.currentTimeMillis()}"

                val saleEntity = SaleEntity(
                    invoiceNo = invoiceNo,
                    totalAmount = grossTotal.toPlainString(),
                    discount = discount.toPlainString(),
                    netAmount = netTotal.toPlainString(),
                    paymentMethod = paymentMethod,
                    timestamp = System.currentTimeMillis()
                )

                val insertedSaleId = db.saleDao().insertSale(saleEntity)
                val saleItems = mutableListOf<SaleItemEntity>()

                for (item in cartItems) {
                    val dbProduct = db.productDao().getProductById(item.product.id)
                        ?: throw IllegalStateException("Product ${item.product.nameEn} no longer exists")

                    val currentStock = UnitConverter.parseBigDecimal(dbProduct.currentStock)
                    val qtyInBaseUnit = UnitConverter.convertQuantity(item.quantity, item.selectedUnit, dbProduct.unit)

                    val newStock = currentStock - qtyInBaseUnit

                    if (newStock < BigDecimal.ZERO && !allowNegativeStock) {
                        throw IllegalStateException("Insufficient stock for ${dbProduct.nameEn}. Available: ${dbProduct.currentStock} ${dbProduct.unit.name.lowercase()}, Requested: ${qtyInBaseUnit.toPlainString()} ${dbProduct.unit.name.lowercase()}")
                    }

                    val saleItem = SaleItemEntity(
                        saleId = insertedSaleId,
                        productId = dbProduct.id,
                        productName = dbProduct.nameEn,
                        quantity = UnitConverter.formatQuantity(item.quantity),
                        unit = item.selectedUnit,
                        unitCostPrice = dbProduct.costPrice,
                        unitSellingPrice = item.unitPrice.toPlainString(),
                        totalPrice = item.lineTotal.toPlainString()
                    )
                    saleItems.add(saleItem)

                    val tx = InventoryTransactionEntity(
                        productId = dbProduct.id,
                        type = TransactionType.SALE,
                        quantityChange = "-${UnitConverter.formatQuantity(qtyInBaseUnit)}",
                        previousStock = dbProduct.currentStock,
                        newStock = UnitConverter.formatQuantity(newStock),
                        unitCost = dbProduct.costPrice,
                        referenceId = invoiceNo,
                        reason = "Manual Sale #$invoiceNo",
                        timestamp = System.currentTimeMillis()
                    )
                    db.inventoryTransactionDao().insertTransaction(tx)

                    db.productDao().updateProduct(
                        dbProduct.copy(
                            currentStock = UnitConverter.formatQuantity(newStock),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }

                db.saleDao().insertSaleItems(saleItems)
                insertedSaleId
            }
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
