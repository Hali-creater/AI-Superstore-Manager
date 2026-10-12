package com.superstore.manager.data.sync

import android.content.Context
import com.google.gson.Gson
import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.local.entity.SaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class BackupPayload(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val products: List<ProductEntity> = emptyList(),
    val sales: List<SaleEntity> = emptyList()
)

interface CloudSyncService {
    suspend fun backupLocalDatabaseToJson(context: Context, db: AppDatabase): Result<File>
    suspend fun restoreDatabaseFromJson(context: Context, db: AppDatabase, backupFile: File): Result<Boolean>
    suspend fun syncPendingQueueWithCloud(context: Context, cloudEndpointUrl: String): Result<Int>
}

class CloudSyncServiceImpl : CloudSyncService {

    override suspend fun backupLocalDatabaseToJson(context: Context, db: AppDatabase): Result<File> = withContext(Dispatchers.IO) {
        try {
            val products = db.productDao().getAllActiveProductsList()

            val payload = BackupPayload(
                exportTimestamp = System.currentTimeMillis(),
                products = products,
                sales = emptyList()
            )

            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "superstore_backup_${System.currentTimeMillis()}.json")
            val writer = FileWriter(backupFile)
            Gson().toJson(payload, writer)
            writer.flush()
            writer.close()

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreDatabaseFromJson(context: Context, db: AppDatabase, backupFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val reader = FileReader(backupFile)
            val payload = Gson().fromJson(reader, BackupPayload::class.java)
            reader.close()

            if (payload == null) {
                return@withContext Result.failure(IllegalArgumentException("Invalid or corrupted backup file."))
            }

            payload.products.forEach { p ->
                db.productDao().insertProduct(p)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingQueueWithCloud(context: Context, cloudEndpointUrl: String): Result<Int> = withContext(Dispatchers.IO) {
        if (cloudEndpointUrl.isBlank()) {
            return@withContext Result.failure(Exception("Cloud URL not configured. Working in local-only mode."))
        }

        try {
            Result.success(0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
