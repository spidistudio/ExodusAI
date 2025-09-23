package com.exodus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exodus.data.model.UpdateInfo
import com.exodus.data.model.UpdateStatus
import com.exodus.data.repository.UpdateRepository
import com.exodus.data.repository.UserPreferencesRepository
import com.exodus.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val appVersion: String = "1.28",
    val buildNumber: String = "128",
    val groqApiKey: String? = null,
    val updateStatus: UpdateStatus = UpdateStatus.NO_UPDATE,
    val updateInfo: UpdateInfo? = null,
    val lastUpdateCheck: String = "Never"
)

private data class LocalSettingsState(
    val appVersion: String = "1.28",
    val buildNumber: String = "128",
    val updateStatus: UpdateStatus = UpdateStatus.NO_UPDATE,
    val updateInfo: UpdateInfo? = null,
    val lastUpdateCheck: String = "Never"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // Combine user preferences with local state
    private val _localState = MutableStateFlow(LocalSettingsState())
    val uiState: StateFlow<SettingsUiState> = combine(
        _localState,
        userPreferencesRepository.groqApiKey,
        userPreferencesRepository.isDarkMode
    ) { local: LocalSettingsState, groqApiKey: String?, isDarkMode: Boolean ->
        SettingsUiState(
            isDarkMode = isDarkMode,
            appVersion = local.appVersion,
            buildNumber = local.buildNumber,
            groqApiKey = groqApiKey,
            updateStatus = local.updateStatus,
            updateInfo = local.updateInfo,
            lastUpdateCheck = local.lastUpdateCheck
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentMode = userPreferencesRepository.getIsDarkMode()
            userPreferencesRepository.setIsDarkMode(!currentMode)
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                AppLogger.d("SettingsViewModel", "Starting update check...")
                
                _localState.value = _localState.value.copy(
                    updateStatus = UpdateStatus.CHECKING
                )
                
                val updateInfo = updateRepository.checkForUpdates()
                
                val status = when {
                    updateInfo.isUpdateAvailable -> UpdateStatus.UPDATE_AVAILABLE
                    else -> UpdateStatus.NO_UPDATE
                }
                
                val currentTime = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                
                _localState.value = _localState.value.copy(
                    updateStatus = status,
                    updateInfo = updateInfo,
                    lastUpdateCheck = currentTime
                )
                
                AppLogger.d("SettingsViewModel", "Update check completed. Status: $status")
                
            } catch (e: Exception) {
                AppLogger.e("SettingsViewModel", "Error during update check", e)
                _localState.value = _localState.value.copy(
                    updateStatus = UpdateStatus.ERROR,
                    updateInfo = null
                )
            }
        }
    }
    
    fun downloadUpdate() {
        val updateInfo = _localState.value.updateInfo
        if (updateInfo?.isUpdateAvailable == true && updateInfo.releaseInfo != null) {
            AppLogger.d("SettingsViewModel", "Opening update download for version ${updateInfo.latestVersion}")
            updateRepository.openUpdatePage(updateInfo.releaseInfo)
        }
    }
    
    fun dismissUpdateStatus() {
        _localState.value = _localState.value.copy(
            updateStatus = UpdateStatus.NO_UPDATE,
            updateInfo = null
        )
    }
    
    fun updateGroqApiKey(apiKey: String) {
        viewModelScope.launch {
            userPreferencesRepository.setGroqApiKey(apiKey.trim().takeIf { it.isNotBlank() })
        }
    }
}