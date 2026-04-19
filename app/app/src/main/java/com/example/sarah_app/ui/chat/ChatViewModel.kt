package com.example.sarah_app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.ChatRepository
import com.example.sarah_app.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val role: String,
    val content: String,
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sessionId: String = UUID.randomUUID().toString(),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val sessionCreatedAt = System.currentTimeMillis()

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        val userMsg = ChatMessage(role = "user", content = text)
        val updatedMessages = _uiState.value.messages + userMsg

        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            inputText = "",
            isLoading = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            val preview = text.take(60)
            chatRepository.ensureSessionExists(sessionId, preview, sessionCreatedAt)
            chatRepository.saveMessage(sessionId, "user", text)

            val serverUrl = serverRepository.serverUrl.first()
            if (serverUrl == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Brak połączenia z serwerem. Przejdź do Ustawień i zeskanuj kod QR.",
                )
                return@launch
            }
            sendToServer(serverUrl, updatedMessages)
        }
    }

    private suspend fun sendToServer(serverUrl: String, messages: List<ChatMessage>) {
        try {
            val body = buildJsonBody(messages)
            val baseUrl = serverUrl.trimEnd('/')
            val url = URL("$baseUrl/chat")

            val (responseCode, responseBody) = withContext(Dispatchers.IO) {
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    doOutput = true
                    connectTimeout = 10_000
                    readTimeout = 90_000
                }
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
                val code = conn.responseCode
                val text = if (code == 200) {
                    conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                } else {
                    conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "Nieznany błąd"
                }
                code to text
            }

            if (responseCode == 200) {
                val json = JSONObject(responseBody)
                val reply = json.getString("reply")
                val assistantMsg = ChatMessage(role = "assistant", content = reply)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMsg,
                    isLoading = false,
                )
                chatRepository.saveMessage(_uiState.value.sessionId, "assistant", reply)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Błąd serwera ($responseCode): $responseBody",
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Nie można połączyć z serwerem: ${e.message}",
            )
        }
    }

    private fun buildJsonBody(messages: List<ChatMessage>): String {
        val messagesArray = messages.joinToString(",") { msg ->
            """{"role":"${msg.role}","content":${JSONObject.quote(msg.content)}}"""
        }
        val sessionId = _uiState.value.sessionId
        return """{"session_id":"$sessionId","messages":[$messagesArray]}"""
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearChat() {
        _uiState.value = ChatUiState()
    }
}
