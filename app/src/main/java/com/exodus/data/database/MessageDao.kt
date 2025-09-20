package com.exodus.data.database

import com.exodus.data.model.Message

// Simple interface without Room annotations for VS Code compatibility
interface MessageDao {
    fun getAllMessages(): List<Message>

    fun getMessagesByModel(modelName: String): List<Message>

    fun insertMessage(message: Message)

    fun clearAllMessages()

    fun clearMessagesForModel(modelName: String)
}
