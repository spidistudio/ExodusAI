package com.exodus.data.repository

import android.content.Context
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.api.ApiResult
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import com.exodus.data.model.ChatMessage
import com.exodus.data.model.ChatRequest
import com.exodus.data.database.MessageDao
import com.exodus.data.model.AIModel
import com.exodus.data.model.Message
import com.exodus.utils.AppLogger
import com.exodus.utils.DocumentProcessor
import com.exodus.utils.ImageEncoder
import java.util.Date

class ChatRepository(
    private val context: Context,
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

    /**
     * Checks if the model supports vision/image analysis
     */
    private fun isVisionModel(modelName: String): Boolean {
        val visionModels = listOf(
            "llava", "bakllava", "moondream", "llava-llama3", "llava-phi3", "llava-v1.6"
        )
        return visionModels.any { visionModel -> 
            modelName.lowercase().contains(visionModel.lowercase()) 
        }
    }

    /**
     * Finds the best available vision model, prioritizing llava
     */
    private suspend fun findBestVisionModel(): String? {
        return try {
            when (val response = ollamaApiClient.getAvailableModels()) {
                is ApiResult.Success -> {
                    val availableModels = response.data.models.map { it.name }
                    AppLogger.i("ChatRepo", "🔍 Searching for vision models in: ${availableModels.joinToString(", ")}")
                    
                    // Priority order: llava variants first, then others
                    val visionModelPriority = listOf(
                        "llava", "llava-llama3", "llava-v1.6", "llava-phi3", 
                        "bakllava", "moondream"
                    )
                    
                    for (preferredModel in visionModelPriority) {
                        val foundModel = availableModels.find { model ->
                            model.lowercase().contains(preferredModel.lowercase())
                        }
                        if (foundModel != null) {
                            AppLogger.i("ChatRepo", "✅ Found vision model: $foundModel")
                            return foundModel
                        }
                    }
                    
                    AppLogger.w("ChatRepo", "❌ No vision models found")
                    null
                }
                is ApiResult.Error -> {
                    AppLogger.e("ChatRepo", "Failed to fetch models for vision detection: ${response.message}")
                    null
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ChatRepo", "Exception finding vision model: ${e.message}")
            null
        }
    }

    suspend fun sendMessage(
        message: String,
        modelName: String,
        conversationHistory: List<Message>,
        attachments: List<Attachment> = emptyList()
    ): String {
        return try {
            AppLogger.i("ChatRepo", "💬 Sending message to model: $modelName")
            AppLogger.d("ChatRepo", "Message length: ${message.length} chars")
            AppLogger.d("ChatRepo", "Conversation history: ${conversationHistory.size} messages")
            AppLogger.d("ChatRepo", "Attachments: ${attachments.size} files")
            
            // Check for vision model compatibility when images are attached
            val hasImages = attachments.any { it.type == AttachmentType.IMAGE }
            var actualModelName = modelName
            
            if (hasImages && !isVisionModel(modelName)) {
                AppLogger.w("ChatRepo", "⚠️ Image attachments sent to non-vision model: $modelName")
                AppLogger.i("ChatRepo", "🔄 Automatically searching for vision model...")
                
                val visionModel = findBestVisionModel()
                if (visionModel != null) {
                    actualModelName = visionModel
                    AppLogger.i("ChatRepo", "✅ Auto-switched to vision model: $actualModelName")
                } else {
                    AppLogger.e("ChatRepo", "❌ No vision models available for auto-switch")
                    return "🤖 **Vision Model Required**\n\n" +
                            "You've attached an image, but **$modelName** is a text-only model that doesn't support image analysis.\n\n" +
                            "**No vision models found on your server!**\n\n" +
                            "**To analyze images, install a vision model:**\n" +
                            "• `ollama pull llava` - General image analysis (recommended)\n" +
                            "• `ollama pull bakllava` - Llama-based vision model\n" +
                            "• `ollama pull moondream` - Lightweight vision model\n\n" +
                            "**After installing:**\n" +
                            "1. Restart your Ollama server\n" +
                            "2. Resend your image - ExodusAI will auto-switch to the vision model!\n\n" +
                            "Your text message: \"$message\""
                }
            }
            
            // Create user message
            val userMessage = Message(
                content = message,
                isFromUser = true,
                timestamp = Date(),
                modelName = actualModelName,
                attachments = attachments
            )
            
            // Prepare chat history for API
            val chatMessages = conversationHistory.map { msg ->
                ChatMessage(
                    role = if (msg.isFromUser) "user" else "assistant",
                    content = msg.content
                )
            }.toMutableList()

            // Add current message with image attachments and document content
            val imageData = ImageEncoder.encodeImageAttachments(context, attachments)
            val documentData = DocumentProcessor.extractTextFromDocuments(context, attachments)
            
            // Build enhanced message content with document text
            val enhancedContent = buildString {
                append(message)
                
                // Add document content if any
                if (documentData.isNotEmpty()) {
                    append("\n\n📄 **Attached Documents:**\n")
                    documentData.forEachIndexed { index, doc ->
                        append("\n**${index + 1}. ${doc.fileName}**\n")
                        append("Type: ${doc.mimeType}\n")
                        append("Content:\n```\n${doc.textContent}\n```\n")
                    }
                }
            }
            
            val currentMessage = ChatMessage(
                role = "user", 
                content = enhancedContent,
                images = if (imageData.isNotEmpty()) imageData else null
            )
            chatMessages.add(currentMessage)

            // Send to Ollama
            val request = ChatRequest(
                model = actualModelName,
                messages = chatMessages
            )
            
            AppLogger.network("ChatRepo", "🚀 Calling Ollama API with ${chatMessages.size} messages")
            AppLogger.i("ChatRepo", "🎯 Using model: $actualModelName ${if (actualModelName != modelName) "(auto-switched from $modelName)" else ""}")

            when (val response = ollamaApiClient.sendMessage(request)) {
                is ApiResult.Success -> {
                    val aiResponse = response.data.message.content
                    AppLogger.i("ChatRepo", "✅ AI response received: ${aiResponse.length} chars")
                    AppLogger.d("ChatRepo", "Response preview: ${aiResponse.take(100)}...")
                    
                    // Add model switch notification if auto-switched
                    val finalResponse = if (actualModelName != modelName) {
                        "🔄 **Auto-switched to vision model: $actualModelName**\n\n$aiResponse"
                    } else {
                        aiResponse
                    }
                    
                    // In real app, save to database here
                    // messageDao.insertMessage(userMessage)
                    // messageDao.insertMessage(aiMessage)
                    
                    finalResponse
                }
                is ApiResult.Error -> {
                    AppLogger.e("ChatRepo", "❌ API error: ${response.message}")
                    AppLogger.w("ChatRepo", "Falling back to demo mode")
                    
                    // Provide a helpful fallback response when Ollama is not available
                    val attachmentText = if (attachments.isNotEmpty()) {
                        "\n\n📎 **Attachments received (${attachments.size}):**\n" +
                        attachments.joinToString("\n") { "• ${it.fileName} (${it.type})" }
                    } else ""
                    
                    when {
                        response.message.contains("model") && response.message.contains("not found") -> {
                            "🤖 **Model Not Found**\n\n" +
                            "The model '$modelName' is not available on your Ollama server.\n\n" +
                            "**Available model on your server:**\n" +
                            "• llama3.2:latest (recommended - most recent knowledge)\n\n" +
                            "**To install more models:**\n" +
                            "1. `ollama pull llama3.1` - Install Llama 3.1\n" +
                            "2. `ollama pull codellama` - Install CodeLlama for coding\n" +
                            "3. `ollama list` - See all installed models\n\n" +
                            "Please select Llama 3.2 from the dropdown above.$attachmentText"
                        }
                        response.message.contains("Failed to connect") || 
                        response.message.contains("Connection refused") || 
                        response.message.contains("Unable to resolve host") ||
                        response.message.contains("Network connection failed") -> {
                            "🤖 **Demo Mode Active**\n\n" +
                            "Hi! I received your message: \"$message\"$attachmentText\n\n" +
                            "I'm currently running in demo mode because Ollama server is not available. " +
                            "To get real AI responses:\n\n" +
                            "1. Install Ollama from https://ollama.ai\n" +
                            "2. Run: `ollama pull codellama:latest`\n" +
                            "3. Start server: `ollama serve`\n\n" +
                            "For now, I can only echo your messages in demo mode! CodeLlama is perfect for coding tasks! 💻"
                        }
                        else -> {
                            "🤖 **Demo Response**\n\n" +
                            "Your message: \"$message\"$attachmentText\n\n" +
                            "API Error: ${response.message}\n\n" +
                            "To enable real AI responses, please install Ollama server."
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val attachmentText = if (attachments.isNotEmpty()) {
                "\n\n📎 **Attachments received (${attachments.size}):**\n" +
                attachments.joinToString("\n") { "• ${it.fileName} (${it.type})" }
            } else ""
            
            // Provide demo response for testing
            "🤖 **Demo Mode - Exception Caught**\n\n" +
            "Your message: \"$message\"$attachmentText\n\n" +
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
                    // Return the model that exists on your server
                    listOf(
                        AIModel("llama3.2:latest", "Llama 3.2 Latest", "2.0 GB", true)
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ChatRepo", "❌ Exception getting models: ${e.message}", e)
            // Return the model that exists on your server
            listOf(
                AIModel("llama3.2:latest", "Llama 3.2 Latest", "2.0 GB", true)
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
