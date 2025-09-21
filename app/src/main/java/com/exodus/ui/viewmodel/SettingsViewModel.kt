package com.exodus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val appVersion: String = "1.20",
    val buildNumber: String = "120"
)

class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDarkMode = !_uiState.value.isDarkMode
            )
            // In a real app, you would save this preference to SharedPreferences or DataStore
            // For now, we'll just update the state
        }
    }
}