package com.exodus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.database.MessageDaoImpl
import com.exodus.data.repository.ChatRepository
import com.exodus.ui.chat.ChatScreen
import com.exodus.ui.chat.ChatViewModel
import com.exodus.ui.theme.ExodusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manual dependency injection since we removed Hilt for VS Code compatibility
        val ollamaApiClient = OllamaApiClient()
        val messageDao = MessageDaoImpl() // Placeholder implementation
        val chatRepository = ChatRepository(ollamaApiClient, messageDao)
        val chatViewModel = ChatViewModel(chatRepository)
        
        setContent {
            ExodusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatScreen(viewModel = chatViewModel)
                }
            }
        }
    }
}
