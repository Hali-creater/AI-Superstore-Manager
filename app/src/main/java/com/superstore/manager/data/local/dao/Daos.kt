package com.superstore.manager.data.local.dao

import androidx.room.*
import com.superstore.manager.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY nameEn ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0")
    fun getActiveProductsCount(): Flow<Int>

    @Query("SELECT * FROM products WHERE isArchived = 0")
    suspend fun getAllActiveProductsList(): List<ProductEntity>
}

@Dao
interface ProductAliasDao {
    @Query("SELECT * FROM product_aliases WHERE productId = :productId")
    fun getAliasesForProduct(productId: Long): Flow<List<ProductAliasEntity>>

    @Query("SELECT * FROM product_aliases")
    suspend fun getAllAliases(): List<ProductAliasEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: ProductAliasEntity): Long

    @Query("DELETE FROM product_aliases WHERE id = :id")
    suspend fun deleteAlias(id: Long)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSalesSince(startTime: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesList(): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity>
}

@Dao
interface InventoryTransactionDao {
    @Query("SELECT * FROM inventory_transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransactionEntity): Long
}

@Dao
interface BillImportDao {
    @Query("SELECT * FROM bill_imports ORDER BY createdAt DESC")
    fun getAllBills(): Flow<List<BillImportEntity>>

    @Query("SELECT COUNT(*) FROM bill_imports WHERE status = 'PENDING'")
    fun getPendingBillsCount(): Flow<Int>

    @Query("SELECT * FROM bill_imports WHERE id = :id")
    suspend fun getBillById(id: Long): BillImportEntity?

    @Query("SELECT * FROM bill_import_lines WHERE billId = :billId")
    suspend fun getBillLines(billId: Long): List<BillImportLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillImportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillLines(lines: List<BillImportLineEntity>)

    @Update
    suspend fun updateBill(bill: BillImportEntity)

    @Update
    suspend fun updateBillLine(line: BillImportLineEntity)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Query("SELECT value FROM app_settings WHERE key = :key")
    fun observeValue(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(setting: AppSettingsEntity)
}
