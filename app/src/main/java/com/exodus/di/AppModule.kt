package com.exodus.di

import android.content.Context
import com.exodus.data.api.OllamaApiService
import com.exodus.data.database.MessageDao
import com.exodus.data.database.ExodusDatabase
import com.exodus.data.repository.ChatRepository

object AppModule {

    fun provideOllamaApiService(): OllamaApiService {
        return OllamaApiService("http://localhost:11434")
    }

    fun provideDatabase(context: Context): ExodusDatabase {
        return ExodusDatabase.getDatabase(context)
    }

    fun provideMessageDao(database: ExodusDatabase): MessageDao {
        return database.messageDao()
    }
    
    fun provideChatRepository(
        ollamaApiService: OllamaApiService,
        messageDao: MessageDao
    ): ChatRepository {
        return ChatRepository(ollamaApiService, messageDao)
    }
}
