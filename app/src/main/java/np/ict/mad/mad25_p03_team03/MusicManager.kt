package np.ict.mad.mad25_p03_team03

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object MusicManager {
    private var player: ExoPlayer? = null

    // Get the existing player or create a new one if missing
    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
        return player!!
    }

    fun release() {
        player?.release()
        player = null
    }
}