package com.exodus.data.database

import androidx.room.TypeConverter
import com.exodus.data.model.Attachment
import com.exodus.data.model.AttachmentType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.net.Uri
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAttachmentList(attachments: List<Attachment>?): String? {
        if (attachments == null) return null
        
        // Convert attachments to a simplified format for storage
        val simplifiedAttachments = attachments.map { attachment ->
            mapOf(
                "id" to attachment.id,
                "uri" to attachment.uri.toString(),
                "fileName" to attachment.fileName,
                "mimeType" to attachment.mimeType,
                "size" to attachment.size,
                "type" to attachment.type.name
            )
        }
        return Gson().toJson(simplifiedAttachments)
    }

    @TypeConverter
    fun toAttachmentList(attachmentsString: String?): List<Attachment> {
        if (attachmentsString == null) return emptyList()
        
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val simplifiedAttachments: List<Map<String, Any>> = Gson().fromJson(attachmentsString, type)
        
        return simplifiedAttachments.map { map ->
            Attachment(
                id = map["id"] as String,
                uri = Uri.parse(map["uri"] as String),
                fileName = map["fileName"] as String,
                mimeType = map["mimeType"] as String,
                size = (map["size"] as Double).toLong(), // Gson converts numbers to Double
                type = AttachmentType.valueOf(map["type"] as String)
            )
        }
    }
}
