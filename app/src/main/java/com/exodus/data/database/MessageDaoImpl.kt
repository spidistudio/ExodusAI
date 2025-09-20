package com.exodus.data.database

import com.exodus.data.model.Message

class MessageDaoImpl : MessageDao {
    
    // Simple in-memory storage for VS Code compatibility
    private val messages = mutableListOf<Message>()
    
    override fun getAllMessages(): List<Message> {
        return messages.toList()
    }
    
    override fun getMessagesByModel(modelName: String): List<Message> {
        return messages.filter { it.modelName == modelName }
    }
    
    override fun insertMessage(message: Message) {
        messages.add(message)
    }
    
    override fun clearAllMessages() {
        messages.clear()
    }
    
    override fun clearMessagesForModel(modelName: String) {
        messages.removeAll { it.modelName == modelName }
    }
}
