package com.superstore.manager.data.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiProductSuggestion(
    @SerializedName("nameEn") val nameEn: String = "",
    @SerializedName("nameUr") val nameUr: String = "",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("suggestedUnit") val suggestedUnit: String = "PCS",
    @SerializedName("description") val description: String = "",
    @SerializedName("confidence") val confidence: Float = 0.8f
)

interface AIProductIdentifier {
    suspend fun identifyProductFromImage(
        context: Context,
        imageUri: String,
        apiKey: String
    ): Result<AiProductSuggestion>
}

class AIProductIdentifierImpl : AIProductIdentifier {

    override suspend fun identifyProductFromImage(
        context: Context,
        imageUri: String,
        apiKey: String
    ): Result<AiProductSuggestion> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("AI API key is missing. Please configure it in Settings or fill manually."))
        }

        try {
            val dummyJson = """
                {
                    "nameEn": "Nestle Milkpak 1 Litre",
                    "nameUr": "نسلے ملَک پیک ۱ لیٹر",
                    "brand": "Nestle",
                    "category": "Dairy / ڈائری",
                    "suggestedUnit": "L",
                    "description": "1 Litre Milk Carton",
                    "confidence": 0.92
                }
            """.trimIndent()

            val suggestion = Gson().fromJson(dummyJson, AiProductSuggestion::class.java)
            Result.success(suggestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
