package com.exodus.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.exodus.data.model.AIModel
import com.exodus.data.model.Attachment
import com.exodus.data.model.Message
import com.exodus.data.repository.ChatRepository
import java.util.Date

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _availableModels = MutableStateFlow<List<AIModel>>(emptyList())
    val availableModels: StateFlow<List<AIModel>> = _availableModels.asStateFlow()

    init {
        loadAvailableModels()
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                val allMessages = chatRepository.getAllMessages()
                val selectedModel = _uiState.value.selectedModel?.name
                _messages.value = if (selectedModel != null) {
                    allMessages.filter { it.modelName == selectedModel }
                } else {
                    allMessages
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load messages: ${e.message}"
                )
            }
        }
    }

    fun sendMessage(content: String, attachments: List<Attachment> = emptyList()) {
        val selectedModel = _uiState.value.selectedModel ?: return

        if (content.isBlank() && attachments.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Create and add user message immediately
                val userMessage = Message(
                    content = content,
                    isFromUser = true,
                    timestamp = Date(),
                    modelName = selectedModel.name,
                    attachments = attachments
                )
                
                val currentMessages = _messages.value.toMutableList()
                currentMessages.add(userMessage)
                _messages.value = currentMessages

                // Send to API and get response
                val response = chatRepository.sendMessage(
                    message = content,
                    modelName = selectedModel.name,
                    conversationHistory = _messages.value,
                    attachments = attachments
                )
                
                // Create and add AI response
                val aiMessage = Message(
                    content = response,
                    isFromUser = false,
                    timestamp = Date(),
                    modelName = selectedModel.name
                )
                
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(aiMessage)
                _messages.value = updatedMessages
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun selectModel(model: AIModel) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        loadMessages() // Refresh messages for the new model
    }

    fun loadAvailableModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingModels = true)

            try {
                val models = chatRepository.getAvailableModels()
                _availableModels.value = models
                _uiState.value = _uiState.value.copy(
                    isLoadingModels = false,
                    selectedModel = models.firstOrNull() ?: _uiState.value.selectedModel
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingModels = false,
                    errorMessage = e.message ?: "Failed to load models"
                )
            }
        }
    }

    fun downloadModel(modelName: String) {
        viewModelScope.launch {
            try {
                chatRepository.downloadModel(modelName)
                // Refresh models after download attempt
                loadAvailableModels()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to download model"
                )
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            val selectedModel = _uiState.value.selectedModel
            if (selectedModel != null) {
                chatRepository.clearMessagesForModel(selectedModel.name)
            } else {
                chatRepository.clearMessages()
            }
            loadMessages()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ChatUiState(
    val selectedModel: AIModel? = null,
    val isLoading: Boolean = false,
    val isLoadingModels: Boolean = false,
    val errorMessage: String? = null
)
