package com.exodus.utils

import android.content.Context
import android.net.Uri
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object DocumentProcessor {
    
    /**
     * Extracts text content from document attachments
     */
    fun extractTextFromDocuments(context: Context, attachments: List<Attachment>): List<DocumentContent> {
        AppLogger.i("DocumentProcessor", "🔄 Processing ${attachments.size} total attachments")
        
        // Debug: Log all attachment types
        attachments.forEachIndexed { index, attachment ->
            AppLogger.d("DocumentProcessor", "📋 Attachment $index: ${attachment.fileName} -> Type: ${attachment.type}, MIME: ${attachment.mimeType}")
        }
        
        val documentAttachments = attachments.filter { it.type == AttachmentType.DOCUMENT }
        AppLogger.i("DocumentProcessor", "📄 Found ${documentAttachments.size} document attachments to process")
        
        return documentAttachments.mapNotNull { attachment ->
            AppLogger.d("DocumentProcessor", "📑 Processing document: ${attachment.fileName} (${attachment.mimeType})")
            try {
                val textContent = extractTextFromDocument(context, attachment)
                if (textContent != null) {
                    AppLogger.i("DocumentProcessor", "✅ Successfully extracted text from ${attachment.fileName}: ${textContent.length} chars")
                    DocumentContent(
                        fileName = attachment.fileName,
                        mimeType = attachment.mimeType,
                        textContent = textContent
                    )
                } else {
                    AppLogger.w("DocumentProcessor", "❌ Failed to extract text from ${attachment.fileName} - returned null")
                    null
                }
            } catch (e: Exception) {
                AppLogger.e("DocumentProcessor", "❌ Exception processing document ${attachment.fileName}: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Extracts text from a single document
     */
    private fun extractTextFromDocument(context: Context, attachment: Attachment): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(attachment.uri) ?: return null
            
            when {
                // Plain text files - full extraction
                attachment.mimeType.startsWith("text/") ||
                attachment.fileName.endsWith(".txt", true) ||
                attachment.fileName.endsWith(".md", true) ||
                attachment.fileName.endsWith(".log", true) -> {
                    extractTextFromPlainText(inputStream)
                }
                
                // PDF files - guidance for user
                attachment.mimeType == "application/pdf" ||
                attachment.fileName.endsWith(".pdf", true) -> {
                    AppLogger.d("DocumentProcessor", "📄 Detected PDF file - providing guidance")
                    "📄 PDF Document: ${attachment.fileName} (${formatBytes(attachment.size)})\n\n" +
                    "I can see you've attached a PDF file. While I can't extract text from PDFs directly, " +
                    "I'd be happy to help if you could:\n" +
                    "• Copy and paste the relevant text from the PDF\n" +
                    "• Tell me what specific information you're looking for\n" +
                    "• Describe the content you'd like me to analyze\n\n" +
                    "What would you like to discuss about this PDF?"
                }
                
                // Word documents - guidance for user
                attachment.mimeType.contains("word") ||
                attachment.mimeType.contains("wordprocessingml") ||
                attachment.mimeType.contains("vnd.openxmlformats-officedocument") ||
                attachment.fileName.endsWith(".docx", true) ||
                attachment.fileName.endsWith(".doc", true) -> {
                    AppLogger.d("DocumentProcessor", "📝 Detected Word document - providing guidance")
                    "📝 **Word Document**: ${attachment.fileName} (${formatBytes(attachment.size)})\n\n" +
                    "I can see you've attached a Word document. While I can't extract text from Word files directly, " +
                    "I'd be happy to help if you could:\n\n" +
                    "• **Copy and paste** the relevant text from the document\n" +
                    "• **Save as plain text**: File → Save As → Plain Text (.txt) and attach that\n" +
                    "• **Tell me** what specific content you'd like me to analyze\n\n" +
                    "What would you like to discuss about this document?"
                }
                
                // Excel files - guidance for user
                attachment.mimeType.contains("spreadsheet") || 
                attachment.mimeType.contains("excel") ||
                attachment.mimeType.contains("sheet") ||
                attachment.fileName.endsWith(".xlsx", true) ||
                attachment.fileName.endsWith(".xls", true) -> {
                    AppLogger.d("DocumentProcessor", "📊 Detected Excel file - providing guidance")
                    "📊 Excel Spreadsheet: ${attachment.fileName} (${formatBytes(attachment.size)})\n\n" +
                    "I can see you've attached an Excel file. While I can't extract data from spreadsheets directly, " +
                    "I'd be happy to help if you could:\n" +
                    "• Copy and paste the relevant data from the spreadsheet\n" +
                    "• Export the data as a CSV or text file and attach that\n" +
                    "• Describe what analysis or help you need with the data\n\n" +
                    "What would you like to discuss about this spreadsheet?"
                }
                
                else -> {
                    AppLogger.d("DocumentProcessor", "❓ Unknown document type - providing general guidance")
                    "📎 Document: ${attachment.fileName} (${formatBytes(attachment.size)})\n" +
                    "Type: ${attachment.mimeType}\n\n" +
                    "I can see you've attached a file, but I can only read plain text files directly. " +
                    "For other file types, I'd be happy to help if you could:\n" +
                    "• Copy and paste the relevant content from the file\n" +
                    "• Convert the file to plain text format\n" +
                    "• Tell me what you'd like me to analyze or help with\n\n" +
                    "What would you like to discuss about this file?"
                }
            }
        } catch (e: Exception) {
            AppLogger.e("DocumentProcessor", "Error extracting text from document: ${e.message}", e)
            null
        }
    }
    
    /**
     * Extracts text from plain text files
     */
    private fun extractTextFromPlainText(inputStream: InputStream): String? {
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val content = reader.readText()
            inputStream.close()
            
            // Limit text size to prevent API overload (max 15KB of text)
            if (content.length > 15360) {
                AppLogger.w("DocumentProcessor", "📄 Text file too long (${content.length} chars), truncating to 15KB")
                content.take(15360) + "\n\n[Text truncated - original file was ${content.length} characters]"
            } else {
                content
            }
        } catch (e: Exception) {
            AppLogger.e("DocumentProcessor", "Error reading plain text file: ${e.message}")
            null
        }
    }
    
    /**
     * Formats file size in human-readable format
     */
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> "%.1f GB".format(gb)
            mb >= 1 -> "%.1f MB".format(mb)
            kb >= 1 -> "%.1f KB".format(kb)
            else -> "$bytes B"
        }
    }
}

/**
 * Data class to hold extracted document content
 */
data class DocumentContent(
    val fileName: String,
    val mimeType: String,
    val textContent: String
)