package com.exodus.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Centralized logging system for debugging network connectivity and app behavior
 */
object AppLogger {
    private const val MAX_LOGS = 1000 // Keep only last 1000 log entries
    
    data class LogEntry(
        val timestamp: String,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )
    
    enum class LogLevel(val displayName: String, val color: String) {
        DEBUG("DEBUG", "#808080"),
        INFO("INFO", "#0066CC"),
        WARNING("WARN", "#FF8C00"),
        ERROR("ERROR", "#CC0000"),
        NETWORK("NET", "#008000")
    }
    
    private val logs = ConcurrentLinkedQueue<LogEntry>()
    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
        Log.d(tag, message, throwable)
    }
    
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
        Log.i(tag, message, throwable)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARNING, tag, message, throwable)
        Log.w(tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
        Log.e(tag, message, throwable)
    }
    
    fun network(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.NETWORK, tag, message, throwable)
        Log.d(tag, "[NETWORK] $message", throwable)
    }
    
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val timestamp = dateFormat.format(Date())
        val entry = LogEntry(timestamp, level, tag, message, throwable)
        
        logs.offer(entry)
        
        // Keep only recent logs
        while (logs.size > MAX_LOGS) {
            logs.poll()
        }
        
        // Update flow
        _logsFlow.value = logs.toList()
    }
    
    fun getAllLogs(): List<LogEntry> = logs.toList()
    
    fun clearLogs() {
        logs.clear()
        _logsFlow.value = emptyList()
    }
    
    fun getLogsAsString(): String {
        return logs.joinToString("\n") { entry ->
            val throwableText = entry.throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
            "${entry.timestamp} [${entry.level.displayName}] ${entry.tag}: ${entry.message}$throwableText"
        }
    }
    
    // Initialize with app start log
    init {
        i("AppLogger", "🚀 ExodusAI Logging System Initialized")
        i("AppLogger", "📱 App Version: 1.16")
        i("AppLogger", "🔧 Debug logging enabled for network troubleshooting")
    }
}