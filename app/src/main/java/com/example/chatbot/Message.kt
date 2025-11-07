package com.example.chatbot

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(UriTypeConverter::class)
data class Message (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0, // Primary key for database

    var sessionId: String, // Link every message to session

    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: Uri? = null
)