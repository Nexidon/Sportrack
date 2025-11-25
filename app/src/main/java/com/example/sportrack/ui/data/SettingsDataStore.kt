package com.example.sportrack.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Extension, який створює DataStore тільки один раз
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
