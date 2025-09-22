package com.exodus.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exodus.data.model.AIModel
import com.exodus.data.model.Attachment
import com.exodus.data.model.LLMProvider
import com.exodus.data.model.Message
import com.exodus.ui.theme.*
import com.exodus.ui.components.MarkdownText
import com.exodus.ui.components.AttachmentPreviewGrid
import com.exodus.utils.AttachmentManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val attachmentManager = remember { AttachmentManager(context) }
    
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val configuration = LocalConfiguration.current
    
    var messageText by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    val listState = rememberLazyListState()
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val attachment = attachmentManager.createAttachment(selectedUri)
            if (attachment != null && attachmentManager.isValidAttachment(attachment)) {
                selectedAttachments = selectedAttachments + attachment
            }
        }
    }
    
    // Responsive dimensions based on screen size
    val screenWidth = configuration.screenWidthDp.dp
    val maxMessageWidth = when {
        screenWidth < 360.dp -> screenWidth * 0.85f  // Small phones
        screenWidth < 600.dp -> screenWidth * 0.80f  // Regular phones
        screenWidth < 840.dp -> screenWidth * 0.75f  // Large phones/small tablets
        else -> screenWidth * 0.70f                   // Tablets and larger
    }
    
    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp  // Small phones
        screenWidth < 600.dp -> 16.dp  // Regular phones
        else -> 24.dp                  // Tablets and larger
    }
    
    // Set default model on first launch
    LaunchedEffect(Unit) {
        // Ensure we have a default model selected
        if (uiState.selectedModel == null) {
            val defaultModel = AIModel("llama3.2:latest", "Llama 3.2 Latest", "2.0 GB", true)
            viewModel.selectModel(defaultModel)
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with model selection
        TopAppBar(
            title = { 
                Text(
                    "Exodus Chat",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
                IconButton(onClick = { viewModel.clearMessages() }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear Chat"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Simple Model Indicator (replacing dropdown)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI Model: CodeLlama Custom 13B",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Your personal private chat bot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Model Ready",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // AI Provider Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI Provider",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var expanded by remember { mutableStateOf(false) }
                val providers = listOf(
                    LLMProvider.AUTO,
                    LLMProvider.GROQ_ONLINE,
                    LLMProvider.OLLAMA_LOCAL
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedProvider.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Provider Mode") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = provider.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (provider == uiState.selectedProvider) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = provider.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectProvider(provider)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Show API key status for Groq
                if (uiState.selectedProvider == LLMProvider.GROQ_ONLINE || uiState.selectedProvider == LLMProvider.AUTO) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hasApiKey = !uiState.groqApiKey.isNullOrBlank()
                        Icon(
                            imageVector = if (hasApiKey) Icons.Default.CheckCircle else Icons.Default.Clear,
                            contentDescription = if (hasApiKey) "API Key Configured" else "API Key Missing",
                            tint = if (hasApiKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!uiState.groqApiKey.isNullOrBlank()) "Groq API Key: Configured" else "Groq API Key: Missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!uiState.groqApiKey.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    
                    if (uiState.groqApiKey.isNullOrBlank() && (uiState.selectedProvider == LLMProvider.GROQ_ONLINE || uiState.selectedProvider == LLMProvider.AUTO)) {
                        Text(
                            text = "Configure API key in Settings to use Groq (Online)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    maxWidth = maxMessageWidth
                )
            }
            
            if (uiState.isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Message input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Attachment preview
                if (selectedAttachments.isNotEmpty()) {
                    AttachmentPreviewGrid(
                        attachments = selectedAttachments,
                        onRemoveAttachment = { attachment ->
                            selectedAttachments = selectedAttachments.filter { it.id != attachment.id }
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Attach button
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        enabled = uiState.selectedModel != null && !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Attach File",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { 
                            Text(
                                "Type your message...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        enabled = uiState.selectedModel != null && !uiState.isLoading
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                                viewModel.sendMessage(messageText, selectedAttachments)
                                messageText = ""
                                selectedAttachments = emptyList()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    maxWidth: androidx.compose.ui.unit.Dp = 280.dp
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isDarkTheme = isSystemInDarkTheme()
    
    // Theme-aware colors
    val userBubbleColor = if (isDarkTheme) DarkUserMessageColor else LightUserMessageColor
    val aiBubbleColor = if (isDarkTheme) DarkAIMessageColor else LightAIMessageColor
    val userTextColor = if (isDarkTheme) DarkUserTextColor else LightUserTextColor
    val aiTextColor = if (isDarkTheme) DarkAITextColor else LightAITextColor
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = maxWidth),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) userBubbleColor else aiBubbleColor
            ),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromUser) 18.dp else 6.dp,
                bottomEnd = if (message.isFromUser) 6.dp else 18.dp
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
            ) {
                // Display attachments first for user messages
                if (message.isFromUser && message.attachments.isNotEmpty()) {
                    AttachmentPreviewGrid(
                        attachments = message.attachments,
                        onRemoveAttachment = { /* No-op for sent messages */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (message.isFromUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = userTextColor,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                } else {
                    MarkdownText(
                        text = message.content,
                        color = aiTextColor,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = dateFormat.format(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (message.isFromUser) userTextColor else aiTextColor).copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = AIMessageColor
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if ((index % 3) == 0) 1f else 0.3f,
                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(600),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                Color.White.copy(alpha = alpha),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
    }
}
