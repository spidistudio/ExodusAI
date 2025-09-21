package com.exodus.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import java.util.UUID

class AttachmentManager(private val context: Context) {
    
    fun createAttachment(uri: Uri): Attachment? {
        return try {
            val contentResolver = context.contentResolver
            
            // Get file details from ContentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var fileName = "unknown_file"
            var fileSize = 0L
            
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                if (it.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex) ?: "unknown_file"
                    }
                    if (sizeIndex != -1) {
                        fileSize = it.getLong(sizeIndex)
                    }
                }
            }
            
            // Get MIME type
            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName)
            
            // Debug: Log the MIME type for troubleshooting
            AppLogger.i("AttachmentManager", "🔍 File: $fileName, MIME type: $mimeType")
            
            // Determine attachment type
            val attachmentType = AttachmentType.fromMimeType(mimeType)
            
            // Debug: Log the determined attachment type
            AppLogger.i("AttachmentManager", "📋 Attachment type determined: $attachmentType for $fileName")
            
            Attachment(
                id = UUID.randomUUID().toString(),
                uri = uri,
                fileName = fileName,
                mimeType = mimeType,
                size = fileSize,
                type = attachmentType
            )
        } catch (e: Exception) {
            AppLogger.e("AttachmentManager", "Error creating attachment", e)
            null
        }
    }
    
    private fun getMimeTypeFromExtension(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) 
            ?: "application/octet-stream"
    }
    
    fun getFileContent(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            AppLogger.e("AttachmentManager", "Error reading file content", e)
            null
        }
    }
    
    fun isValidAttachment(attachment: Attachment): Boolean {
        // Check file size (limit to 10MB for now)
        val maxSize = 10 * 1024 * 1024 // 10MB
        if (attachment.size > maxSize) {
            AppLogger.w("AttachmentManager", "File too large: ${attachment.size} bytes")
            return false
        }
        
        // Check if file still exists
        return try {
            context.contentResolver.openInputStream(attachment.uri)?.use { 
                true 
            } ?: false
        } catch (e: Exception) {
            AppLogger.w("AttachmentManager", "File not accessible: ${attachment.uri}")
            false
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}