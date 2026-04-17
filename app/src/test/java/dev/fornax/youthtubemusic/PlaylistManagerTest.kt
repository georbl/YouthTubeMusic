package dev.fornax.youthtubemusic

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlaylistManagerTest {

    private lateinit var playlistManager: PlaylistManager

    @Before
    fun setUp() {
        playlistManager = PlaylistManager()
    }

    @Test
    fun `initial state is INIT`() {
        assertEquals(PlaylistState.INIT, playlistManager.playlistState)
    }

    @Test
    fun `playlistUrl is initially empty`() {
        assertEquals("", playlistManager.playlistUrl)
    }

    @Test
    fun `setting playlistUrl updates the property`() {
        val testUrl = "https://www.youtube.com/playlist?list=PL3oW2tjiIxvScl18T_xTPr8L-7I6xI6Yn"
        playlistManager.playlistUrl = testUrl
        assertEquals(testUrl, playlistManager.playlistUrl)
    }

    @Test
    fun `mediaItems is initially empty`() {
        assertEquals(0, playlistManager.mediaItems.size)
    }
}
