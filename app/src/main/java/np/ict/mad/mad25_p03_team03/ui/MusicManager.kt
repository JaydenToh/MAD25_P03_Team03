package np.ict.mad.mad25_p03_team03.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import np.ict.mad.mad25_p03_team03.SongItem

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

    fun setPlaylistAndPlay(songs: List<SongItem>, index: Int, context: Context) {
        val p = getPlayer(context)
        if (playlist != songs) {
            playlist = songs
            p.clearMediaItems()
            val mediaItems = songs.map { MediaItem.fromUri(it.audioUrl) }
            p.setMediaItems(mediaItems)
        }
        if (currentIndex != index) {
            p.seekTo(index, 0)
            p.prepare()
            p.play()
            currentIndex = index
            currentSong = songs[index]
        } else {
            playPause(context)
        }
    }

    fun playPause(context: Context) {
        val p = getPlayer(context)
        if (p.isPlaying) p.pause() else p.play()
    }

    fun next(context: Context) {
        val p = getPlayer(context)
        if (p.hasNextMediaItem()) p.seekToNext()
    }

    fun previous(context: Context) {
        val p = getPlayer(context)
        if (p.hasPreviousMediaItem()) p.seekToPrevious()
    }

    fun release() {
        player?.release()
        player = null
    }
}