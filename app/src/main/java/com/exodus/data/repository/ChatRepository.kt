package com.exodus.data.repository

import com.exodus.data.api.OllamaApiClient
import com.exodus.data.api.ApiResult
import com.exodus.data.model.ChatMessage
import com.exodus.data.model.ChatRequest
import com.exodus.data.database.MessageDao
import com.exodus.data.model.AIModel
import com.exodus.data.model.Message
import com.exodus.utils.AppLogger
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

    suspend fun sendMessage(
        message: String,
        modelName: String,
        conversationHistory: List<Message>
    ): String {
        return try {
            AppLogger.i("ChatRepo", "💬 Sending message to model: $modelName")
            AppLogger.d("ChatRepo", "Message length: ${message.length} chars")
            AppLogger.d("ChatRepo", "Conversation history: ${conversationHistory.size} messages")
            
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
            
            AppLogger.network("ChatRepo", "🚀 Calling Ollama API with ${chatMessages.size} messages")

            when (val response = ollamaApiClient.sendMessage(request)) {
                is ApiResult.Success -> {
                    val aiResponse = response.data.message.content
                    AppLogger.i("ChatRepo", "✅ AI response received: ${aiResponse.length} chars")
                    AppLogger.d("ChatRepo", "Response preview: ${aiResponse.take(100)}...")
                    
                    // In real app, save to database here
                    // messageDao.insertMessage(userMessage)
                    // messageDao.insertMessage(aiMessage)
                    
                    aiResponse
                }
                is ApiResult.Error -> {
                    AppLogger.e("ChatRepo", "❌ API error: ${response.message}")
                    AppLogger.w("ChatRepo", "Falling back to demo mode")
                    
                    // Provide a helpful fallback response when Ollama is not available
                    when {
                        response.message.contains("model") && response.message.contains("not found") -> {
                            "🤖 **Model Not Found**\n\n" +
                            "The model '$modelName' is not available on your Ollama server.\n\n" +
                            "**Available models on your server:**\n" +
                            "• codellama:latest\n" +
                            "• codellama-custom:latest\n\n" +
                            "**To install more models:**\n" +
                            "1. `ollama pull llama2` - Install Llama 2\n" +
                            "2. `ollama pull codellama:13b-instruct` - Install CodeLlama 13B\n" +
                            "3. `ollama list` - See all installed models\n\n" +
                            "Please select an available model from the dropdown above."
                        }
                        response.message.contains("Failed to connect") || 
                        response.message.contains("Connection refused") || 
                        response.message.contains("Unable to resolve host") ||
                        response.message.contains("Network connection failed") -> {
                            "🤖 **Demo Mode Active**\n\n" +
                            "Hi! I received your message: \"$message\"\n\n" +
                            "I'm currently running in demo mode because Ollama server is not available. " +
                            "To get real AI responses:\n\n" +
                            "1. Install Ollama from https://ollama.ai\n" +
                            "2. Run: `ollama pull codellama:latest`\n" +
                            "3. Start server: `ollama serve`\n\n" +
                            "For now, I can only echo your messages in demo mode! CodeLlama is perfect for coding tasks! 💻"
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

    suspend fun getAvailableModels(): List<AIModel> {
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
                    AppLogger.w("ChatRepo", "⚠️ API failed to fetch models, using fallback list")
                    // Return models that we know exist on your server
                    listOf(
                        AIModel("codellama:latest", "CodeLlama Latest", "3.8 GB", true),
                        AIModel("codellama-custom:latest", "CodeLlama Custom", "3.8 GB", true)
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ChatRepo", "❌ Exception getting models: ${e.message}", e)
            // Return models that we know exist on your server
            listOf(
                AIModel("codellama:latest", "CodeLlama Latest", "3.8 GB", true),
                AIModel("codellama-custom:latest", "CodeLlama Custom", "3.8 GB", true)
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
