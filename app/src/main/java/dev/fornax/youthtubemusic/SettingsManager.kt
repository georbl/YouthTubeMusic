package dev.fornax.youthtubemusic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val PLAYLIST_URL = stringPreferencesKey("playlist_url")
        const val DEFAULT_PLAYLIST_URL = ""
    }

    val playlistUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PLAYLIST_URL] ?: DEFAULT_PLAYLIST_URL
        }

    suspend fun updatePlaylistUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PLAYLIST_URL] = url
        }
    }
}
