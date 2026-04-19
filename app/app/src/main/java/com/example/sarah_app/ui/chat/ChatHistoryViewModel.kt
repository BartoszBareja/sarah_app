package com.example.sarah_app.ui.chat

import androidx.lifecycle.ViewModel
import com.example.sarah_app.data.db.ChatSessionEntity
import com.example.sarah_app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    chatRepository: ChatRepository,
) : ViewModel() {

    val sessions: StateFlow<List<ChatSessionEntity>> = chatRepository
        .getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
