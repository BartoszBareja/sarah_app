package com.example.sarah_app.data.repository

import com.example.sarah_app.data.db.ChatMessageDao
import com.example.sarah_app.data.db.ChatMessageEntity
import com.example.sarah_app.data.db.ChatSessionDao
import com.example.sarah_app.data.db.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao,
) {
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = sessionDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        messageDao.getMessagesForSession(sessionId)

    suspend fun ensureSessionExists(sessionId: String, preview: String, createdAt: Long) {
        if (sessionDao.getById(sessionId) == null) {
            sessionDao.insert(ChatSessionEntity(id = sessionId, createdAt = createdAt, preview = preview))
        }
    }

    suspend fun saveMessage(sessionId: String, role: String, content: String) {
        messageDao.insert(
            ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                content = content,
                timestamp = System.currentTimeMillis(),
            )
        )
    }
}
