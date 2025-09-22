package com.exodus.data.api

import com.exodus.data.model.ChatMessage
import com.exodus.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GroqApiService(private val client: OkHttpClient) {
    
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    
    companion object {
        private const val BASE_URL = "https://api.groq.com/openai/v1"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat/completions"
        private const val DEFAULT_MODEL = "llama-3.1-70b-versatile" // Fast and capable model
        
        // Available Groq models
        val AVAILABLE_MODELS = listOf(
            "llama-3.1-70b-versatile",
            "llama-3.1-8b-instant", 
            "mixtral-8x7b-32768",
            "gemma-7b-it"
        )
    }
    
    fun streamChat(
        messages: List<ChatMessage>,
        model: String = DEFAULT_MODEL,
        apiKey: String
    ): Flow<String> = flow {
        AppLogger.i("GroqAPI", "🌐 Starting Groq chat stream with model: $model")
        
        try {
            val requestBody = buildChatRequest(messages, model)
            AppLogger.d("GroqAPI", "📤 Request: $requestBody")
            
            val request = Request.Builder()
                .url(CHAT_ENDPOINT)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(mediaType))
                .build()
            
            // Use suspendCancellableCoroutine to make async network call
            val response = withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    val call = client.newCall(request)
                    continuation.invokeOnCancellation { call.cancel() }
                    
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            continuation.resumeWithException(e)
                        }
                        
                        override fun onResponse(call: Call, response: Response) {
                            continuation.resume(response)
                        }
                    })
                }
            }
            
            response.use {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    AppLogger.e("GroqAPI", "❌ HTTP ${response.code}: $errorBody")
                    throw IOException("Groq API error: HTTP ${response.code} - $errorBody")
                }
                
                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    AppLogger.e("GroqAPI", "❌ Empty response from Groq API")
                    throw IOException("Empty response from Groq API")
                }
                
                AppLogger.d("GroqAPI", "📥 Response: ${responseBody.take(200)}...")
                
                // Parse the response and extract content
                val content = parseGroqResponse(responseBody)
                AppLogger.i("GroqAPI", "✅ Groq response parsed successfully")
                
                emit(content)
            }
        } catch (e: Exception) {
            AppLogger.e("GroqAPI", "❌ Error in Groq chat: ${e.message}", e)
            throw e
        }
    }
    
    private fun buildChatRequest(messages: List<ChatMessage>, model: String): String {
        val messagesJson = messages.joinToString(",") { message ->
            val escapedContent = message.content.replace("\"", "\\\"").replace("\n", "\\n")
            
            // Include images if present (for vision models)
            val imagesJson = message.images?.let { images ->
                AppLogger.i("GroqAPI", "🖼️ Including ${images.size} images in message")
                val imagesList = images.joinToString(",") { "\"$it\"" }
                ""","images":[$imagesList]"""
            } ?: ""
            
            """{"role":"${message.role}","content":"$escapedContent"$imagesJson}"""
        }
        
        return """{
            "model": "$model",
            "messages": [$messagesJson],
            "stream": false,
            "max_tokens": 4096,
            "temperature": 0.7
        }"""
    }
    
    private fun parseGroqResponse(responseBody: String): String {
        return try {
            // Simple JSON parsing for the content
            val contentStart = responseBody.indexOf("\"content\":\"") + 11
            val contentEnd = responseBody.indexOf("\",", contentStart)
            
            if (contentStart > 10 && contentEnd > contentStart) {
                val content = responseBody.substring(contentStart, contentEnd)
                // Unescape JSON string
                content.replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\")
            } else {
                AppLogger.w("GroqAPI", "⚠️ Could not parse content from response")
                "Sorry, I couldn't process the response from Groq."
            }
        } catch (e: Exception) {
            AppLogger.e("GroqAPI", "❌ Error parsing Groq response: ${e.message}")
            "Sorry, there was an error processing the response."
        }
    }
    
    /**
     * Test API key validity
     */
    suspend fun testApiKey(apiKey: String): Boolean {
        return try {
            val testMessages = listOf(
                ChatMessage(role = "user", content = "Hello")
            )
            
            streamChat(testMessages, apiKey = apiKey).collect { 
                // If we get any response, the API key is valid
                return@collect
            }
            true
        } catch (e: Exception) {
            AppLogger.w("GroqAPI", "🔑 API key test failed: ${e.message}")
            false
        }
    }
}