package np.ict.mad.mad25_p03_team03.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import np.ict.mad.mad25_p03_team03.R
import np.ict.mad.mad25_p03_team03.ui.theme.MAD25_P03_Team03Theme

class MusicProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("TITLE") ?: "Unknown"
        val artist = intent.getStringExtra("ARTIST") ?: "Unknown"
        val lyrics = intent.getStringExtra("LYRICS") ?: "No lyrics available."
        val drawableId = intent.getIntExtra("IMAGE_ID", R.drawable.arcanepic)
        setContent {
            MAD25_P03_Team03Theme {
                        MusicProfileScreen(title, artist, lyrics, drawableId, onBack = { finish() })
            }
        }
    }
}

@Composable
fun MusicProfileScreen(
    title: String,
    artist: String,
    lyrics: String,
    drawableId: Int,
    onBack: () -> Unit
){
    val context = LocalContext.current
    // use the same shared player
    val exoPlayer = MusicManager.getPlayer(context)

    // Track Play/Pause state
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }

    // Listener to update button if music ends or changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2C1C45), Color(0xFF0F0F1A))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Now Playing", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Album Art
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier.size(300.dp)
            ) {
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(artist, fontSize = 18.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPrevious() }, modifier = Modifier.size(50.dp)) {
                    Icon(Icons.Filled.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }

                IconButton(
                    onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    modifier = Modifier.size(80.dp).background(Color(0xFF6200EE), shape = RoundedCornerShape(50))
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }

                IconButton(onClick = { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNext() }, modifier = Modifier.size(50.dp)) {
                    Icon(Icons.Filled.SkipNext, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrolling the lyrics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = lyrics.replace("\\n", "\n"),
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }
            }
        }
    }
}