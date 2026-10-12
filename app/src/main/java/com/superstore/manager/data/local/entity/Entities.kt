package com.superstore.manager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UnitType {
    PCS, PKT, KG, G, L, ML
}

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["nameEn"]),
        Index(value = ["nameUr"]),
        Index(value = ["barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameUr: String = "",
    val brand: String = "",
    val category: String = "",
    val imageUri: String? = null,
    val unit: UnitType = UnitType.PCS,
    val currentStock: String,
    val openingStock: String = "0",
    val costPrice: String,
    val sellingPrice: String,
    val minThreshold: String = "5",
    val barcode: String = "",
    val sku: String = "",
    val expiryDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(
    tableName = "product_aliases",
    indices = [Index(value = ["productId"]), Index(value = ["alias"])]
)
data class ProductAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val alias: String,
    val language: String = "ur"
)

enum class TransactionType {
    SALE, RECEIVING, ADJUSTMENT_ADD, ADJUSTMENT_SUB, RETURN_CUSTOMER, RETURN_SUPPLIER, INITIAL
}

@Entity(
    tableName = "inventory_transactions",
    indices = [Index(value = ["productId"]), Index(value = ["timestamp"])]
)
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: TransactionType,
    val quantityChange: String,
    val previousStock: String,
    val newStock: String,
    val unitCost: String,
    val referenceId: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val totalAmount: String,
    val discount: String = "0",
    val netAmount: String,
    val paymentMethod: String = "CASH",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(
    tableName = "sale_items",
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: String,
    val unit: UnitType,
    val unitCostPrice: String,
    val unitSellingPrice: String,
    val totalPrice: String
)

enum class BillStatus {
    PENDING, CONFIRMED, CANCELLED
}

@Entity(tableName = "bill_imports")
data class BillImportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imagePath: String,
    val status: BillStatus = BillStatus.PENDING,
    val rawAiResponse: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null
)

@Entity(
    tableName = "bill_import_lines",
    indices = [Index(value = ["billId"])]
)
data class BillImportLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val originalText: String,
    val suggestedProductName: String,
    val matchedProductId: Long? = null,
    val quantity: String,
    val unit: UnitType = UnitType.PCS,
    val confidence: Float = 0f,
    val warning: String = "",
    val isResolved: Boolean = false
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
