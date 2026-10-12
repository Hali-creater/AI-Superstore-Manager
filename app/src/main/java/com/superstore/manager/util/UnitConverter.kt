package com.superstore.manager.util

import com.superstore.manager.data.local.entity.UnitType
import java.math.BigDecimal
import java.math.RoundingMode

object UnitConverter {

    fun convertQuantity(
        quantity: BigDecimal,
        fromUnit: UnitType,
        toUnit: UnitType
    ): BigDecimal {
        if (fromUnit == toUnit) return quantity

        return when {
            fromUnit == UnitType.G && toUnit == UnitType.KG -> {
                quantity.divide(BigDecimal("1000"), 4, RoundingMode.HALF_UP)
            }
            fromUnit == UnitType.KG && toUnit == UnitType.G -> {
                quantity.multiply(BigDecimal("1000"))
            }
            fromUnit == UnitType.ML && toUnit == UnitType.L -> {
                quantity.divide(BigDecimal("1000"), 4, RoundingMode.HALF_UP)
            }
            fromUnit == UnitType.L && toUnit == UnitType.ML -> {
                quantity.multiply(BigDecimal("1000"))
            }
            else -> quantity
        }
    }

    fun calculateTotalCost(quantity: BigDecimal, costPrice: BigDecimal): BigDecimal {
        return quantity.multiply(costPrice).setScale(2, RoundingMode.HALF_UP)
    }

    fun calculateSalesValue(quantity: BigDecimal, sellingPrice: BigDecimal): BigDecimal {
        return quantity.multiply(sellingPrice).setScale(2, RoundingMode.HALF_UP)
    }

    fun formatQuantity(qty: BigDecimal): String {
        return qty.stripTrailingZeros().toPlainString()
    }

    fun parseBigDecimal(text: String, defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
        return try {
            if (text.isBlank()) defaultValue else BigDecimal(text.trim())
        } catch (e: Exception) {
            defaultValue
        }
    }
}
