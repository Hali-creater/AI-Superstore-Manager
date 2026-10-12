package com.superstore.manager.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.superstore.manager.data.local.entity.ProductEntity
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun exportProductsToCsv(context: Context, products: List<ProductEntity>): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, "products_catalogue_${System.currentTimeMillis()}.csv")
            val writer = FileWriter(file)

            writer.append("ID,Name (English),Name (Urdu),Brand,Category,Unit,Current Stock,Min Threshold,Purchase Cost,Selling Price,Barcode\n")

            products.forEach { p ->
                writer.append("${p.id},\"${p.nameEn.replace("\"", "\"\"")}\",\"${p.nameUr.replace("\"", "\"\"")}\",\"${p.brand}\",\"${p.category}\",${p.unit.name},${p.currentStock},${p.minThreshold},${p.costPrice},${p.sellingPrice},\"${p.barcode}\"\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsvFile(context: Context, file: File, title: String = "Share CSV") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
