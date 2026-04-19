package com.example.sarah_app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "server_prefs")

@Singleton
class ServerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    }

    /** null = jeszcze nie sparowano */
    val serverUrl: Flow<String?> = context.serverDataStore.data
        .map { it[SERVER_URL_KEY] }

    suspend fun saveServerUrl(url: String) {
        context.serverDataStore.edit { it[SERVER_URL_KEY] = url }
    }

    suspend fun clearServerUrl() {
        context.serverDataStore.edit { it.remove(SERVER_URL_KEY) }
    }
}
