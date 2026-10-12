package com.superstore.manager.data.repository

import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.InventoryTransactionEntity
import com.superstore.manager.data.local.entity.ProductAliasEntity
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.local.entity.TransactionType
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val db: AppDatabase) {

    fun getAllActiveProducts(): Flow<List<ProductEntity>> =
        db.productDao().getAllActiveProducts()

    suspend fun getAllActiveProductsList(): List<ProductEntity> =
        db.productDao().getAllActiveProductsList()

    suspend fun getProductById(id: Long): ProductEntity? =
        db.productDao().getProductById(id)

    fun getAliasesForProduct(productId: Long): Flow<List<ProductAliasEntity>> =
        db.productAliasDao().getAliasesForProduct(productId)

    fun getTransactionsForProduct(productId: Long): Flow<List<InventoryTransactionEntity>> =
        db.inventoryTransactionDao().getTransactionsForProduct(productId)

    suspend fun saveProduct(
        product: ProductEntity,
        initialAliases: List<String> = emptyList()
    ): Long {
        return db.withTransaction {
            val isNew = product.id == 0L
            val productId = db.productDao().insertProduct(product)

            if (isNew) {
                val initialTx = InventoryTransactionEntity(
                    productId = productId,
                    type = TransactionType.INITIAL,
                    quantityChange = product.openingStock,
                    previousStock = "0",
                    newStock = product.openingStock,
                    unitCost = product.costPrice,
                    reason = "Initial opening stock"
                )
                db.inventoryTransactionDao().insertTransaction(initialTx)

                if (product.nameUr.isNotBlank()) {
                    db.productAliasDao().insertAlias(
                        ProductAliasEntity(productId = productId, alias = product.nameUr, language = "ur")
                    )
                }
                if (product.nameEn.isNotBlank()) {
                    db.productAliasDao().insertAlias(
                        ProductAliasEntity(productId = productId, alias = product.nameEn, language = "en")
                    )
                }
            }

            initialAliases.forEach { alias ->
                if (alias.isNotBlank()) {
                    db.productAliasDao().insertAlias(
                        ProductAliasEntity(productId = productId, alias = alias.trim())
                    )
                }
            }

            productId
        }
    }

    suspend fun archiveProduct(productId: Long) {
        val p = db.productDao().getProductById(productId) ?: return
        db.productDao().updateProduct(p.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
    }
}
