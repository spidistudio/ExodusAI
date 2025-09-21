package com.exodus.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageEncoder {
    
    /**
     * Converts image attachments to base64 strings for Ollama vision API
     */
    fun encodeImageAttachments(context: Context, attachments: List<Attachment>): List<String> {
        AppLogger.i("ImageEncoder", "🔄 Processing ${attachments.size} total attachments")
        
        // Debug: Log all attachment types
        attachments.forEachIndexed { index, attachment ->
            AppLogger.d("ImageEncoder", "🖼️ Attachment $index: ${attachment.fileName} -> Type: ${attachment.type}, MIME: ${attachment.mimeType}")
        }
        
        val imageAttachments = attachments.filter { it.type == AttachmentType.IMAGE }
        AppLogger.i("ImageEncoder", "🖼️ Found ${imageAttachments.size} image attachments to encode")
        
        return imageAttachments
            .mapNotNull { attachment ->
                AppLogger.d("ImageEncoder", "📸 Encoding image: ${attachment.fileName} (${attachment.mimeType})")
                try {
                    val encodedImage = encodeImageToBase64(context, attachment.uri)
                    if (encodedImage != null) {
                        AppLogger.i("ImageEncoder", "✅ Successfully encoded ${attachment.fileName}")
                    } else {
                        AppLogger.w("ImageEncoder", "❌ Failed to encode ${attachment.fileName} - returned null")
                    }
                    encodedImage
                } catch (e: Exception) {
                    AppLogger.e("ImageEncoder", "❌ Exception encoding image ${attachment.fileName}: ${e.message}")
                    null
                }
            }
    }
    
    /**
     * Encodes a single image URI to base64 string
     */
    private fun encodeImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(imageUri) ?: return null
            
            // Decode the image
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                AppLogger.e("ImageEncoder", "Failed to decode bitmap from URI: $imageUri")
                return null
            }
            
            // Resize image if it's too large (max 1024x1024 for efficient processing)
            val resizedBitmap = resizeImageIfNeeded(bitmap, 1024, 1024)
            
            // Convert to base64
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            
            val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            AppLogger.d("ImageEncoder", "Encoded image: ${imageBytes.size} bytes -> ${base64String.length} chars")
            
            // Clean up
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()
            
            base64String
        } catch (e: Exception) {
            AppLogger.e("ImageEncoder", "Error encoding image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Resizes bitmap if it exceeds the maximum dimensions
     */
    private fun resizeImageIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            val newW = minOf(width, maxWidth)
            val newH = (newW / aspectRatio).toInt()
            Pair(newW, newH)
        } else {
            // Portrait or square
            val newH = minOf(height, maxHeight)
            val newW = (newH * aspectRatio).toInt()
            Pair(newW, newH)
        }
        
        AppLogger.d("ImageEncoder", "Resizing image from ${width}x${height} to ${newWidth}x${newHeight}")
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}