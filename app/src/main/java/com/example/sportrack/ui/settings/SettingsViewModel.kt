package com.example.sportrack.ui.settings

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportrack.data.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val mode: String = "cut" // "cut" = похуднення, "bulk" = набір
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.applicationContext.dataStore

    private val THEME_KEY = booleanPreferencesKey("dark_theme")
    private val MODE_KEY = stringPreferencesKey("mode")

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // підписка на зміни в DataStore
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val isDark = prefs[THEME_KEY] ?: false
                val mode = prefs[MODE_KEY] ?: "cut"
                _uiState.value = SettingsUiState(isDarkTheme = isDark, mode = mode)
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[THEME_KEY] = enabled
            }
        }
    }

    fun setMode(mode: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[MODE_KEY] = mode
            }
        }
    }

    fun toggleMode() {
        val newMode = if (_uiState.value.mode == "cut") "bulk" else "cut"
        setMode(newMode)
    }
}
