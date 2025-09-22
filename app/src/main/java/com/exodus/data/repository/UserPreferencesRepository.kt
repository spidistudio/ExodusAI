package com.exodus.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.exodus.data.model.LLMProvider
import com.exodus.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                "exodus_encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            AppLogger.w("UserPrefs", "Failed to create encrypted preferences, using regular: ${e.message}")
            context.getSharedPreferences("exodus_prefs", Context.MODE_PRIVATE)
        }
    }
    
    // State flows for reactive updates
    private val _groqApiKey = MutableStateFlow(getGroqApiKey())
    val groqApiKey: StateFlow<String?> = _groqApiKey.asStateFlow()
    
    private val _selectedProvider = MutableStateFlow(getSelectedProvider())
    val selectedProvider: StateFlow<LLMProvider> = _selectedProvider.asStateFlow()
    
    private val _isDarkMode = MutableStateFlow(getIsDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    // Groq API Key
    fun getGroqApiKey(): String? {
        return preferences.getString(GROQ_API_KEY, null)?.takeIf { it.isNotBlank() }
    }
    
    fun setGroqApiKey(apiKey: String?) {
        preferences.edit().putString(GROQ_API_KEY, apiKey?.trim()).apply()
        _groqApiKey.value = apiKey?.trim()
        AppLogger.d("UserPrefs", "Groq API key ${if (apiKey.isNullOrBlank()) "cleared" else "saved"}")
    }
    
    // Provider Selection
    fun getSelectedProvider(): LLMProvider {
        val providerName = preferences.getString(SELECTED_PROVIDER, LLMProvider.AUTO.name)
        return try {
            LLMProvider.valueOf(providerName ?: LLMProvider.AUTO.name)
        } catch (e: Exception) {
            AppLogger.w("UserPrefs", "Invalid provider name: $providerName, defaulting to AUTO")
            LLMProvider.AUTO
        }
    }
    
    fun setSelectedProvider(provider: LLMProvider) {
        preferences.edit().putString(SELECTED_PROVIDER, provider.name).apply()
        _selectedProvider.value = provider
        AppLogger.d("UserPrefs", "Provider selection saved: ${provider.displayName}")
    }
    
    // Dark Mode
    fun getIsDarkMode(): Boolean {
        return preferences.getBoolean(IS_DARK_MODE, true) // Default to dark mode
    }
    
    fun setIsDarkMode(isDark: Boolean) {
        preferences.edit().putBoolean(IS_DARK_MODE, isDark).apply()
        _isDarkMode.value = isDark
        AppLogger.d("UserPrefs", "Dark mode ${if (isDark) "enabled" else "disabled"}")
    }
    
    // Clear all preferences (for debugging/reset)
    fun clearAll() {
        preferences.edit().clear().apply()
        _groqApiKey.value = null
        _selectedProvider.value = LLMProvider.AUTO
        _isDarkMode.value = true
        AppLogger.i("UserPrefs", "All preferences cleared")
    }
    
    companion object {
        private const val GROQ_API_KEY = "groq_api_key"
        private const val SELECTED_PROVIDER = "selected_provider"
        private const val IS_DARK_MODE = "is_dark_mode"
    }
}