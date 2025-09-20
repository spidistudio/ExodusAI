package com.exodus.data.repository

import com.exodus.data.api.OllamaApiClient
import com.exodus.data.api.ApiResult
import com.exodus.data.model.ChatMessage
import com.exodus.data.model.ChatRequest
import com.exodus.data.database.MessageDao
import com.exodus.data.model.AIModel
import com.exodus.data.model.Message
import java.util.Date

class ChatRepository(
    private val ollamaApiClient: OllamaApiClient,
    private val messageDao: MessageDao
) {

    fun getAllMessages(): List<Message> = try {
        // For now, return sample data - in real app this would use Room database
        listOf()
    } catch (e: Exception) {
        listOf()
    }

    fun getMessagesByModel(modelName: String): List<Message> = try {
        // Filter messages by model - placeholder implementation
        listOf()
    } catch (e: Exception) {
        listOf()
    }

    fun sendMessage(
        message: String,
        modelName: String,
        conversationHistory: List<Message>
    ): String {
        return try {
            // Create user message
            val userMessage = Message(
                content = message,
                isFromUser = true,
                timestamp = Date(),
                modelName = modelName
            )
            
            // Prepare chat history for API
            val chatMessages = conversationHistory.map { msg ->
                ChatMessage(
                    role = if (msg.isFromUser) "user" else "assistant",
                    content = msg.content
                )
            }.toMutableList()

            // Add current message
            chatMessages.add(ChatMessage(role = "user", content = message))

            // Send to Ollama
            val request = ChatRequest(
                model = modelName,
                messages = chatMessages
            )

            when (val response = ollamaApiClient.sendMessage(request)) {
                is ApiResult.Success -> {
                    val aiResponse = response.data.message.content
                    android.util.Log.d("ExodusAI", "ChatRepository: AI response received: $aiResponse")
                    
                    // In real app, save to database here
                    // messageDao.insertMessage(userMessage)
                    // messageDao.insertMessage(aiMessage)
                    
                    aiResponse
                }
                is ApiResult.Error -> {
                    android.util.Log.e("ExodusAI", "ChatRepository: API error: ${response.message}")
                    // Provide a helpful fallback response when Ollama is not available
                    when {
                        response.message.contains("Failed to connect") || 
                        response.message.contains("Connection refused") || 
                        response.message.contains("Unable to resolve host") ||
                        response.message.contains("Network connection failed") -> {
                            "🤖 **Demo Mode Active**\n\n" +
                            "Hi! I received your message: \"$message\"\n\n" +
                            "I'm currently running in demo mode because Ollama server is not available. " +
                            "To get real AI responses:\n\n" +
                            "1. Install Ollama from https://ollama.ai\n" +
                            "2. Run: `ollama pull codellama:13b-instruct`\n" +
                            "3. Start server: `ollama serve`\n\n" +
                            "For now, I can only echo your messages in demo mode! CodeLlama 13B is perfect for coding tasks! �"
                        }
                        else -> {
                            "🤖 **Demo Response**\n\n" +
                            "Your message: \"$message\"\n\n" +
                            "API Error: ${response.message}\n\n" +
                            "To enable real AI responses, please install Ollama server."
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Provide demo response for testing
            "🤖 **Demo Mode - Exception Caught**\n\n" +
            "Your message: \"$message\"\n\n" +
            "This is a test response since Ollama server is not running.\n\n" +
            "Error details: ${e.javaClass.simpleName} - ${e.message ?: "Unknown error"}\n\n" +
            "**To get real AI responses:**\n" +
            "1. Install Ollama from https://ollama.ai\n" +
            "2. Run: `ollama pull codellama:13b-instruct`\n" +
            "3. Start: `ollama serve`\n\n" +
            "Your CodeLlama 13B model will provide excellent coding assistance! 🚀"
        }
    }

    fun getAvailableModels(): List<AIModel> {
        return try {
            when (val response = ollamaApiClient.getAvailableModels()) {
                is ApiResult.Success -> {
                    response.data.models.map { modelInfo ->
                        AIModel(
                            name = modelInfo.name,
                            displayName = modelInfo.name.replace(":", " - "),
                            size = formatBytes(modelInfo.size),
                            isDownloaded = true
                        )
                    }
                }
                is ApiResult.Error -> {
                    // Return default models if API fails
                    listOf(
                        AIModel("codellama-custom:latest", "CodeLlama Custom 13B", "3.6 GB", true),
                        AIModel("codellama:latest", "CodeLlama Standard", "3.6 GB", true)
                    )
                }
            }
        } catch (e: Exception) {
            // Return default models if error
            listOf(
                AIModel("codellama:13b-instruct", "CodeLlama 13B Instruct", "7.3 GB", true),
                AIModel("llama2", "Llama 2", "3.8 GB", true)
            )
        }
    }

    fun downloadModel(modelName: String): String {
        return try {
            when (val response = ollamaApiClient.downloadModel(modelName)) {
                is ApiResult.Success -> {
                    "Model download started"
                }
                is ApiResult.Error -> {
                    "Failed to download model: ${response.message}"
                }
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun clearMessages() {
        // In real app: messageDao.clearAllMessages()
    }

    fun clearMessagesForModel(modelName: String) {
        // In real app: messageDao.clearMessagesForModel(modelName)
    }

    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> "%.1f GB".format(gb)
            mb >= 1 -> "%.1f MB".format(mb)
            kb >= 1 -> "%.1f KB".format(kb)
            else -> "$bytes B"
        }
    }
}
