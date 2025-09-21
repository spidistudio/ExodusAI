package com.exodus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exodus.utils.AppLogger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val logs by AppLogger.logsFlow.collectAsState()
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Debug Logs")
                        Text(
                            "${logs.size} entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val logsText = AppLogger.getLogsAsString()
                            clipboardManager.setText(AnnotatedString(logsText))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("📋 Logs copied to clipboard!")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Logs")
                    }
                    IconButton(
                        onClick = {
                            AppLogger.clearLogs()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🗑️ Logs cleared")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "📋 Debug Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "This screen shows real-time logs of network operations. Use the copy button to share logs for troubleshooting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logs display
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📝",
                            fontSize = 48.sp
                        )
                        Text(
                            "No logs yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Send a message to see network logs appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { logEntry ->
                            LogEntryItem(logEntry = logEntry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(logEntry: AppLogger.LogEntry) {
    val backgroundColor = when (logEntry.level) {
        AppLogger.LogLevel.ERROR -> Color(0xFFFFEBEE)
        AppLogger.LogLevel.WARNING -> Color(0xFFFFF3E0)
        AppLogger.LogLevel.NETWORK -> Color(0xFFE8F5E8)
        AppLogger.LogLevel.INFO -> Color(0xFFE3F2FD)
        AppLogger.LogLevel.DEBUG -> Color(0xFFF5F5F5)
    }
    
    val textColor = when (logEntry.level) {
        AppLogger.LogLevel.ERROR -> Color(0xFFD32F2F)
        AppLogger.LogLevel.WARNING -> Color(0xFFF57C00)
        AppLogger.LogLevel.NETWORK -> Color(0xFF388E3C)
        AppLogger.LogLevel.INFO -> Color(0xFF1976D2)
        AppLogger.LogLevel.DEBUG -> Color(0xFF616161)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    logEntry.timestamp,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    "[${logEntry.level.displayName}]",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    logEntry.tag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            Text(
                logEntry.message,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                lineHeight = 14.sp
            )
            logEntry.throwable?.let { throwable ->
                Text(
                    throwable.stackTraceToString().take(300) + if (throwable.stackTraceToString().length > 300) "..." else "",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = textColor.copy(alpha = 0.6f),
                    lineHeight = 12.sp
                )
            }
        }
    }
}