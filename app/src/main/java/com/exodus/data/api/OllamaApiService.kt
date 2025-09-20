package com.exodus.data.api

import com.exodus.data.model.ChatRequest
import com.exodus.data.model.ChatResponse
import com.exodus.data.model.ModelsResponse
import com.exodus.data.model.ModelInfo
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
    
    fun sendMessage(request: ChatRequest): ApiResult<ChatResponse> {
        return try {
            val url = URL("$baseUrl/api/chat")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 30000 // 30 seconds
            
            // Convert request to JSON manually
            val requestJson = chatRequestToJson(request)
            
            // Log the request for debugging
            android.util.Log.d("ExodusAI", "Sending request to: $url")
            android.util.Log.d("ExodusAI", "Request JSON: $requestJson")
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            android.util.Log.d("ExodusAI", "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                android.util.Log.d("ExodusAI", "Raw response: $response")
                val chatResponse = parseChatResponse(response)
                android.util.Log.d("ExodusAI", "Parsed response: ${chatResponse.message.content}")
                ApiResult.Success(chatResponse)
            } else {
                val errorMsg = "HTTP $responseCode"
                android.util.Log.e("ExodusAI", "HTTP Error: $errorMsg")
                ApiResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to connect to Ollama: ${e.javaClass.simpleName} - ${e.message ?: "Network connection failed"}"
            android.util.Log.e("ExodusAI", "Exception in sendMessage: $errorMsg", e)
            ApiResult.Error(errorMsg)
        }
    }
    
    fun getAvailableModels(): ApiResult<ModelsResponse> {
        return try {
            val url = URL("$baseUrl/api/tags")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                val modelsResponse = parseModelsResponse(response)
                ApiResult.Success(modelsResponse)
            } else {
                ApiResult.Error("HTTP $responseCode")
            }
        } catch (e: Exception) {
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
