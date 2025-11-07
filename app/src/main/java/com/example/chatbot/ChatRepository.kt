package com.example.chatbot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import com.example.chatbot.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ChatRepository(
    private val apiKey: String,
    private val context: Context,
    private val dao: ChatDao // Database Access
) {
    private val service = RetrofitInstance.create(apiKey)

    // New DataBase Function
    suspend fun loadMessagesForSession(sessionId: String): List<Message> {
        return dao.getMessagesForSession(sessionId)
    }

    suspend fun loadAllSessions(): List<ChatSession> {
        return dao.getAllSessions()
    }

    suspend fun createNewSession(): ChatSession {
        val newSession = ChatSession()
        dao.insertSession(newSession)
        return newSession
    }

    suspend fun updateSessionTitleIfNeeded(sessionId: String, firstMessage: String) {
        val session = dao.getSessionById(sessionId)
        if (session != null && session.title == "New Chat") {
            session.title = firstMessage.take(50) // Set History title upto 50 characters
            dao.updateSession(session)
        }
    }

   // This Function will update the last Access time of Session
    suspend fun updateSessionAccessTime(sessionId: String) {
        val session = dao.getSessionById(sessionId)
        if (session != null) {
            session.lastAccessed = System.currentTimeMillis()
            dao.updateSession(session)
        }
    }

     // This function send message to AI and AI will reply to it
    suspend fun sendMessageToAi(userMessage: Message): Message {
        // 1. Save user msg in databases
        dao.insertMessage(userMessage)

        // 2. API Call (only for text)
        val replyText = withContext(Dispatchers.IO) {
            try {
                val textContent = VisionMessageContent.TextContent(userMessage.text)
                val message = VisionChatMessage("user", listOf(textContent))
                val request = VisionChatRequest(model = "gpt-4o", messages = listOf(message))
                val response = service.createChatCompletion(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Log.e("NETWORK_ERROR", e.message.toString())
                "Network error: ${e.localizedMessage}"
            }
        }

        // 3. Save bot response in database
        val botMessage = Message(
            sessionId = userMessage.sessionId, // Same session ID
            text = replyText,
            isUser = false,
            imageUri = null
        )
        dao.insertMessage(botMessage)
        return botMessage
    }

    suspend fun sendMessageWithImageToAi(userMessage: Message): Message {

        dao.insertMessage(userMessage)

        // 2. API Call (image + text)
        val replyText = withContext(Dispatchers.IO) {
            try {
                val base64Image = imageUriToBase64(userMessage.imageUri!!)
                if (base64Image == null) return@withContext "Error: Could not process image."

                val contentList = mutableListOf<VisionMessageContent>()
                contentList.add(VisionMessageContent.TextContent(userMessage.text))
                val imageUrl = ImageUrl("data:image/jpeg;base64,$base64Image")
                contentList.add(VisionMessageContent.ImageContent(imageUrl))

                val message = VisionChatMessage("user", contentList)
                val request = VisionChatRequest(model = "gpt-4o", messages = listOf(message))

                val response = service.createChatCompletion(request)
                handleApiResponse(response)
            } catch (e: Exception) {
                Log.e("NETWORK_ERROR", e.message.toString())
                "Network error: ${e.localizedMessage}"
            }
        }

        val botMessage = Message(
            sessionId = userMessage.sessionId, // Same session ID
            text = replyText,
            isUser = false,
            imageUri = null
        )
        dao.insertMessage(botMessage)
        return botMessage
    }

    private fun handleApiResponse(response: ChatResponse): String {
        if (response.error != null) { Log.e("API_ERROR", response.error.toString()); return "Error: ${response.error["message"] ?: "Unknown API error"}" }
        return response.choices?.firstOrNull()?.message?.content ?: "Sorry, I couldn’t get a reply."
    }
    private fun imageUriToBase64(uri: Uri): String? {
        return try {
            val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else { MediaStore.Images.Media.getBitmap(context.contentResolver, uri) }
            val scaledBitmap = getScaledBitmap(originalBitmap, 1024)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) { Log.e("Base64Error", "Failed to convert image to Base64", e); null }
    }
    private fun getScaledBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width; val originalHeight = bitmap.height
        if (originalWidth <= maxDimension && originalHeight <= maxDimension) { return bitmap }
        val newWidth: Int; val newHeight: Int
        if (originalWidth > originalHeight) {
            newWidth = maxDimension; newHeight = (originalHeight.toFloat() / originalWidth.toFloat() * maxDimension).roundToInt()
        } else {
            newHeight = maxDimension; newWidth = (originalWidth.toFloat() / originalHeight.toFloat() * maxDimension).roundToInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}