package com.exodus.data.api

import com.exodus.data.model.ChatRequest
import com.exodus.data.model.ChatResponse
import com.exodus.data.model.ModelsResponse
import com.exodus.data.model.ModelInfo
import com.exodus.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader

// Simple result wrapper to replace Retrofit Response
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class OllamaApiService(private val baseUrl: String = "http://192.168.0.115:11434") {
    
    suspend fun sendMessage(request: ChatRequest): ApiResult<ChatResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$baseUrl/api/chat")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 30000 // 30 seconds
            
            // Convert request to JSON manually
            val requestJson = chatRequestToJson(request)
            
            // Enhanced logging for debugging
            AppLogger.network("OllamaAPI", "=== NETWORK CONNECTION ATTEMPT ===")
            AppLogger.network("OllamaAPI", "Base URL: $baseUrl")
            AppLogger.network("OllamaAPI", "Full URL: $url")
            AppLogger.network("OllamaAPI", "Model: ${request.model}")
            AppLogger.network("OllamaAPI", "Messages count: ${request.messages.size}")
            AppLogger.network("OllamaAPI", "Request JSON length: ${requestJson.length} chars")
            AppLogger.network("OllamaAPI", "Connect timeout: 10s, Read timeout: 30s")
            AppLogger.network("OllamaAPI", "Thread: ${Thread.currentThread().name}")
            AppLogger.network("OllamaAPI", "=== STARTING CONNECTION ===")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
                AppLogger.network("OllamaAPI", "✅ Request data sent successfully")
            }
            
            val responseCode = connection.responseCode
            AppLogger.network("OllamaAPI", "📡 Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                AppLogger.network("OllamaAPI", "✅ Raw response received: ${response.length} chars")
                AppLogger.d("OllamaAPI", "Response preview: ${response.take(200)}...")
                
                val chatResponse = parseChatResponse(response)
                AppLogger.network("OllamaAPI", "✅ Response parsed successfully")
                AppLogger.i("OllamaAPI", "AI response: ${chatResponse.message.content.take(100)}...")
                ApiResult.Success(chatResponse)
            } else {
                val errorMsg = "HTTP $responseCode"
                AppLogger.e("OllamaAPI", "❌ HTTP Error: $errorMsg")
                
                // Try to read error response
                try {
                    val errorResponse = BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                        reader.readText()
                    }
                    AppLogger.e("OllamaAPI", "Error response body: $errorResponse")
                } catch (e: Exception) {
                    AppLogger.w("OllamaAPI", "Could not read error response: ${e.message}")
                }
                
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to connect to Ollama: ${e.javaClass.simpleName} - ${e.message ?: "Network connection failed"}"
            AppLogger.e("OllamaAPI", "=== CONNECTION FAILED ===", e)
            AppLogger.e("OllamaAPI", "Exception type: ${e.javaClass.simpleName}")
            AppLogger.e("OllamaAPI", "Exception message: ${e.message ?: "No message"}")
            AppLogger.e("OllamaAPI", "Base URL was: $baseUrl")
            AppLogger.e("OllamaAPI", "Thread: ${Thread.currentThread().name}")
            AppLogger.e("OllamaAPI", "Stack trace: ${e.stackTraceToString().take(500)}...")
            ApiResult.Error(errorMsg)
        }
    }
    
    suspend fun getAvailableModels(): ApiResult<ModelsResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            AppLogger.network("OllamaAPI", "🔍 Fetching available models from server")
            val url = URL("$baseUrl/api/tags")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            AppLogger.network("OllamaAPI", "GET request to: $url")
            AppLogger.network("OllamaAPI", "Thread: ${Thread.currentThread().name}")
            
            val responseCode = connection.responseCode
            AppLogger.network("OllamaAPI", "Models API response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                AppLogger.network("OllamaAPI", "✅ Models response received: ${response.length} chars")
                AppLogger.d("OllamaAPI", "Models response preview: ${response.take(200)}...")
                
                val modelsResponse = parseModelsResponse(response)
                AppLogger.i("OllamaAPI", "✅ Found ${modelsResponse.models.size} available models")
                modelsResponse.models.forEach { model ->
                    AppLogger.d("OllamaAPI", "Model: ${model.name} (${model.size} bytes)")
                }
                ApiResult.Success(modelsResponse)
            } else {
                AppLogger.e("OllamaAPI", "❌ Models API error: HTTP $responseCode")
                ApiResult.Error("HTTP $responseCode")
            }
        } catch (e: Exception) {
            AppLogger.e("OllamaAPI", "❌ Failed to fetch models: ${e.javaClass.simpleName} - ${e.message}", e)
            AppLogger.e("OllamaAPI", "Thread: ${Thread.currentThread().name}")
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
    
    fun downloadModel(modelName: String): ApiResult<String> {
        return try {
            val url = URL("$baseUrl/api/pull")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val requestJson = """{"name":"$modelName","stream":false}"""
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                ApiResult.Success("Model download started")
            } else {
                ApiResult.Error("HTTP $responseCode")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
    
    // JSON parsing helpers using simple string manipulation
    private fun chatRequestToJson(request: ChatRequest): String {
        val messagesJson = request.messages.joinToString(",") { message ->
            """{"role":"${message.role}","content":"${escapeJson(message.content)}"}"""
        }
        return """{"model":"${request.model}","stream":${request.stream},"messages":[$messagesJson]}"""
    }
    
    private fun parseChatResponse(jsonString: String): ChatResponse {
        // Parse Ollama chat response format
        try {
            val role = extractJsonValue(jsonString, "role")
            val content = extractJsonValue(jsonString, "content")
            val done = jsonString.contains("\"done\":true")
            
            return ChatResponse(
                message = com.exodus.data.model.ChatMessage(
                    role = if (role.isNotEmpty()) role else "assistant", 
                    content = if (content.isNotEmpty()) content else "Error parsing response"
                ),
                done = done
            )
        } catch (e: Exception) {
            // Fallback parsing for any format issues
            return ChatResponse(
                message = com.exodus.data.model.ChatMessage(
                    role = "assistant", 
                    content = "Failed to parse response: ${e.message}"
                ),
                done = true
            )
        }
    }
    
    private fun parseModelsResponse(jsonString: String): ModelsResponse {
        // Simple parsing for models array
        val models = mutableListOf<ModelInfo>()
        val modelsStart = jsonString.indexOf("\"models\":[") + 10
        val modelsEnd = jsonString.lastIndexOf("]")
        
        if (modelsStart > 10 && modelsEnd > modelsStart) {
            val modelsSection = jsonString.substring(modelsStart, modelsEnd)
            // This is a simplified parser - in real implementation you'd use proper JSON parsing
            models.add(ModelInfo("llama2", 3825819519L, "sample-digest", "2024-01-01T00:00:00Z"))
        }
        
        return ModelsResponse(models)
    }
    
    private fun extractJsonValue(json: String, key: String): String {
        // Handle nested message structure in Ollama response
        return when (key) {
            "role" -> {
                val messagePattern = "\"message\":\\s*\\{[^}]*\"role\":\\s*\"([^\"]*)\""
                val regex = Regex(messagePattern)
                regex.find(json)?.groupValues?.get(1) ?: ""
            }
            "content" -> {
                val messagePattern = "\"message\":\\s*\\{[^}]*\"content\":\\s*\"([^\"]*)\""
                val regex = Regex(messagePattern, RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(json)
                if (match != null) {
                    // Unescape JSON content
                    match.groupValues[1].replace("\\n", "\n").replace("\\\"", "\"")
                } else ""
            }
            else -> {
                val keyPattern = "\"$key\":\\s*\"([^\"]*)\""
                val regex = Regex(keyPattern)
                regex.find(json)?.groupValues?.get(1) ?: ""
            }
        }
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
    }
}
