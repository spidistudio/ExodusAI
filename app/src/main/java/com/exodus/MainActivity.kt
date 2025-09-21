package com.exodus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.exodus.data.api.OllamaApiClient
import com.exodus.data.database.MessageDaoImpl
import com.exodus.data.repository.ChatRepository
import com.exodus.ui.chat.ChatScreen
import com.exodus.ui.chat.ChatViewModel
import com.exodus.ui.screens.SettingsScreen
import com.exodus.ui.theme.ExodusTheme
import com.exodus.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manual dependency injection since we removed Hilt for VS Code compatibility
        val ollamaApiClient = OllamaApiClient()
        val messageDao = MessageDaoImpl() // Placeholder implementation
        val chatRepository = ChatRepository(ollamaApiClient, messageDao)
        val chatViewModel = ChatViewModel(chatRepository)
        val settingsViewModel = SettingsViewModel()
        
        setContent {
            var currentScreen by remember { mutableStateOf("chat") }
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            
            ExodusTheme(
                darkTheme = settingsUiState.isDarkMode
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    when (currentScreen) {
                        "chat" -> {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onSettingsClick = { currentScreen = "settings" }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBackClick = { currentScreen = "chat" }
                            )
                        }
                    }
                }
            }
        }
    }
}
