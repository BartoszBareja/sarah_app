package com.example.sarah_app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 0,
    val sex: String = "",
    val pronouns: String = "",
    val mood: Int = 1,
    val photoUri: String? = null,
)
