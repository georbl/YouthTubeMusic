package dev.fornax.youthtubemusic

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.fornax.youthtubemusic.ui.theme.YouthTubeMusicTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        // 'this' is a CreationExtras object
                        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                        MainViewModel(app)
                    }
                }
            )
            if (mainViewModel.playlistManager.playlistState == PlaylistState.INIT) {
                mainViewModel.loadPlaylist()
            }
            YouthTubeMusicTheme {
                MainView(mainViewModel)
            }
        }
    }
}

