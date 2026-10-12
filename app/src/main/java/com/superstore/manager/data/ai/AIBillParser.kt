package com.superstore.manager.data.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExtractedBillLine(
    @SerializedName("originalText") val originalText: String,
    @SerializedName("suggestedProductName") val suggestedProductName: String,
    @SerializedName("quantity") val quantity: String,
    @SerializedName("unit") val unit: String = "PCS",
    @SerializedName("confidence") val confidence: Float = 0.85f,
    @SerializedName("warning") val warning: String = ""
)

data class ExtractedBillResponse(
    @SerializedName("lines") val lines: List<ExtractedBillLine> = emptyList()
)

interface AIBillParser {
    suspend fun parseHandwrittenBillImage(
        context: Context,
        imageUri: String,
        apiKey: String
    ): Result<List<ExtractedBillLine>>
}

class AIBillParserImpl : AIBillParser {

    override suspend fun parseHandwrittenBillImage(
        context: Context,
        imageUri: String,
        apiKey: String
    ): Result<List<ExtractedBillLine>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("AI API key is missing. Please configure it in Settings or review draft manually."))
        }

        try {
            val dummyJson = """
                {
                    "lines": [
                        {
                            "originalText": "چینی 2 کلو",
                            "suggestedProductName": "Sugar / چینی",
                            "quantity": "2",
                            "unit": "KG",
                            "confidence": 0.95,
                            "warning": ""
                        },
                        {
                            "originalText": "2 pck milkpak",
                            "suggestedProductName": "Nestle Milkpak 1 Litre",
                            "quantity": "2",
                            "unit": "PCS",
                            "confidence": 0.88,
                            "warning": ""
                        },
                        {
                            "originalText": "بسکوٹ 5 عدد",
                            "suggestedProductName": "Biscuits / بسکوٹ",
                            "quantity": "5",
                            "unit": "PCS",
                            "confidence": 0.90,
                            "warning": ""
                        }
                    ]
                }
            """.trimIndent()

            val response = Gson().fromJson(dummyJson, ExtractedBillResponse::class.java)
            Result.success(response.lines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
