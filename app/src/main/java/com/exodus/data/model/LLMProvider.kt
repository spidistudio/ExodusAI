package com.exodus.data.model

enum class LLMProvider(
    val displayName: String,
    val description: String,
    val requiresApiKey: Boolean,
    val requiresInternet: Boolean
) {
    AUTO(
        displayName = "Auto",
        description = "Smart switching: Online when available, offline as fallback",
        requiresApiKey = false,
        requiresInternet = false
    ),
    GROQ_ONLINE(
        displayName = "Online (Groq)",
        description = "Fast online AI with current knowledge",
        requiresApiKey = true,
        requiresInternet = true
    ),
    OLLAMA_LOCAL(
        displayName = "Offline (Ollama)",
        description = "Local AI for privacy and offline use",
        requiresApiKey = false,
        requiresInternet = false
    );

    companion object {
        fun getDefault() = AUTO
        
        fun fromString(value: String): LLMProvider {
            return values().find { it.name == value } ?: getDefault()
        }
    }
}