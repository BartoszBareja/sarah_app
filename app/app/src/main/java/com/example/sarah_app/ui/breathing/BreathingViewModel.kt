package com.example.sarah_app.ui.breathing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BreathingViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val soundVolume: StateFlow<Float> = settingsRepository.soundVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.5f)
}
