package com.exodus.data.database

import androidx.room.*
import com.exodus.data.model.Message

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE modelName = :modelName ORDER BY timestamp DESC")
    fun getMessagesByModel(modelName: String): List<Message>

    @Insert
    fun insertMessage(message: Message)

    @Query("DELETE FROM messages")
    fun clearAllMessages()

    @Query("DELETE FROM messages WHERE modelName = :modelName")
    fun clearMessagesForModel(modelName: String)
}
