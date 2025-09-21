package com.exodus.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(
    val id: String,
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val type: AttachmentType
) : Parcelable

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    UNKNOWN;
    
    companion object {
        fun fromMimeType(mimeType: String): AttachmentType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType.startsWith("text/") || 
                mimeType.startsWith("application/pdf") ||
                mimeType.startsWith("application/msword") ||
                mimeType.startsWith("application/vnd.openxmlformats") -> DOCUMENT
                else -> UNKNOWN
            }
        }
    }
}

fun Attachment.isImage(): Boolean = type == AttachmentType.IMAGE
fun Attachment.isDocument(): Boolean = type == AttachmentType.DOCUMENT