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

class OllamaApiService(private val baseUrl: String = "http://26.202.89.251:11434") {
    
    fun sendMessage(request: ChatRequest): ApiResult<ChatResponse> {
        return try {
            val url = URL("$baseUrl/api/chat")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Convert request to JSON manually
            val requestJson = chatRequestToJson(request)
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                val chatResponse = parseChatResponse(response)
                ApiResult.Success(chatResponse)
            } else {
                ApiResult.Error("HTTP $responseCode")
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to connect to Ollama: ${e.javaClass.simpleName} - ${e.message ?: "Network connection failed"}")
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
        // Simple JSON parsing without external libraries
        val messageStart = jsonString.indexOf("\"message\":{") + 11
        val messageEnd = jsonString.indexOf("}", messageStart)
        val messageJson = jsonString.substring(messageStart, messageEnd)
        
        val role = extractJsonValue(messageJson, "role")
        val content = extractJsonValue(messageJson, "content")
        val done = jsonString.contains("\"done\":true")
        
        return ChatResponse(
            message = com.exodus.data.model.ChatMessage(role = role, content = content),
            done = done
        )
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
        val keyPattern = "\"$key\":\""
        val start = json.indexOf(keyPattern) + keyPattern.length
        val end = json.indexOf("\"", start)
        return if (start > keyPattern.length - 1 && end > start) {
            json.substring(start, end)
        } else ""
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
    }
}
