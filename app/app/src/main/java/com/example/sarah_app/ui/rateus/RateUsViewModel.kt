package com.example.sarah_app.ui.rateus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sarah_app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RateUsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val rating = repository.userRating.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    fun setRating(value: Int) = viewModelScope.launch { repository.setUserRating(value) }
}
