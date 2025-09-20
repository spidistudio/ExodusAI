package com.exodus.ui.chat

import com.exodus.data.model.AIModel
import com.exodus.data.model.Message
import com.exodus.data.repository.ChatRepository

class ChatViewModel(
    private val chatRepository: ChatRepository
) {

    private var _uiState = ChatUiState()
    val uiState: ChatUiState get() = _uiState

    private var _messages = listOf<Message>()
    val messages: List<Message> get() = _messages

    private var _availableModels = listOf<AIModel>()
    val availableModels: List<AIModel> get() = _availableModels

    init {
        loadAvailableModels()
        observeMessages()
    }

    private fun observeMessages() {
        // In a real app, this would observe database changes
        val allMessages = chatRepository.getAllMessages()
        _messages = allMessages.filter { it.modelName == _uiState.selectedModel?.name }
    }

    fun sendMessage(content: String) {
        val selectedModel = _uiState.selectedModel ?: return

        if (content.isBlank()) return

        _uiState = _uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        try {
            val response = chatRepository.sendMessage(
                message = content,
                modelName = selectedModel.name,
                conversationHistory = _messages
            )
            
            _uiState = _uiState.copy(
                isLoading = false,
                errorMessage = null
            )
            // Refresh messages
            observeMessages()
        } catch (e: Exception) {
            _uiState = _uiState.copy(
                isLoading = false,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    fun selectModel(model: AIModel) {
        _uiState = _uiState.copy(selectedModel = model)
        observeMessages() // Refresh messages for the new model
    }

    fun loadAvailableModels() {
        _uiState = _uiState.copy(isLoadingModels = true)

        try {
            val models = chatRepository.getAvailableModels()
            _availableModels = models
            _uiState = _uiState.copy(
                isLoadingModels = false,
                selectedModel = models.firstOrNull() ?: _uiState.selectedModel
            )
        } catch (e: Exception) {
            _uiState = _uiState.copy(
                isLoadingModels = false,
                errorMessage = e.message ?: "Failed to load models"
            )
        }
    }

    fun downloadModel(modelName: String) {
        try {
            val result = chatRepository.downloadModel(modelName)
            // Refresh models after download attempt
            loadAvailableModels()
        } catch (e: Exception) {
            _uiState = _uiState.copy(
                errorMessage = e.message ?: "Failed to download model"
            )
        }
    }

    fun clearMessages() {
        val selectedModel = _uiState.selectedModel
        if (selectedModel != null) {
            chatRepository.clearMessagesForModel(selectedModel.name)
        } else {
            chatRepository.clearMessages()
        }
        observeMessages()
    }

    fun clearError() {
        _uiState = _uiState.copy(errorMessage = null)
    }
}

data class ChatUiState(
    val selectedModel: AIModel? = null,
    val isLoading: Boolean = false,
    val isLoadingModels: Boolean = false,
    val errorMessage: String? = null
)
