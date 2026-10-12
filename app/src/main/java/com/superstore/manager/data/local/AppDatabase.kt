package com.superstore.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.superstore.manager.data.local.dao.*
import com.superstore.manager.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        ProductAliasEntity::class,
        InventoryTransactionEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        BillImportEntity::class,
        BillImportLineEntity::class,
        SyncQueueEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun productAliasDao(): ProductAliasDao
    abstract fun inventoryTransactionDao(): InventoryTransactionDao
    abstract fun saleDao(): SaleDao
    abstract fun billImportDao(): BillImportDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "superstore_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
