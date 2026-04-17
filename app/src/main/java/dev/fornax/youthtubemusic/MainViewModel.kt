package dev.fornax.youthtubemusic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val settingsManager = SettingsManager(application)

    // PlaylistManager is now inside the ViewModel
    val playlistManager = PlaylistManager()

    val player: ExoPlayer = ExoPlayer.Builder(application).build()

    val mediaSession: MediaSession = MediaSession.Builder(application, player).build()

    var currentSongIndex by mutableIntStateOf(-1)

    init {
        NewPipe.init(DownloaderImpl.init(null))

        viewModelScope.launch {
            settingsManager.playlistUrl
                .distinctUntilChanged()
                .collectLatest { url ->
                    Log.d("ytm", "New URL from DataStore: $url")
                    playlistManager.playlistUrl = url
                    loadPlaylist()
                }
        }
    }

    fun loadPlaylist() {
        viewModelScope.launch {
            player.stop()
            currentSongIndex = -1
            playlistManager.loadPlaylist()

            if (playlistManager.playlistState == PlaylistState.READY) {
                player.setMediaItems(playlistManager.mediaItems)
                player.playWhenReady = false
                player.prepare()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaSession.release()
        player.release()
    }
}
