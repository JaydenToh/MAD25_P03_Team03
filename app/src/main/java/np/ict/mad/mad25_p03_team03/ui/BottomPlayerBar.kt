package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import np.ict.mad.mad25_p03_team03.SongItem

@Composable
fun BottomPlayerBar(
    song: SongItem,
    isPlaying: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRepeat: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth().height(85.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Image & Text
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = song.drawableId),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRepeat) {
                    val color = if (repeatMode == ExoPlayer.REPEAT_MODE_OFF) Color.Gray else Color(0xFFBB86FC)
                    Icon(if (repeatMode == ExoPlayer.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat, "Repeat", tint = color)
                }
                IconButton(onClick = onPrevious) { Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White) }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.background(Color(0xFFBB86FC), shape = RoundedCornerShape(50)).size(40.dp)
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play", tint = Color.Black)
                }
                IconButton(onClick = onNext) { Icon(Icons.Filled.SkipNext, "Next", tint = Color.White) }
            }
        }
    }
}