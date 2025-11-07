package com.example.chatbot

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// This class will save every new chat
@Entity
data class ChatSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(), // Unique ID for every chat
    var title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    var lastAccessed: Long = System.currentTimeMillis()
)