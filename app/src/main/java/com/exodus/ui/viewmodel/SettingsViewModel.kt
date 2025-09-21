package com.exodus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exodus.data.model.UpdateInfo
import com.exodus.data.model.UpdateStatus
import com.exodus.data.repository.UpdateRepository
import com.exodus.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val appVersion: String = "1.24",
    val buildNumber: String = "124",
    val updateStatus: UpdateStatus = UpdateStatus.NO_UPDATE,
    val updateInfo: UpdateInfo? = null,
    val lastUpdateCheck: String = "Never"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {
    
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
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                AppLogger.d("SettingsViewModel", "Starting update check...")
                
                _uiState.value = _uiState.value.copy(
                    updateStatus = UpdateStatus.CHECKING
                )
                
                val updateInfo = updateRepository.checkForUpdates()
                
                val status = when {
                    updateInfo.isUpdateAvailable -> UpdateStatus.UPDATE_AVAILABLE
                    else -> UpdateStatus.NO_UPDATE
                }
                
                val currentTime = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                
                _uiState.value = _uiState.value.copy(
                    updateStatus = status,
                    updateInfo = updateInfo,
                    lastUpdateCheck = currentTime
                )
                
                AppLogger.d("SettingsViewModel", "Update check completed. Status: $status")
                
            } catch (e: Exception) {
                AppLogger.e("SettingsViewModel", "Error during update check", e)
                _uiState.value = _uiState.value.copy(
                    updateStatus = UpdateStatus.ERROR,
                    updateInfo = null
                )
            }
        }
    }
    
    fun downloadUpdate() {
        val updateInfo = _uiState.value.updateInfo
        if (updateInfo?.isUpdateAvailable == true && updateInfo.releaseInfo != null) {
            AppLogger.d("SettingsViewModel", "Opening update download for version ${updateInfo.latestVersion}")
            updateRepository.openUpdatePage(updateInfo.releaseInfo)
        }
    }
    
    fun dismissUpdateStatus() {
        _uiState.value = _uiState.value.copy(
            updateStatus = UpdateStatus.NO_UPDATE,
            updateInfo = null
        )
    }
}