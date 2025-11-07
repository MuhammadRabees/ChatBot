package com.example.chatbot

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.ChatRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatViewModel(private val repo: ChatRepository) : ViewModel() {

    // Callbacks
    var onBotReply: ((Message) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onBotThinking: ((String) -> Unit)? = null
    var onMessagesLoaded: ((List<Message>) -> Unit)? = null // to load previous chat
    var onSessionsLoaded: ((List<ChatSession>) -> Unit)? = null // For Drawer
    var onNewChatCreated: (() -> Unit)? = null

    private var currentSessionId: String? = null
    private var isFirstMessageInSession = true // Always open new chat when app run
    fun loadAllSessions() {
        viewModelScope.launch {
            val sessions = repo.loadAllSessions()
            withContext(Dispatchers.Main) {
                onSessionsLoaded?.invoke(sessions)
            }
        }
    }

    fun loadMessagesForSession(sessionId: String) {
        viewModelScope.launch {
            currentSessionId = sessionId
            isFirstMessageInSession = false
            val messages = repo.loadMessagesForSession(sessionId)
            withContext(Dispatchers.Main) {
                onMessagesLoaded?.invoke(messages)
            }
        }
    }

    // Function to start new chat when user press '+'
    fun startNewChat() {
        currentSessionId = null // Clear Session ID
        isFirstMessageInSession = true
        onNewChatCreated?.invoke() // Clear UI
    }

    fun sendMessage(userMessage: Message) {
        viewModelScope.launch {
            try {
                // Check whether the new session created or not
                if (currentSessionId == null) {
                    val newSession = repo.createNewSession()
                    currentSessionId = newSession.sessionId
                    repo.updateSessionTitleIfNeeded(newSession.sessionId, userMessage.text)
                    isFirstMessageInSession = false
                }

                val sessionId = currentSessionId!!
                userMessage.sessionId = sessionId
                repo.updateSessionAccessTime(sessionId)

                withContext(Dispatchers.Main) { onBotThinking?.invoke("Thinking...") }

                val botReplyMessage = repo.sendMessageToAi(userMessage)

                withContext(Dispatchers.Main) {
                    onBotReply?.invoke(botReplyMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace(); withContext(Dispatchers.Main) { onError?.invoke(e.localizedMessage ?: "Network error") }
            }
        }
    }

    fun sendMessageWithImage(userMessage: Message) {
        viewModelScope.launch {
            try {
                if (currentSessionId == null) {
                    val newSession = repo.createNewSession()
                    currentSessionId = newSession.sessionId
                    val title = userMessage.text.ifEmpty { "Image analysis" }
                    repo.updateSessionTitleIfNeeded(newSession.sessionId, title)
                    isFirstMessageInSession = false
                }

                val sessionId = currentSessionId!!
                userMessage.sessionId = sessionId
                repo.updateSessionAccessTime(sessionId)

                withContext(Dispatchers.Main) { onBotThinking?.invoke("Analyzing image...") }

                val botReplyMessage = repo.sendMessageWithImageToAi(userMessage)

                withContext(Dispatchers.Main) {
                    onBotReply?.invoke(botReplyMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace(); withContext(Dispatchers.Main) { onError?.invoke(e.localizedMessage ?: "Network error") }
            }
        }
    }
}