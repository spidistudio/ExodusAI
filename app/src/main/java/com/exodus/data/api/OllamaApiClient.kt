package com.exodus.data.api

import com.exodus.data.model.ChatRequest
import com.exodus.data.model.ChatResponse
import com.exodus.data.model.ModelsResponse
import javax.inject.Singleton

/**
 * HTTP client wrapper for Ollama API communication
 * Provides a clean interface for the service layer
 */
@Singleton
class OllamaApiClient(
    private val baseUrl: String = "http://192.168.0.115:11434"  // Local WiFi IP for phone access
) {
    private val apiService = OllamaApiService(baseUrl)
    
    fun sendMessage(request: ChatRequest): ApiResult<ChatResponse> {
        return apiService.sendMessage(request)
    }
    
    fun getAvailableModels(): ApiResult<ModelsResponse> {
        return apiService.getAvailableModels()
    }
    
    fun downloadModel(modelName: String): ApiResult<String> {
        return apiService.downloadModel(modelName)
    }
}
