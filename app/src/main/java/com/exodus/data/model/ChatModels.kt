package com.exodus.data.model

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val images: List<String>? = null // Base64 encoded images for vision models
)

data class ChatResponse(
    val message: ChatMessage,
    val done: Boolean,
    val total_duration: Long? = null,
    val load_duration: Long? = null,
    val prompt_eval_count: Int? = null,
    val prompt_eval_duration: Long? = null,
    val eval_count: Int? = null,
    val eval_duration: Long? = null
)

data class ModelsResponse(
    val models: List<ModelInfo>
)

data class ModelInfo(
    val name: String,
    val size: Long,
    val digest: String,
    val modified_at: String
)
