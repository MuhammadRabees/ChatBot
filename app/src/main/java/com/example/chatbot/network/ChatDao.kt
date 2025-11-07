package com.example.chatbot.network

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chatbot.ChatSession
import com.example.chatbot.Message

@Dao
interface ChatDao {
    // --- Session Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("SELECT * FROM ChatSession WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSession?

    @Query("SELECT * FROM ChatSession ORDER BY lastAccessed DESC")
    suspend fun getAllSessions(): List<ChatSession> // History panel ke liye

    // --- Message Queries ---
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM Message WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: String): List<Message>
}