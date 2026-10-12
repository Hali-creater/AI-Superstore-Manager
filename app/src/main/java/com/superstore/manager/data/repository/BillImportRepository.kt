package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.*
import androidx.room.withTransaction
import com.superstore.manager.util.ProductMatcher
import com.superstore.manager.util.UnitConverter
import kotlinx.coroutines.flow.Flow

class BillImportRepository(private val db: AppDatabase) {

    fun getAllBills(): Flow<List<BillImportEntity>> = db.billImportDao().getAllBills()

    suspend fun getBillById(id: Long): BillImportEntity? = db.billImportDao().getBillById(id)

    suspend fun getBillLines(billId: Long): List<BillImportLineEntity> = db.billImportDao().getBillLines(billId)

    suspend fun createPendingBillDraft(imagePath: String, rawLines: List<Pair<String, String>>): Long {
        return db.withTransaction {
            val bill = BillImportEntity(imagePath = imagePath, status = BillStatus.PENDING)
            val billId = db.billImportDao().insertBill(bill)

            val activeProducts = db.productDao().getAllActiveProductsList()
            val aliases = db.productAliasDao().getAllAliases()

            val lineEntities = rawLines.map { (text, qtyStr) ->
                val matched = ProductMatcher.findBestMatch(text, activeProducts, aliases)
                BillImportLineEntity(
                    billId = billId,
                    originalText = text,
                    suggestedProductName = matched?.nameEn ?: text,
                    matchedProductId = matched?.id,
                    quantity = qtyStr.ifBlank { "1" },
                    unit = matched?.unit ?: UnitType.PCS,
                    isResolved = matched != null
                )
            }

            db.billImportDao().insertBillLines(lineEntities)
            billId
        }
    }

    suspend fun updateBillLineMatch(line: BillImportLineEntity, productId: Long?) {
        val matched = if (productId != null) db.productDao().getProductById(productId) else null
        val updatedLine = line.copy(
            matchedProductId = productId,
            suggestedProductName = matched?.nameEn ?: line.originalText,
            isResolved = productId != null
        )
        db.billImportDao().updateBillLine(updatedLine)
    }

    suspend fun confirmAndDeductBillAtomically(billId: Long): Result<Unit> {
        return try {
            db.withTransaction {
                val bill = db.billImportDao().getBillById(billId)
                    ?: throw IllegalArgumentException("Bill not found")

                if (bill.status == BillStatus.CONFIRMED) {
                    throw IllegalStateException("Bill #$billId has already been confirmed and stock deducted.")
                }

                val lines = db.billImportDao().getBillLines(billId)
                val unresolved = lines.filter { !it.isResolved || it.matchedProductId == null }

                if (unresolved.isNotEmpty()) {
                    throw IllegalStateException("${unresolved.size} items are unresolved. Please resolve all product matches before confirming.")
                }

                val cartItems = lines.map { line ->
                    val product = db.productDao().getProductById(line.matchedProductId!!)
                        ?: throw IllegalStateException("Product not found for line: ${line.originalText}")

                    val qty = UnitConverter.parseBigDecimal(line.quantity)
                    val price = UnitConverter.parseBigDecimal(product.sellingPrice)
                    val lineTotal = UnitConverter.calculateSalesValue(qty, price)

                    CartLineItem(
                        product = product,
                        quantity = qty,
                        selectedUnit = line.unit,
                        unitPrice = price,
                        lineTotal = lineTotal
                    )
                }

                val salesRepo = SalesRepository(db)
                val saleResult = salesRepo.recordSaleAtomically(
                    cartItems = cartItems,
                    paymentMethod = "BILL_IMPORT"
                )

                if (saleResult.isFailure) {
                    throw saleResult.exceptionOrNull() ?: Exception("Failed to record bill sale")
                }

                db.billImportDao().updateBill(
                    bill.copy(
                        status = BillStatus.CONFIRMED,
                        confirmedAt = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
