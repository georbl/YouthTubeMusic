package dev.fornax.youthtubemusic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.stream.StreamInfoItem


/** Lifecycle of playlist data */
enum class PlaylistState{
    /** initial state  ot playlist */
    INIT,
    /** fetching playlist data from network */
    LOADING,
    /** playlist is loaded and ready to use */
    READY,
    /** error occurred while loading playlist */
    ERROR
}

/**
 * Interface for accessing playlist information.
 */
interface IPlaylistManager {
    val name: String
    val mediaItems: List<MediaItem>
    var playlistState: PlaylistState

    var playlistUrl: String
}

/**
 * Manages fetching and storing playlist data.
 * Implements IPlaylistData to provide direct access to the loaded playlist.
 */
class PlaylistManager : IPlaylistManager {
    
    private var _name: String = ""
    private var _mediaItems: List<MediaItem> = emptyList()

    override val name: String get() = _name
    override val mediaItems: List<MediaItem> get() = _mediaItems

    override var playlistState by mutableStateOf(PlaylistState.INIT)

    override var playlistUrl by mutableStateOf("")

    /**
     * Fetches the playlist data and media items.
     * Returns true if successful, false otherwise.
     */
    suspend fun loadPlaylist() {

        if (playlistUrl.isEmpty()){
            Log.e("ytm", "Playlist URL is empty")
            playlistState = PlaylistState.ERROR
            return
        }

        playlistState = PlaylistState.LOADING

        withContext(Dispatchers.IO) {
            try {
                val youtube = YouTube
                val playlistExtractor = youtube.getPlaylistExtractor(playlistUrl)

                playlistExtractor.fetchPage()
                _name = playlistExtractor.name
                
                var playlistPage: ListExtractor.InfoItemsPage<StreamInfoItem>? = playlistExtractor.initialPage
                val allMediaItems = mutableListOf<MediaItem>()

                while (playlistPage != null) {
                    val batch = playlistPage.items.map { item ->
                        async {
                            fetchAudioStream(item, youtube)
                        }
                    }.awaitAll().filterNotNull()
                    
                    allMediaItems.addAll(batch)

                    playlistPage = if (playlistPage.hasNextPage()) {
                        playlistExtractor.getPage(playlistPage.nextPage)
                    } else {
                        null
                    }
                }
                _mediaItems = allMediaItems
                playlistState = PlaylistState.READY
            } catch (e: Exception) {
                Log.e("ytm", "Error fetching playlist: ${e.message}")
                playlistState = PlaylistState.ERROR
            }
        }
    }

    private fun fetchAudioStream(
        item: StreamInfoItem?,
        youtube: YoutubeService?
    ): MediaItem? {

        if (item == null || youtube == null) return null

        Log.d("ytm", "Fetching stream for: ${item.name}")
        return try {
            val streamExtractor = youtube.getStreamExtractor(item.url)
            streamExtractor.fetchPage()
            val audioStreams = streamExtractor.audioStreams

            if (audioStreams.isNotEmpty()) {
                MediaItem.Builder()
                    .setUri(audioStreams[0].content)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.name)
                            .build()
                    )
                    .build()
            } else null
        } catch (e: Exception) {
            Log.e("ytm", "Error fetching stream for ${item.name}: ${e.message}")
            null
        }
    }
}
