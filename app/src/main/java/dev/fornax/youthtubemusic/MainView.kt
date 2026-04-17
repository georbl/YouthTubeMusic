package dev.fornax.youthtubemusic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dev.fornax.youthtubemusic.ui.theme.YouthTubeMusicTheme
import kotlinx.coroutines.launch


@Composable
fun MainView(
    viewModel: MainViewModel
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val player = viewModel.player
    val playlistManager = viewModel.playlistManager

    // Listen for track changes to update UI
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                viewModel.currentSongIndex = player.currentMediaItemIndex
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                    viewModel.currentSongIndex = player.currentMediaItemIndex
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    if (showSettings) {
        SettingsView(
            playlistUrl = playlistManager.playlistUrl,
            onPlaylistUrlChange = { newUrl ->
                viewModel.viewModelScope.launch {
                    viewModel.settingsManager.updatePlaylistUrl(newUrl)
                }
            },
            onBack = {
                @Suppress("AssignedValueIsNeverRead")
                showSettings = false
            }
        )
    }else {
        StartView(
            playlistManager = playlistManager,
            currentSongIndex = viewModel.currentSongIndex,
            onLoadPlaylist = { viewModel.loadPlaylist() },
            onShowSettingsChange = {
                @Suppress("AssignedValueIsNeverRead")
                showSettings = it
            },
            player = player,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartView(
    playlistManager: IPlaylistManager,
    currentSongIndex: Int,
    onLoadPlaylist: () -> Unit,
    onShowSettingsChange: (Boolean) -> Unit,
    player: Player?,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }


        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (playlistManager.playlistState == PlaylistState.READY) {
                                Text(
                                    text = playlistManager.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime < 500) {
                                clickCount++
                            } else {
                                clickCount = 1
                            }
                            lastClickTime = currentTime
                            if (clickCount >= 7) {
                                onShowSettingsChange(true)
                                clickCount = 0
                            }

                        },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Settings",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .padding(0.dp).size(48.dp)
                                    .scale(1.5f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onLoadPlaylist) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Playlist",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                if (playlistManager.playlistState == PlaylistState.READY && playlistManager.mediaItems.isNotEmpty()) {
                    MusicPlayerControlView(player = player)
                }
            }
        ) { innerPadding ->
            PlaylistView(
                playlistManager = playlistManager,
                currentSongIndex = currentSongIndex,
                onTrackClick =  { index ->
                    player?.seekTo(index, 0)
                    player?.play()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }


@Preview(showBackground = true)
@Composable
fun StartViewPreview() {
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
        override var playlistUrl = "https://example.com"
    }

    YouthTubeMusicTheme {
        StartView(
            playlistManager = mockPlaylist,
            currentSongIndex = 0,
            onLoadPlaylist = {},
            onShowSettingsChange = {},
            player = null
        )
    }
}
