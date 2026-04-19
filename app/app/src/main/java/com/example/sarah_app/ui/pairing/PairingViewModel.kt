package com.example.sarah_app.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
) : ViewModel() {

    val serverUrl: Flow<String?> = serverRepository.serverUrl

    fun onServerUrlScanned(url: String) {
        viewModelScope.launch {
            serverRepository.saveServerUrl(url)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            serverRepository.clearServerUrl()
        }
    }
}
