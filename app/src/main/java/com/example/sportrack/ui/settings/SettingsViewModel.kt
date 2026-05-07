package com.example.sportrack.ui.settings

import android.app.Application
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportrack.data.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val mode: String = "cut",
    val userName: String = "Спортсмен",
    val profileImageUri: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.applicationContext.dataStore

    private val THEME_KEY = booleanPreferencesKey("dark_theme")
    private val MODE_KEY = stringPreferencesKey("mode")
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val IMAGE_KEY = stringPreferencesKey("profile_image")

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val isDark = prefs[THEME_KEY] ?: false
                val mode = prefs[MODE_KEY] ?: "cut"
                val name = prefs[NAME_KEY] ?: "Спортсмен"
                val image = prefs[IMAGE_KEY]
                _uiState.value = SettingsUiState(
                    isDarkTheme = isDark,
                    mode = mode,
                    userName = name,
                    profileImageUri = image
                )
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

    fun setUserName(name: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[NAME_KEY] = name
            }
        }
    }

    fun setProfileImageUri(uriString: String?) {
        viewModelScope.launch {
            if (uriString != null) {
                // Копіюємо файл у фоновому потоці
                val localUri = withContext(Dispatchers.IO) {
                    copyImageToInternalStorage(uriString)
                }

                if (localUri != null) {
                    dataStore.edit { prefs ->
                        prefs[IMAGE_KEY] = localUri
                    }
                }
            } else {
                dataStore.edit { prefs ->
                    prefs.remove(IMAGE_KEY)
                }
            }
        }
    }

    // Функція для копіювання фото в пам'ять додатку
    private fun copyImageToInternalStorage(uriString: String): String? {
        val context = getApplication<Application>().applicationContext
        val uri = Uri.parse(uriString)

        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            // Створюємо файл у внутрішній директорії додатку
            val file = File(context.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            // Повертаємо Uri вже нашого, локального файлу
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}