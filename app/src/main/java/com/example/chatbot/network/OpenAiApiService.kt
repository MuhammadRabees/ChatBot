package com.example.chatbot.network

import retrofit2.http.Body
import retrofit2.http.POST

data class Choice(val index: Int, val message: ChatMessage)
data class ChatResponse(val id: String?, val choices: List<Choice>?, val error: Map<String, Any>?)
data class ChatMessage(val role: String, val content: String)

sealed class VisionMessageContent {
    data class TextContent(val text: String) : VisionMessageContent() {
        val type: String = "text"
    }
    data class ImageContent(val image_url: ImageUrl) : VisionMessageContent() {
        val type: String = "image_url"
    }
}
// Image URL object
data class ImageUrl(val url: String, val detail: String = "low")
data class VisionChatMessage(val role: String, val content: List<VisionMessageContent>)
data class VisionChatRequest(val model: String, val messages: List<VisionChatMessage>, val max_tokens: Int = 1000)

// API SERVICE INTERFACE
interface OpenAiApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        // Body ab naya VisionChatRequest object legi
        @Body request: VisionChatRequest
    ): ChatResponse
}