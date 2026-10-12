package com.superstore.manager.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.superstore.manager.SuperstoreApp

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as SuperstoreApp
            val syncService = CloudSyncServiceImpl()
            val url = app.database.appSettingsDao().getValue("cloud_endpoint_url") ?: ""

            val result = syncService.syncPendingQueueWithCloud(applicationContext, url)
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
