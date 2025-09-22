package com.exodus.di

import android.content.Context
import com.exodus.data.api.GroqApiService
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.database.MessageDao
import com.exodus.data.database.ExodusDatabase
import com.exodus.data.repository.ChatRepository
import com.exodus.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext as HiltApplicationContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOllamaApiClient(): OllamaApiClient {
        return OllamaApiClient("http://192.168.0.115:11434")
    }

    @Provides
    @Singleton
    fun provideGroqApiService(client: OkHttpClient): GroqApiService {
        return GroqApiService(client)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @HiltApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
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
        @HiltApplicationContext context: Context,
        ollamaApiClient: OllamaApiClient,
        groqApiService: GroqApiService,
        messageDao: MessageDao
    ): ChatRepository {
        return ChatRepository(context, ollamaApiClient, groqApiService, messageDao)
    }
}
