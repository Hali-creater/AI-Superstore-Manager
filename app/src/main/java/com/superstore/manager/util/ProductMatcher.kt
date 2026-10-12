package com.superstore.manager.util

import com.superstore.manager.data.local.entity.ProductAliasEntity
import com.superstore.manager.data.local.entity.ProductEntity

object ProductMatcher {

    fun findBestMatch(
        query: String,
        products: List<ProductEntity>,
        aliases: List<ProductAliasEntity>
    ): ProductEntity? {
        if (query.isBlank()) return null
        val normalized = query.trim().lowercase()

        val exactNameMatch = products.firstOrNull {
            it.nameEn.lowercase() == normalized || it.nameUr.lowercase() == normalized
        }
        if (exactNameMatch != null) return exactNameMatch

        val matchedAlias = aliases.firstOrNull { it.alias.trim().lowercase() == normalized }
        if (matchedAlias != null) {
            val matchedProduct = products.firstOrNull { it.id == matchedAlias.productId }
            if (matchedProduct != null) return matchedProduct
        }

        val partialMatch = products.firstOrNull {
            it.nameEn.lowercase().contains(normalized) ||
            it.nameUr.lowercase().contains(normalized) ||
            normalized.contains(it.nameEn.lowercase()) ||
            (it.nameUr.isNotBlank() && normalized.contains(it.nameUr.lowercase()))
        }
        if (partialMatch != null) return partialMatch

        return null
    }
}
