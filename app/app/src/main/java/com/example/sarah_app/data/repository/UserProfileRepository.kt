package com.example.sarah_app.data.repository

import com.example.sarah_app.data.db.UserProfileDao
import com.example.sarah_app.data.db.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: UserProfileDao
) {
    fun getProfile(): Flow<UserProfileEntity?> = dao.getProfile()

    suspend fun upsert(profile: UserProfileEntity) = dao.upsert(profile)
}
