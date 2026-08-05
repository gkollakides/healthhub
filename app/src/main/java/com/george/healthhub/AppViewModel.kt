package com.george.healthhub

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore("health_hub_settings")

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val accentKey = stringPreferencesKey("accent")
    val accent = application.dataStore.data.map { prefs ->
        Accent.entries.firstOrNull { it.name == prefs[accentKey] } ?: Accent.Blue
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Accent.Blue)

    fun setAccent(value: Accent) = viewModelScope.launch {
        getApplication<Application>().dataStore.edit { it[accentKey] = value.name }
    }
}
