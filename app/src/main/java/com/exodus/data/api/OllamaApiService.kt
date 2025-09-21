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
        AppLogger.d("OllamaAPI", "🔨 Building JSON for ${request.messages.size} messages")
        
        val messagesJson = request.messages.joinToString(",") { message ->
            AppLogger.d("OllamaAPI", "📝 Message role: ${message.role}, content preview: ${message.content.take(50)}...")
            val escapedContent = escapeJson(message.content)
            AppLogger.d("OllamaAPI", "🔧 Escaped content preview: ${escapedContent.take(50)}...")
            
            // Include images field if present (for vision models)
            val imagesJson = message.images?.let { images ->
                val imagesList = images.joinToString(",") { "\"$it\"" }
                ""","images":[$imagesList]"""
            } ?: ""
            
            """{"role":"${message.role}","content":"${escapedContent}"$imagesJson}"""
        }
        
        val fullJson = """{"model":"${request.model}","stream":${request.stream},"messages":[$messagesJson]}"""
        AppLogger.d("OllamaAPI", "📄 Complete JSON preview: ${fullJson.take(200)}...")
        
        return fullJson
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
        val models = mutableListOf<ModelInfo>()
        
        try {
            AppLogger.d("OllamaAPI", "📝 Parsing models JSON: ${jsonString.take(200)}...")
            
            // Find the models array in the JSON
            val modelsStart = jsonString.indexOf("\"models\":[")
            if (modelsStart == -1) {
                AppLogger.w("OllamaAPI", "⚠️ No 'models' array found in response")
                return ModelsResponse(models)
            }
            
            // Extract the models array section
            val jsonAfterModels = jsonString.substring(modelsStart + 10) // Skip "models":[
            var braceCount = 0
            var inQuotes = false
            var escape = false
            var arrayEnd = -1
            
            for (i in jsonAfterModels.indices) {
                val char = jsonAfterModels[i]
                when {
                    escape -> escape = false
                    char == '\\' -> escape = true
                    char == '"' && !escape -> inQuotes = !inQuotes
                    !inQuotes && char == '[' -> braceCount++
                    !inQuotes && char == ']' -> {
                        if (braceCount == 0) {
                            arrayEnd = i
                            break
                        } else {
                            braceCount--
                        }
                    }
                }
            }
            
            if (arrayEnd != -1) {
                val modelsArray = jsonAfterModels.substring(0, arrayEnd)
                AppLogger.d("OllamaAPI", "📝 Models array content: ${modelsArray.take(100)}...")
                
                // Parse individual model objects
                parseModelObjects(modelsArray, models)
            }
            
        } catch (e: Exception) {
            AppLogger.e("OllamaAPI", "❌ Error parsing models response: ${e.message}", e)
        }
        
        AppLogger.i("OllamaAPI", "✅ Parsed ${models.size} models from server response")
        return ModelsResponse(models)
    }
    
    private fun parseModelObjects(modelsArray: String, models: MutableList<ModelInfo>) {
        // Split by objects (simple approach for now)
        val modelObjects = modelsArray.split("},{").map { 
            if (!it.startsWith("{")) "{$it" else it
        }.map { 
            if (!it.endsWith("}")) "$it}" else it
        }
        
        for (modelObj in modelObjects) {
            if (modelObj.trim().isEmpty() || modelObj == "{}") continue
            
            try {
                val name = extractModelField(modelObj, "name")
                val sizeStr = extractModelField(modelObj, "size")
                val digest = extractModelField(modelObj, "digest")
                val modifiedAt = extractModelField(modelObj, "modified_at")
                
                if (name.isNotEmpty()) {
                    val size = sizeStr.toLongOrNull() ?: 0L
                    models.add(ModelInfo(name, size, digest, modifiedAt))
                    AppLogger.d("OllamaAPI", "➕ Added model: $name (${size} bytes)")
                }
            } catch (e: Exception) {
                AppLogger.w("OllamaAPI", "⚠️ Failed to parse model object: ${modelObj.take(50)}... - ${e.message}")
            }
        }
    }
    
    private fun extractModelField(modelObj: String, fieldName: String): String {
        val pattern = "\"$fieldName\"\\s*:\\s*\"?([^,}\"]+)\"?"
        val regex = Regex(pattern)
        return regex.find(modelObj)?.groupValues?.get(1)?.trim('"') ?: ""
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
                // More robust content extraction that handles escaped quotes
                val messagePattern = "\"message\":\\s*\\{.*?\"content\":\\s*\"(.*?)\""
                val regex = Regex(messagePattern, setOf(RegexOption.DOT_MATCHES_ALL))
                
                var match = regex.find(json)
                if (match != null) {
                    var content = match.groupValues[1]
                    
                    // Handle case where content might contain escaped quotes - we need to find the actual end
                    var searchStart = match.range.last + 1
                    
                    // If the simple match fails due to internal quotes, use character-by-character parsing
                    if (content.endsWith("\\") || searchStart < json.length) {
                        val contentStart = json.indexOf("\"content\":\"") + 11
                        if (contentStart > 10) {
                            var i = contentStart
                            val contentBuilder = StringBuilder()
                            var escaped = false
                            
                            while (i < json.length) {
                                val char = json[i]
                                when {
                                    escaped -> {
                                        contentBuilder.append(char)
                                        escaped = false
                                    }
                                    char == '\\' -> {
                                        contentBuilder.append(char)
                                        escaped = true
                                    }
                                    char == '"' && !escaped -> break
                                    else -> contentBuilder.append(char)
                                }
                                i++
                            }
                            content = contentBuilder.toString()
                        }
                    }
                    
                    // Unescape JSON content
                    AppLogger.d("OllamaAPI", "🔍 Extracted content length: ${content.length} chars")
                    content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
                } else {
                    AppLogger.w("OllamaAPI", "⚠️ Failed to extract content from response")
                    ""
                }
            }
            else -> {
                val keyPattern = "\"$key\":\\s*\"([^\"]*)\""
                val regex = Regex(keyPattern)
                regex.find(json)?.groupValues?.get(1) ?: ""
            }
        }
    }
    
    private fun escapeJson(text: String): String {
        AppLogger.d("OllamaAPI", "🔧 Escaping JSON text: ${text.take(50)}...")
        return text
            .replace("\\", "\\\\")  // Escape backslashes first
            .replace("\"", "\\\"")  // Escape quotes
            .replace("\n", "\\n")   // Escape newlines
            .replace("\r", "\\r")   // Escape carriage returns
            .replace("\t", "\\t")   // Escape tabs
    }
}
