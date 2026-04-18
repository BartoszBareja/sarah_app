package com.example.sarah_app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SOUND_VOLUME          = floatPreferencesKey("sound_volume")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications")
        val LANGUAGE              = stringPreferencesKey("language")
        val USER_RATING           = intPreferencesKey("user_rating")
    }

    val soundVolume: Flow<Float> = context.dataStore.data.map { it[SOUND_VOLUME] ?: 0.5f }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "en" }
    val userRating: Flow<Int> = context.dataStore.data.map { it[USER_RATING] ?: 0 }

    suspend fun setSoundVolume(value: Float) = context.dataStore.edit { it[SOUND_VOLUME] = value }
    suspend fun setNotificationsEnabled(value: Boolean) = context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[LANGUAGE] = value }
    suspend fun setUserRating(value: Int) = context.dataStore.edit { it[USER_RATING] = value }
}
