package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    suspend fun generateContent(
        apiKey: String,
        model: String = "gemini-2.5-flash",
        history: List<GeminiContent>,
        systemInstructionText: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is missing. Please set GEMINI_API_KEY in the AI Studio Secrets panel or configure it in Settings.")
            )
        }

        val requestBodyData = GeminiRequest(
            contents = history,
            systemInstruction = systemInstructionText?.let {
                GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 2048
            )
        )

        val jsonPayload = requestAdapter.toJson(requestBodyData)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorDetails = try {
                    val errorObj = responseAdapter.fromJson(responseString)
                    errorObj?.error?.message ?: response.message
                } catch (e: Exception) {
                    response.message
                }
                val code = response.code
                val errorMessage = when (code) {
                    400 -> "API ଅନୁରୋଧ ତ୍ରୁଟି (Bad Request): $errorDetails"
                    403 -> "API Key ଅମାନ୍ୟ କିମ୍ବା ଅନୁମତି ନାହିଁ (Forbidden). ଦୟାକରି ଆପଣଙ୍କ Gemini API Key ଯାଞ୍ଚ କରନ୍ତୁ।"
                    429 -> "ସୀମା ସମାପ୍ତ ହୋଇଛି (Rate limit exceeded). ଅଳ୍ପ ସମୟ ପରେ ପୁନଃ ଚେଷ୍ଟା କରନ୍ତୁ।"
                    500, 503 -> "Gemini ସର୍ଭର ବ୍ୟସ୍ତ ଅଛି। ଦୟାକରି କିଛି ସମୟ ପରେ ଚେଷ୍ଟା କରନ୍ତୁ।"
                    else -> "ତ୍ରୁଟି (Error $code): $errorDetails"
                }
                return@withContext Result.failure(IOException(errorMessage))
            }

            val parsedResponse = responseAdapter.fromJson(responseString)
            val text = parsedResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IOException("ମଡେଲରୁ କୌଣସି ଉତ୍ତର ଆସିନାହିଁ (Empty response from AI model)."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
