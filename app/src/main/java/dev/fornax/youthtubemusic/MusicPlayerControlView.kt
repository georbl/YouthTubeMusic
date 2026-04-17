package dev.fornax.youthtubemusic

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player

/**
 * Stateful version of the Music Player Control View that interacts with the Media3 Player.
 */
@Composable
fun MusicPlayerControlView(
    player: Player?,
    modifier: Modifier = Modifier
) {
    // 1. Reactive State: UI updates automatically when these change
    var isPlaying by remember { mutableStateOf(player?.isPlaying) }
    var trackTitle by remember {
        mutableStateOf(player?.mediaMetadata?.title?.toString() ?: "No Track")
    }

    // 2. Observer: Listens to the Media3 Player (ExoPlayer) events
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onMediaMetadataChanged(metadata: androidx.media3.common.MediaMetadata) {
                trackTitle = metadata.title?.toString() ?: "Unknown Title"
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    // 3. Delegate to stateless Composable
    MusicPlayerControlContent(
        trackTitle = trackTitle,
        isPlaying = isPlaying == true,
        onPreviousClick = { player?.seekToPrevious() },
        onPlayPauseClick = { if (isPlaying == true) player?.pause() else player?.play() },
        onNextClick = { player?.seekToNext() },
        modifier = modifier
    )
}

/**
 * Stateless version of the Music Player Control View, easy to use in Previews.
 */
@Composable
fun MusicPlayerControlContent(
    trackTitle: String,
    isPlaying: Boolean,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Song Title - Now at the top and centered
            Text(
                text = trackTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                softWrap = false,
                modifier = Modifier
                    .basicMarquee(
                        iterations = if (isPlaying) Int.MAX_VALUE else 0, // Loop forever
                        initialDelayMillis = 2000, // Delay before starting
                    )
                    .padding(bottom = 12.dp)
            )

            // Controls Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPreviousClick) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(30.dp))

                // Centered Play/Pause Button
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(30.dp))

                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MusicPlayerControlPreview() {
    MusicPlayerControlContent(
        trackTitle = "Insane Ridiculously Extremely Very Long Sample Track Title",
        isPlaying = false,
        onPreviousClick = {},
        onPlayPauseClick = {},
        onNextClick = {}
    )
}