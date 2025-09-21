package com.exodus.di

import android.content.Context
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.database.MessageDao
import com.exodus.data.database.ExodusDatabase
import com.exodus.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOllamaApiClient(): OllamaApiClient {
        return OllamaApiClient("http://192.168.0.115:11434")
    }

    fun provideDatabase(context: Context): ExodusDatabase {
        return ExodusDatabase.getDatabase(context)
    }

    fun provideMessageDao(database: ExodusDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(
        ollamaApiClient: OllamaApiClient,
        messageDao: MessageDao
    ): ChatRepository {
        return ChatRepository(ollamaApiClient, messageDao)
    }
}
