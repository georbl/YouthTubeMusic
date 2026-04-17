package dev.fornax.youthtubemusic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata


@Composable
fun PlaylistView(
    playlistManager: IPlaylistManager,
    currentSongIndex: Int,
    onTrackClick: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (playlistManager.playlistState != PlaylistState.READY) {
            item (
                key = "error",
                contentType = "error",
            ){
                PlaylistStateView(playlistData = playlistManager, modifier = Modifier.fillParentMaxSize())
            }
        } else {
            itemsIndexed(playlistManager.mediaItems) { index, item ->
                val isSelected = index == currentSongIndex
                ListItem(
                    headlineContent = {
                        Text(
                            text = item.mediaMetadata.title?.toString() ?: "Unknown Track",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTrackClick(index)
                        },
                    colors = if (isSelected) {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.4f
                            )
                        )
                    } else {
                        ListItemDefaults.colors()
                    }
                )
            }
        }
    }
}


@Composable
fun PlaylistStateView( playlistData: IPlaylistManager, modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val playlistState = playlistData.playlistState
        if (playlistState == PlaylistState.INIT || playlistState == PlaylistState.LOADING) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = stringResource(R.string.unable_to_load_playlist),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistViewPreviewLoading() {
    val mockPlaylist = object : IPlaylistManager {
        override val name: String = ""
        override val mediaItems: List<MediaItem> = emptyList()
        override var playlistState = PlaylistState.LOADING
        override var playlistUrl = ""
    }

    MaterialTheme {
        PlaylistView(
            playlistManager = mockPlaylist,
            currentSongIndex = 1,
            onTrackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistViewPreviewError() {
    val mockPlaylist = object : IPlaylistManager {
        override val name: String = ""
        override val mediaItems: List<MediaItem> = emptyList()
        override var playlistState = PlaylistState.ERROR
        override var playlistUrl = ""
    }

    MaterialTheme {
        PlaylistView(
            playlistManager = mockPlaylist,
            currentSongIndex = 1,
            onTrackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistViewPreview() {
    val mockPlaylist = object : IPlaylistManager {
        override val name: String = "My Awesome Playlist"
        override val mediaItems: List<MediaItem> = listOf(
            MediaItem.Builder()
                .setMediaMetadata(MediaMetadata.Builder().setTitle("Song 1").build())
                .build(),
            MediaItem.Builder()
                .setMediaMetadata(MediaMetadata.Builder().setTitle("Song 2").build())
                .build(),
        )
        override var playlistState = PlaylistState.READY
        override var playlistUrl = ""
    }

    MaterialTheme {
        PlaylistView(
            playlistManager = mockPlaylist,
            currentSongIndex = 1,
            onTrackClick = {}
        )
    }
}
