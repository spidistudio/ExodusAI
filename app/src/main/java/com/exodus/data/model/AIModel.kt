package com.exodus.data.model

data class AIModel(
    val name: String,
    val displayName: String,
    val size: String = "",
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val description: String = ""
)
