package com.exodus.data.repository

import android.content.Context
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.api.GroqApiService
import com.exodus.data.api.ApiResult
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import com.exodus.data.model.ChatMessage
import com.exodus.data.model.ChatRequest
import com.exodus.data.model.LLMProvider
import com.exodus.data.database.MessageDao
import com.exodus.data.model.AIModel
import com.exodus.data.model.Message
import com.exodus.utils.AppLogger
import com.exodus.utils.DocumentProcessor
import com.exodus.utils.ImageEncoder
import com.exodus.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class ChatRepository(
    private val context: Context,
    private val ollamaApiClient: OllamaApiClient,
    private val groqApiService: GroqApiService,
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

    /**
     * Determines the best provider to use based on user preference and network availability
     */
    private suspend fun selectProvider(
        userPreference: LLMProvider,
        groqApiKey: String?
    ): LLMProvider {
        return when (userPreference) {
            LLMProvider.OLLAMA_LOCAL -> {
                AppLogger.i("ChatRepo", "🏠 Using Ollama (Local) as requested")
                LLMProvider.OLLAMA_LOCAL
            }
            LLMProvider.GROQ_ONLINE -> {
                if (groqApiKey.isNullOrBlank()) {
                    AppLogger.w("ChatRepo", "🔑 Groq API key not configured, falling back to Ollama")
                    LLMProvider.OLLAMA_LOCAL
                } else if (!NetworkUtils.isNetworkAvailable(context)) {
                    AppLogger.w("ChatRepo", "📶 No internet connection, falling back to Ollama")
                    LLMProvider.OLLAMA_LOCAL
                } else {
                    AppLogger.i("ChatRepo", "🌐 Using Groq (Online) as requested")
                    LLMProvider.GROQ_ONLINE
                }
            }
            LLMProvider.AUTO -> {
                if (groqApiKey.isNullOrBlank()) {
                    AppLogger.i("ChatRepo", "🤖 Auto mode: No Groq API key, using Ollama")
                    LLMProvider.OLLAMA_LOCAL
                } else if (!NetworkUtils.isNetworkAvailable(context)) {
                    AppLogger.i("ChatRepo", "🤖 Auto mode: No internet, using Ollama")
                    LLMProvider.OLLAMA_LOCAL
                } else {
                    AppLogger.i("ChatRepo", "🤖 Auto mode: Internet available, using Groq")
                    LLMProvider.GROQ_ONLINE
                }
            }
        }
    }

    suspend fun sendMessage(
        message: String,
        modelName: String,
        conversationHistory: List<Message>,
        attachments: List<Attachment> = emptyList(),
        providerPreference: LLMProvider = LLMProvider.AUTO,
        groqApiKey: String? = null
    ): String {
        return try {
            AppLogger.i("ChatRepo", "💬 Sending message to model: $modelName")
            AppLogger.d("ChatRepo", "Message length: ${message.length} chars")
            AppLogger.d("ChatRepo", "Conversation history: ${conversationHistory.size} messages")
            AppLogger.d("ChatRepo", "Attachments: ${attachments.size} files")
            
            // Determine which provider to use
            val selectedProvider = selectProvider(providerPreference, groqApiKey)
            AppLogger.i("ChatRepo", "🎯 Selected provider: ${selectedProvider.displayName}")
            
            // Check for vision model compatibility when images are attached (Ollama only)
            val hasImages = attachments.any { it.type == AttachmentType.IMAGE }
            var actualModelName = modelName
            
            if (hasImages && selectedProvider == LLMProvider.OLLAMA_LOCAL && !isVisionModel(modelName)) {
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
            
            // For Groq, warn about images but continue (Groq has vision models)
            if (hasImages && selectedProvider == LLMProvider.GROQ_ONLINE) {
                AppLogger.i("ChatRepo", "🖼️ Images detected with Groq - using vision-capable model")
                // Groq's llama-3.1-70b-versatile can handle images
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

            // Call the appropriate API based on selected provider
            val (aiResponse, actualProviderUsed) = when (selectedProvider) {
                LLMProvider.OLLAMA_LOCAL -> {
                    AppLogger.i("ChatRepo", "🏠 Calling Ollama API")
                    val response = callOllamaApiWithErrorHandling(actualModelName, chatMessages)
                    Pair(response, LLMProvider.OLLAMA_LOCAL)
                }
                LLMProvider.GROQ_ONLINE -> {
                    AppLogger.i("ChatRepo", "🌐 Calling Groq API")
                    val response = callGroqApi(chatMessages, groqApiKey!!)
                    Pair(response, LLMProvider.GROQ_ONLINE)
                }
                LLMProvider.AUTO -> {
                    AppLogger.i("ChatRepo", "🤖 Auto mode: Attempting Groq with Ollama fallback")
                    callGroqWithFallback(actualModelName, chatMessages, groqApiKey)
                }
            }
            
            // Add provider notification to response
            val providerNotification = when (actualProviderUsed) {
                LLMProvider.GROQ_ONLINE -> "🌐 **Groq (Online)**"
                LLMProvider.OLLAMA_LOCAL -> "🏠 **Ollama (Local)**"
                LLMProvider.AUTO -> ""
            }
            
            val modelSwitchNotification = if (actualModelName != modelName) {
                " • Auto-switched to vision model: $actualModelName"
            } else {
                ""
            }
            
            val finalResponse = if (providerNotification.isNotEmpty() || modelSwitchNotification.isNotEmpty()) {
                "$providerNotification$modelSwitchNotification\n\n$aiResponse"
            } else {
                aiResponse
            }
            
            // In real app, save to database here
            // messageDao.insertMessage(userMessage)
            // messageDao.insertMessage(aiMessage)
            
            finalResponse
        } catch (e: Exception) {
            val attachmentText = if (attachments.isNotEmpty()) {
                "\n\n📎 **Attachments received (${attachments.size}):**\n" +
                attachments.joinToString("\n") { "• ${it.fileName} (${it.type})" }
            } else ""
            
            // Provide demo response for testing
            "🤖 **Demo Mode - Exception Caught**\n\n" +
            "Your message: \"$message\"$attachmentText\n\n" +
            "This is a test response since the AI service is not available.\n\n" +
            "Error details: ${e.javaClass.simpleName} - ${e.message ?: "Unknown error"}\n\n" +
            "**To get real AI responses:**\n" +
            "1. Install Ollama from https://ollama.ai\n" +
            "2. Run: `ollama pull codellama:13b-instruct`\n" +
            "3. Start: `ollama serve`\n\n" +
            "Your AI assistant will provide excellent help! 🚀"
        }
    }
    
    /**
     * Helper method to call Ollama API
     */
    private suspend fun callOllamaApi(modelName: String, chatMessages: List<ChatMessage>): String {
        val request = ChatRequest(
            model = modelName,
            messages = chatMessages
        )
        
        AppLogger.network("ChatRepo", "🚀 Calling Ollama API with ${chatMessages.size} messages")
        AppLogger.i("ChatRepo", "🎯 Using model: $modelName")

        return when (val response = ollamaApiClient.sendMessage(request)) {
            is ApiResult.Success -> {
                val aiResponse = response.data.message.content
                AppLogger.i("ChatRepo", "✅ Ollama response received: ${aiResponse.length} chars")
                AppLogger.d("ChatRepo", "Response preview: ${aiResponse.take(100)}...")
                aiResponse
            }
            is ApiResult.Error -> {
                AppLogger.e("ChatRepo", "❌ Ollama API error: ${response.message}")
                // For fallback scenarios, throw exception instead of returning error message
                throw Exception("Ollama API error: ${response.message}")
            }
        }
    }
    
    /**
     * Helper method to call Ollama API with error message handling (for direct Ollama calls)
     */
    private suspend fun callOllamaApiWithErrorHandling(modelName: String, chatMessages: List<ChatMessage>): String {
        return try {
            callOllamaApi(modelName, chatMessages)
        } catch (e: Exception) {
            handleOllamaError(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Helper method to call Groq API
     */
    private suspend fun callGroqApi(chatMessages: List<ChatMessage>, apiKey: String): String {
        AppLogger.network("ChatRepo", "🚀 Calling Groq API with ${chatMessages.size} messages")
        
        return try {
            var response = ""
            groqApiService.streamChat(chatMessages, apiKey = apiKey).collect { chunk ->
                response = chunk
            }
            
            AppLogger.i("ChatRepo", "✅ Groq response received: ${response.length} chars")
            AppLogger.d("ChatRepo", "Response preview: ${response.take(100)}...")
            response
        } catch (e: Exception) {
            AppLogger.e("ChatRepo", "❌ Groq API error: ${e.message}")
            handleGroqError(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Call Groq API with automatic fallback to Ollama in Auto mode
     */
    private suspend fun callGroqWithFallback(
        ollamaModelName: String,
        chatMessages: List<ChatMessage>,
        groqApiKey: String?
    ): Pair<String, LLMProvider> {
        // First, try Groq if API key is available
        if (groqApiKey != null) {
            try {
                AppLogger.i("ChatRepo", "🌐 Auto mode: Trying Groq API first")
                var response = ""
                groqApiService.streamChat(chatMessages, apiKey = groqApiKey).collect { chunk ->
                    response = chunk
                }
                
                AppLogger.i("ChatRepo", "✅ Groq success in Auto mode: ${response.length} chars")
                return Pair(response, LLMProvider.GROQ_ONLINE)
                
            } catch (e: Exception) {
                AppLogger.w("ChatRepo", "⚠️ Groq failed in Auto mode: ${e.message}")
                AppLogger.i("ChatRepo", "🔄 Auto mode: Falling back to Ollama")
                
                // Continue to Ollama fallback below
            }
        } else {
            AppLogger.i("ChatRepo", "🔑 Auto mode: No Groq API key, using Ollama")
        }
        
        // Fallback to Ollama
        try {
            val response = callOllamaApi(ollamaModelName, chatMessages)
            val fallbackNotification = if (groqApiKey != null) {
                "🔄 **Auto Fallback**: Groq failed, switched to Ollama\n\n"
            } else {
                ""
            }
            return Pair("$fallbackNotification$response", LLMProvider.OLLAMA_LOCAL)
            
        } catch (e: Exception) {
            AppLogger.e("ChatRepo", "❌ Both providers failed in Auto mode")
            val errorResponse = buildString {
                append("🤖 **Auto Mode Error**\n\n")
                append("Both AI providers are currently unavailable:\n\n")
                if (groqApiKey != null) {
                    append("• **Groq (Online)**: Failed to connect\n")
                } else {
                    append("• **Groq (Online)**: No API key configured\n")
                }
                append("• **Ollama (Local)**: ${e.message}\n\n")
                append("**Troubleshooting:**\n")
                append("1. Check your internet connection for Groq\n")
                append("2. Ensure Ollama is running locally\n")
                append("3. Try switching providers manually")
            }
            return Pair(errorResponse, LLMProvider.AUTO)
        }
    }
    
    /**
     * Handle Ollama API errors with helpful messages
     */
    private fun handleOllamaError(errorMessage: String): String {
        return when {
            errorMessage.contains("model") && errorMessage.contains("not found") -> {
                "🤖 **Model Not Found**\n\n" +
                "The requested model is not available on your Ollama server.\n\n" +
                "**To install models:**\n" +
                "1. `ollama pull llama3.2:latest` - Latest Llama model\n" +
                "2. `ollama pull codellama` - For coding tasks\n" +
                "3. `ollama list` - See all installed models\n\n" +
                "Please install a model and try again."
            }
            errorMessage.contains("Failed to connect") || 
            errorMessage.contains("Connection refused") || 
            errorMessage.contains("Unable to resolve host") ||
            errorMessage.contains("Network connection failed") -> {
                "🤖 **Ollama Server Not Available**\n\n" +
                "Cannot connect to your local Ollama server.\n\n" +
                "**To fix this:**\n" +
                "1. Install Ollama from https://ollama.ai\n" +
                "2. Start server: `ollama serve`\n" +
                "3. Pull a model: `ollama pull llama3.2:latest`\n\n" +
                "Make sure Ollama is running on localhost:11434"
            }
            else -> {
                "🤖 **Ollama Error**\n\n" +
                "There was an issue with the Ollama server.\n\n" +
                "Error: $errorMessage\n\n" +
                "Please check your Ollama installation and try again."
            }
        }
    }
    
    /**
     * Handle Groq API errors with helpful messages
     */
    private fun handleGroqError(errorMessage: String): String {
        return when {
            errorMessage.contains("401") || errorMessage.contains("unauthorized") -> {
                "🌐 **Groq API Key Invalid**\n\n" +
                "Your Groq API key appears to be invalid or expired.\n\n" +
                "**To fix this:**\n" +
                "1. Go to https://console.groq.com/keys\n" +
                "2. Generate a new API key\n" +
                "3. Update your API key in Settings\n\n" +
                "Falling back to local Ollama if available."
            }
            errorMessage.contains("429") || errorMessage.contains("rate limit") -> {
                "🌐 **Groq Rate Limit Reached**\n\n" +
                "You've exceeded the Groq API rate limit.\n\n" +
                "**Options:**\n" +
                "1. Wait a few minutes and try again\n" +
                "2. Switch to 'Offline (Ollama)' mode\n" +
                "3. Use 'Auto' mode to fallback automatically\n\n" +
                "Free tier: 6,000 requests/minute"
            }
            errorMessage.contains("No network") || errorMessage.contains("network") -> {
                "🌐 **No Internet Connection**\n\n" +
                "Cannot reach Groq API - no internet connection.\n\n" +
                "**Automatic fallback:**\n" +
                "Switching to local Ollama for offline operation.\n\n" +
                "Enable 'Auto' mode for seamless switching."
            }
            else -> {
                "🌐 **Groq API Error**\n\n" +
                "There was an issue with the Groq API.\n\n" +
                "Error: $errorMessage\n\n" +
                "Try switching to 'Offline (Ollama)' mode or check your internet connection."
            }
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
