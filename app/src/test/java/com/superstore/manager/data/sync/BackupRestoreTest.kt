package com.superstore.manager.data.sync

import android.content.Context
import com.superstore.manager.data.local.AppDatabase
import com.superstore.manager.data.local.entity.ProductEntity
import com.superstore.manager.data.local.entity.UnitType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BackupRestoreTest {

    @Test
    fun testBackupPayloadSerializationAndDeserialization() {
        val payload = BackupPayload(
            exportTimestamp = 100000L,
            appVersion = "1.0",
            products = listOf(
                ProductEntity(
                    id = 1,
                    nameEn = "Rice 5kg",
                    nameUr = "چاول",
                    unit = UnitType.KG,
                    currentStock = "20",
                    costPrice = "1200",
                    sellingPrice = "1350"
                )
            )
        )

        val json = com.google.gson.Gson().toJson(payload)
        val deserialized = com.google.gson.Gson().fromJson(json, BackupPayload::class.java)

        assertNotNull(deserialized)
        assertEquals(1, deserialized.products.size)
        assertEquals("Rice 5kg", deserialized.products[0].nameEn)
        assertEquals("چاول", deserialized.products[0].nameUr)
    }
}
