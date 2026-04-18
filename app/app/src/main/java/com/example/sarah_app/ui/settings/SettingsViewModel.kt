package com.example.sarah_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val soundVolume = repository.soundVolume.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f
    )
    val notificationsEnabled = repository.notificationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val language = repository.language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "en"
    )

    fun setSoundVolume(v: Float) = viewModelScope.launch { repository.setSoundVolume(v) }
    fun setNotificationsEnabled(v: Boolean) = viewModelScope.launch { repository.setNotificationsEnabled(v) }
    fun setLanguage(v: String) = viewModelScope.launch { repository.setLanguage(v) }
}
