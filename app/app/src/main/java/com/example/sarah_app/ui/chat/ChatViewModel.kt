package com.example.sarah_app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
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
    val sessionId: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

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
            val serverUrl = serverRepository.serverUrl.first()
            if (serverUrl == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Brak połączenia z serwerem. Wróć i zeskanuj kod QR.",
                )
                return@launch
            }
            sendToServer(serverUrl, updatedMessages)
        }
    }

    private suspend fun sendToServer(serverUrl: String, messages: List<ChatMessage>) {
        try {
            val body = buildJsonBody(messages)
            val url = URL("$serverUrl/chat")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 30_000
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                val reply = json.getString("reply")
                val sessionId = json.getString("session_id")

                val assistantMsg = ChatMessage(role = "assistant", content = reply)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMsg,
                    isLoading = false,
                    sessionId = sessionId,
                )
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Nieznany błąd"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Błąd serwera (${conn.responseCode}): $error",
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
        val sessionPart = _uiState.value.sessionId?.let { """"session_id":"$it",""" } ?: ""
        return """{${sessionPart}"messages":[$messagesArray]}"""
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearChat() {
        _uiState.value = ChatUiState()
    }
}
