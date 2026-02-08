package np.ict.mad.mad25_p03_team03.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

object MusicManager {
    private var player: ExoPlayer? = null

    var currentSong by mutableStateOf<SongItem?>(null)
    var isPlaying by mutableStateOf(false)
    var playlist by mutableStateOf(listOf<SongItem>())
    var currentIndex by mutableStateOf(-1)

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()

            player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Updates song when music changes automatically
                    val index = player?.currentMediaItemIndex ?: -1
                    if (index != -1 && index < playlist.size) {
                        currentIndex = index
                        currentSong = playlist[index]
                    }
                }
            })
        }
        return player!!
    }

    fun release() {
        player?.release()
        player = null
    }
}