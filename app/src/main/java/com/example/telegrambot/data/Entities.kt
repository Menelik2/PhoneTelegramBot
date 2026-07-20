package com.example.telegrambot.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "update_id")
    val updateId: Long,
    
    @ColumnInfo(name = "type")
    val type: MessageType,
    
    @ColumnInfo(name = "from_user")
    val from: String,
    
    @ColumnInfo(name = "chat_id")
    val chatId: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "raw_data")
    val rawData: String? = null,
    
    @ColumnInfo(name = "processed")
    val processed: Boolean = false
)

enum class MessageType {
    TEXT,
    COMMAND,
    CALLBACK,
    INLINE_QUERY
}
